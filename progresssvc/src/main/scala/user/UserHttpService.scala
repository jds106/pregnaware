package user

import akka.actor.{ActorContext, ActorRefFactory}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import spray.http.StatusCodes
import spray.routing.{HttpService, Route}
import user.entities._
import utils.CustomDirectives
import utils.Json4sSupport._

import scala.concurrent.ExecutionContext

object UserHttpService {
  val serviceName = "UserSvc"

  def apply(persistence: UserPersistence)
    (implicit ac: ActorContext, ec: ExecutionContext, to: Timeout) : UserHttpService = {

    new UserHttpService(persistence) {

      // Needed for ExecutionWrapper
      implicit override final def context: ActorContext = ac
      implicit override final def executor: ExecutionContext = ec
      implicit override final def timeout: Timeout = to

      // Needed for HttpService
      implicit override final def actorRefFactory: ActorRefFactory = ac
    }
  }
}

/** Support user creation */
abstract class UserHttpService(persistence: UserPersistence)
  extends HttpService with CustomDirectives with StrictLogging {

  /** The routes defined by this service */
  val routes =
    pathPrefix(UserHttpService.serviceName) {
      getUser ~ findUser ~ postUser ~ putUser ~ putFriend ~ deleteFriend
    }

  /** userId -> WrappedUser */
  def getUser: Route = get {
    path("user" / IntNumber) { userId =>
      completeWithFailure("getUser", persistence.getUser(userId)) {
        case None => complete(StatusCodes.NotFound)
        case Some(u) => complete(u)
      }
    }
  }

  /** email -> WrappedUser */
  def findUser: Route = get {
    path("findUser" / Segment) { email =>
      completeWithFailure("findUser", persistence.getUser(email)) {
        case None => complete(StatusCodes.NotFound)
        case Some(u) => complete(u)
      }
    }
  }

  /** AddUserRequest -> WrappedUser */
  def postUser: Route = put {
    path("user") {
      entity(as[AddUserRequest]) { entry =>
        completeWithFailure("postUser[find]", persistence.getUser(entry.email)) {
          case Some(user) => complete(StatusCodes.Conflict -> "User already exists")

          case None =>
            val addUserFut = persistence.addUser(entry.displayName, entry.email, entry.passwordHash)
            completeWithFailure("postUser[Add]", addUserFut) { user =>
              complete(user)
            }
        }
      }
    }
  }

  /** userId / EditUserRequest -> () */
  def putUser: Route = put {
    path("user" / IntNumber) { userId =>
      entity(as[EditUserRequest]) { request =>
        val updateUserFut = persistence.updateUser(
          userId, request.displayName, request.email, request.passwordHash)

        completeWithFailure("putUser", updateUserFut)
      }
    }
  }

  /** userId / AddFriendRequest -> WrappedFriend */
  def putFriend: Route = put {
    path("user" / IntNumber / "friend") { userId =>
      entity(as[AddFriendRequest]) { request =>
        val addFriendFut = persistence.addFriend(userId, request.friendId)
        completeWithFailure("putFriend", addFriendFut)(f => complete(f))
      }
    }
  }

  /** userId / friendId -> () */
  def deleteFriend: Route = delete {
    path("user" / IntNumber / "friend" / IntNumber) { (userId, friendId) =>
      val deleteFriendFut = persistence.deleteFriend(userId, friendId)
      completeWithFailure("deleteFriend", deleteFriendFut)
    }
  }
}
