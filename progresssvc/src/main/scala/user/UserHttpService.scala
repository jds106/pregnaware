package user

import akka.actor.ActorRefFactory
import com.typesafe.scalalogging.StrictLogging
import spray.http.StatusCodes
import spray.routing.{HttpService, Route}
import user.entities._
import utils.CustomDirectives
import utils.Json4sSupport._

import scala.concurrent.ExecutionContext

object UserHttpService {
  val serviceName = "UserSvc"
}

/** Support user creation */
case class UserHttpService(
  persistence: UserPersistence,
  ar: ActorRefFactory,
  ec: ExecutionContext) extends HttpService with CustomDirectives with StrictLogging {

  implicit def actorRefFactory: ActorRefFactory = ar
  implicit def executionContext : ExecutionContext = ec

  /** The routes defined by this service */
  val routes =
    pathPrefix(UserHttpService.serviceName) {
      getUser ~ findUser ~ putUser ~ editUser ~ putFriend ~ deleteFriend
    }

  def getUser : Route = get {
    path("user" / IntNumber) { userId =>
      completeWithFailure("getUser", persistence.getUser(userId)) {
        case None => complete(StatusCodes.NotFound)
        case Some(u) => complete(u)
      }
    }
  }

  def findUser : Route = get {
    path("findUser" / Segment) { email =>
      completeWithFailure("findUser", persistence.getUser(email)) {
        case None => complete(StatusCodes.NotFound)
        case Some(u) => complete(u)
      }
    }
  }

  def putUser : Route = put {
    path("user") {
      entity(as[AddUserRequest]) { entry =>
        completeWithFailure("putUser[find]", persistence.getUser(entry.email)) {
          case Some(user) => complete(StatusCodes.Conflict -> "User already exists")

          case None =>
            val addUserFut = persistence.addUser(entry.displayName, entry.email, entry.passwordHash)
            completeWithFailure("putUser[Add]", addUserFut) { user =>
              complete(user)
            }
        }
      }
    }
  }

  def editUser : Route = put {
    path("editUser") {
      entity(as[ModifyUserRequest]) { request =>
        val updateUserFut = persistence.updateUser(
          request.userId, request.displayName, request.email, request.passwordHash)

        completeWithFailure("editUser", updateUserFut) {
          case false => complete(StatusCodes.NotFound)
          case true => complete(StatusCodes.OK)
        }
      }
    }
  }

  def putFriend : Route = put {
    path("friend") {
      entity(as[AddFriendRequest]) { request =>
        val addFriendFut = persistence.addFriend(request.userId, request.friendId)
        completeWithFailure("putFriend", addFriendFut)(f => complete(f))
      }
    }
  }

  def deleteFriend : Route = delete {
    path("friend") {
      entity(as[DeleteFriendRequest]) { request =>
        val deleteFriendFut = persistence.deleteFriend(request.userId, request.friendId)
        completeWithFailure("deleteFriend", deleteFriendFut){
          case false => complete(StatusCodes.NotFound)
          case true => complete(StatusCodes.OK)
        }
      }
    }
  }
}
