package frontend.services.user

import frontend.FrontEndDirectives
import frontend.entities.{AddFriendRequest, AddUserRequest, EditUserRequest, LoginRequest}
import org.mindrot.jbcrypt.BCrypt
import spray.http.StatusCodes
import spray.routing._
import utils.Json4sSupport._

import scala.util.{Failure, Success}

trait UserServiceFrontEnd extends FrontEndDirectives {

  // The password hashing salt
  private val salt = BCrypt.gensalt()

  // The routes provided by this service
  val userServiceRoutes : Route = login ~ postUser ~ putUser ~ putFriend ~ deleteFriend

  /** LoginRequest -> WrappedUser */
  def login: Route = post {
    path("login") {
      entity(as[LoginRequest]) { request =>
        completeWithFailure("login", getUserService.findUser(request.email)) { user =>
          complete(user)
        }
      }
    }
  }

  /** NewUserRequest -> WrappedUser */
  def postUser: Route = post {
    path("user") {
      entity(as[AddUserRequest]) { request =>
        val passwordHash = BCrypt.hashpw(request.password, salt)
        val newUserFut = getUserService.postUser(request.displayName, request.email, passwordHash)

        completeWithFailure("newUser", newUserFut) { user =>
          complete(user)
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
          completeWithFailure("putUser[edit]", editUserFut)
        }
      }
    }
  }

  /** AddFriendRequest -> () */
  def putFriend : Route = put {
    path("user" / "friend") {
      getUserId("putFriend") { userId =>
        entity(as[AddFriendRequest]) { request =>
          onComplete(getUserService.findUser(request.email)) {
            case Failure(error) =>
              complete(StatusCodes.NotFound -> s"Friend not found for email: ${request.email}")

            case Success(friendUser) =>
              completeWithFailure("putFriend[put]", getUserService.putFriend(userId, friendUser.userId)) { friend =>
                complete(StatusCodes.OK -> friend)
              }
          }
        }
      }
    }
  }

  /** DeleteFriendRequest -> () */
  def deleteFriend : Route = delete {
    path("user" / "friend" / IntNumber) { friendId =>
      getUserId("deleteFriend") { userId =>
        completeWithFailure("deleteFriend", getUserService.deleteFriend(userId, friendId))
      }
    }
  }
}
