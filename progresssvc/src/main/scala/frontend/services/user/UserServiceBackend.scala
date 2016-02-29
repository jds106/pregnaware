package frontend.services.user

import akka.actor.ActorContext
import akka.util.Timeout
import frontend.services.BackEndFuncs
import spray.http.HttpMethods._
import spray.httpx.ResponseTransformation._
import user.entities._
import utils.ExecutionWrapper
import utils.Json4sSupport._

import scala.concurrent.{ExecutionContext, Future}

/** Client to the UserHttpService */
abstract class UserServiceBackend(userServiceName: String) extends BackEndFuncs(userServiceName) {

  def findUser(email: String) : Future[WrappedUser] = {
    send(GET, s"/user/$email").map(r => r ~> unmarshal[WrappedUser])
  }

  def getUser(userId: Int) : Future[WrappedUser] = {
    send(GET, s"/user/$userId").map(r => r ~> unmarshal[WrappedUser])
  }

  def postUser(
    displayName: String, email: String, passwordHash: String) : Future[WrappedUser] = {

    val addUserRequest = AddUserRequest(displayName, email, passwordHash)
    send(POST, "user", (b,u) => b(u, addUserRequest)).map(r => r ~> unmarshal[WrappedUser])
  }

  def putUser(
    userId: Int, displayName: String, email: String, passwordHash: String) : Future[Unit] = {

    val editUserRequest = EditUserRequest(displayName, email, passwordHash)
    send(PUT, s"user/$userId", (b,u) => b(u, editUserRequest)).map(_ => ())
  }

  def putFriend(userId: Int, friendId: Int) : Future[WrappedFriend] = {
    val addFriendRequest = AddFriendRequest(friendId)
    send(PUT, s"user/$userId/friend", (b,u) => b(u, addFriendRequest)).map(r => r ~> unmarshal[WrappedFriend])
  }

  def deleteFriend(userId: Int, friendId: Int) : Future[Unit] = {
    send(DELETE, s"user/$userId/friend/$friendId").map(_ => ())
  }
}

object UserServiceBackend {
  def apply(userServiceName: String)
      (implicit ac: ActorContext, ec: ExecutionContext, to: Timeout) : UserServiceBackend= {

    new UserServiceBackend(userServiceName) {
      implicit override final def context: ActorContext = ac
      implicit override final def executor: ExecutionContext = ec
      implicit override final def timeout: Timeout = to
    }
  }
}