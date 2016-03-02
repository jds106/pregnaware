package pregnaware.frontend.services.user

import java.time.LocalDate

import akka.actor.ActorContext
import akka.util.Timeout
import pregnaware.frontend.services.BackEndFuncs
import pregnaware.user.entities._
import pregnaware.utils.Json4sSupport._
import spray.http.HttpMethods._
import spray.httpx.ResponseTransformation._

import scala.concurrent.{ExecutionContext, Future}

/** Client to the UserHttpService */
abstract class UserServiceBackend(userServiceName: String) extends BackEndFuncs(userServiceName) {

  def findUser(email: String) : Future[WrappedUser] = {
    send(GET, s"user/$email").map(r => r ~> unmarshal[WrappedUser])
  }

  def getUser(userId: Int) : Future[WrappedUser] = {
    send(GET, s"user/$userId").map(r => r ~> unmarshal[WrappedUser])
  }

  def postUser(displayName: String, email: String, passwordHash: String) : Future[WrappedUser] = {
    val addUserRequest = AddUserRequest(displayName, email, passwordHash)
    send(POST, "user", (b,u) => b(u, addUserRequest)).map(r => r ~> unmarshal[WrappedUser])
  }

  def putUser(userId: Int, displayName: String, email: String, passwordHash: String) : Future[Unit] = {
    val editUserRequest = EditUserRequest(displayName, email, passwordHash)
    send(PUT, s"user/$userId", (b,u) => b(u, editUserRequest)).map(_ => ())
  }

  def putFriend(userId: Int, friendId: Int) : Future[WrappedFriend] = {
    send(PUT, s"user/$userId/friend/$friendId").map(r => r ~> unmarshal[WrappedFriend])
  }

  def blockFriend(userId: Int, friendId: Int) : Future[Unit] = {
    send(PUT, s"user/$userId/friend/$friendId/block").map(_ => ())
  }

  def deleteFriend(userId: Int, friendId: Int) : Future[Unit] = {
    send(DELETE, s"user/$userId/friend/$friendId").map(_ => ())
  }

  def putDueDate(userId: Int, dueDate: LocalDate) : Future[LocalDate] = {
    send(PUT, s"user/$userId/duedate", (b,u) => b(u, dueDate)).map(r => r ~> unmarshal[LocalDate])
  }

  def deleteDueDate(userId: Int) : Future[Unit] = {
    send(DELETE, s"user/$userId/duedate").map(_ => ())
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
