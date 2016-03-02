package pregnaware.user

import java.time.LocalDate

import akka.actor.{ActorContext, ActorRefFactory}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import spray.routing.{HttpService, Route}
import pregnaware.user.entities._
import pregnaware.utils.Json4sSupport._
import pregnaware.utils.{CustomDirectives, ResponseCodes}

import scala.concurrent.ExecutionContext

object UserHttpService {
  val serviceName = "UserSvc"

  def apply(persistence: UserPersistence)
    (implicit ac: ActorContext, ec: ExecutionContext, to: Timeout): UserHttpService = {

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
      getUser ~ findUser ~ postUser ~ putUser ~
        putFriend ~ blockFriend ~ deleteFriend ~
        putDueDate ~ deleteDueDate ~
        getUserState ~ putUserState
    }

  /** userId -> WrappedUser */
  def getUser: Route = get {
    path("user" / IntNumber) { userId =>
      routeFuture("getUser", persistence.getUser(userId)) {
        case None => complete(ResponseCodes.NotFound)
        case Some(u) => complete(u)
      }
    }
  }

  /** email -> WrappedUser */
  def findUser: Route = get {
    path("finduser" / Segment) { email =>
      routeFuture("findUser", persistence.getUser(email)) {
        case None => complete(ResponseCodes.NotFound)
        case Some(u) => complete(u)
      }
    }
  }

  /** AddUserRequest -> WrappedUser */
  def postUser: Route = post {
    path("user") {
      entity(as[AddUserRequest]) { entry =>
        routeFuture("postUser[find]", persistence.getUser(entry.email)) {
          case Some(user) => complete(ResponseCodes.Conflict -> "User already exists")

          case None =>
            val addUserFut = persistence.addUser(entry.displayName, entry.email, entry.passwordHash)
            routeFuture("postUser[Add]", addUserFut) { user =>
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

        completeFuture("putUser", updateUserFut)
      }
    }
  }

  /** userId / friendId -> WrappedFriend (either creates a friend request, or confirms a friendship) */
  def putFriend: Route = put {
    path("user" / IntNumber / "friend" / IntNumber) { (userId, friendId) =>
      val addFriendFut = persistence.addFriend(userId, friendId)
      routeFuture("putFriend", addFriendFut)(f => complete(f))
    }
  }

  /** userId / friendId -> () */
  def blockFriend: Route = put {
    path("user" / IntNumber / "friend" / IntNumber / "block") { (userId, friendId) =>
      val blockFriendFut = persistence.blockFriend(userId, friendId)
      completeFuture("blockFriend", blockFriendFut)
    }
  }

  /** userId / friendId -> () */
  def deleteFriend: Route = delete {
    path("user" / IntNumber / "friend" / IntNumber) { (userId, friendId) =>
      val deleteFriendFut = persistence.deleteFriend(userId, friendId)
      completeFuture("deleteFriend", deleteFriendFut)
    }
  }

  def putDueDate: Route = put {
    path("user" / IntNumber / "duedate") { userId =>
      entity(as[LocalDate]) { dueDate =>
        routeFuture("putDueDate", persistence.setDueDate(userId, dueDate)) { storedDate =>
          complete(storedDate)
        }
      }
    }
  }

  def deleteDueDate: Route = delete {
    path("user" / IntNumber / "duedate") { userId =>
      completeFuture("deleteDueDate", persistence.deleteDueDate(userId))
    }
  }

  def getUserState: Route = get {
    path("user" / IntNumber / "state") { userId =>
      routeFuture("getUserState", persistence.getUserState(userId))(state => complete(state))
    }
  }

  def putUserState: Route = put {
    path("user" / IntNumber / "state") { userId =>
      entity(as[String]) { state =>
        completeFuture("putUserState", persistence.setUserState(userId, state))
      }
    }
  }
}
