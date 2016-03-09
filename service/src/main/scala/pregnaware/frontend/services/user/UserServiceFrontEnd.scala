package pregnaware.frontend.services.user

import java.time.LocalDate

import pregnaware.frontend.FrontEndDirectives
import pregnaware.frontend.entities.{AddUserRequest, EditUserRequest, LoginRequest}
import org.mindrot.jbcrypt.BCrypt
import spray.routing._
import pregnaware.utils.Json4sSupport._
import pregnaware.utils.ResponseCodes

import scala.util.{Failure, Success}

trait UserServiceFrontEnd extends FrontEndDirectives {

  // The password hashing salt
  private val salt = BCrypt.gensalt()

  // The routes provided by this service
  val userServiceRoutes: Route =
    authenticate ~ login ~
      getUser ~ postUser ~ putUser ~
      putFriend ~ deleteFriend ~
      putDueDate ~ deleteDueDate ~
      getUserState ~ putUserState

  /** () -> OK | NotFound */
  def authenticate: Route = get {
    path("authenticate") {
      getUserId("authenticate") { _ =>
        complete(ResponseCodes.OK)
      }
    }
  }

  /** LoginRequest -> sessionId */
  def login: Route = post {
    path("login") {
      entity(as[LoginRequest]) { request =>
        routeFuture("login[user]", getUserService.findUser(request.email)) { user =>
          routeFuture("login[session]", getSessionPersistence.getSession(user.userId)) { session =>
            complete(ResponseCodes.OK -> session)
          }
        }
      }
    }
  }

  /** () -> WrappedUser */
  def getUser: Route = get {
    path("user") {
      getUser("putUser[fetch]") { user =>
        complete(user)
      }
    }
  }

  /** NewUserRequest -> sessionId */
  def postUser: Route = post {
    path("user") {
      entity(as[AddUserRequest]) { request =>
        val passwordHash = BCrypt.hashpw(request.password, salt)
        val newUserFut = getUserService.postUser(request.displayName, request.email, passwordHash)

        routeFuture("newUser[create]", newUserFut) { user =>
          routeFuture("newUser[session]", getSessionPersistence.getSession(user.userId)) { session =>
            complete(ResponseCodes.OK -> session)
          }
        }
      }
    }
  }

  /** EditUserRequest -> WrappedUser */
  def putUser: Route = put {
    path("user") {
      getUser("putUser[fetch]") { user =>
        entity(as[EditUserRequest]) { request =>

          val displayName = request.displayName.getOrElse(user.displayName)
          val email = request.email.getOrElse(user.email)
          val passwordHash = request.password match {
            case None => user.passwordHash
            case Some(password) => BCrypt.hashpw(password, salt)
          }

          val editUserFut = getUserService.putUser(user.userId, displayName, email, passwordHash)
          routeFuture("putUser[edit]", editUserFut) { updatedUser =>
            complete(ResponseCodes.OK -> updatedUser)
          }
        }
      }
    }
  }

  /** email -> WrappedFriend */
  def putFriend: Route = put {
    path("user" / "friend") {
      getUserId("putFriend") { userId =>
        extract(_.request.entity.asString) { email =>
          onComplete(getUserService.findUser(email)) {
            case Failure(error) =>
              complete(ResponseCodes.NotFound -> s"Friend not found for email: $email")

            case Success(friendUser) =>
              routeFuture("putFriend[put]", getUserService.putFriend(userId, friendUser.userId)) { friend =>
                complete(ResponseCodes.OK -> friend)
              }
          }
        }
      }
    }
  }

  /** DeleteFriendRequest -> () */
  def deleteFriend: Route = delete {
    path("user" / "friend" / IntNumber) { friendId =>
      getUserId("deleteFriend") { userId =>
        completeFuture("deleteFriend", getUserService.deleteFriend(userId, friendId))
      }
    }
  }

  /** LocalDate -> LocalDate */
  def putDueDate: Route = put {
    path("user" / "duedate") {
      getUserId("putDueDate") { userId =>
        entity(as[LocalDate]) { dueDate =>
          routeFuture("putDueDate", getUserService.putDueDate(userId, dueDate)) { model =>
            complete(model)
          }
        }
      }
    }
  }

  /** () -> () */
  def deleteDueDate: Route = delete {
    path("user" / "duedate") {
      getUserId("deleteDueDate") { userId =>
        completeFuture("deleteDueDate", getUserService.deleteDueDate(userId))
      }
    }
  }

  /** () -> state */
  def getUserState: Route = get {
    path("user" / "state") {
      getUserId("getUserState") { userId =>
        routeFuture("getUserState", getUserService.getUserState(userId))(state => complete(state))
      }
    }
  }

  /** state -> () */
  def putUserState: Route = put {
    path("user" / "state") {
      entity(as[String]) { state =>
        getUserId("putUserState") { userId =>
          completeFuture("putUserState", getUserService.petUserState(userId, state))
        }
      }
    }
  }
}
