package pregnaware.frontend.services.user

import java.time.LocalDate

import pregnaware.frontend.FrontEndDirectives
import pregnaware.frontend.entities.{AddFriendRequest, AddUserRequest, EditUserRequest, LoginRequest}
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
    login ~ getUser ~ postUser ~ putUser ~
      putFriend ~ deleteFriend ~
      putDueDate ~ deleteDueDate

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

  /** EditUserRequest -> () */
  def putUser: Route = put {
    path("user") {
      getUser("putUser[fetch]") { user =>
        entity(as[EditUserRequest]) { request =>

          val displayName = request.displayName.getOrElse(user.displayName)
          val email = request.email.getOrElse(user.displayName)
          val passwordHash = request.password match {
            case None => user.passwordHash
            case Some(password) => BCrypt.hashpw(password, salt)
          }

          val editUserFut = getUserService.putUser(user.userId, displayName, email, passwordHash)
          completeFuture("putUser[edit]", editUserFut)
        }
      }
    }
  }

  /** AddFriendRequest -> () */
  def putFriend: Route = put {
    path("user" / "friend") {
      getUserId("putFriend") { userId =>
        entity(as[AddFriendRequest]) { request =>
          onComplete(getUserService.findUser(request.email)) {
            case Failure(error) =>
              complete(ResponseCodes.NotFound -> s"Friend not found for email: ${request.email}")

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
}