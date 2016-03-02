package pregnaware.user

import java.time.LocalDate

import pregnaware.user.entities.{WrappedFriend, WrappedUser}
import pregnaware.utils.ExecutionWrapper

import scala.concurrent.Future

trait UserPersistence extends ExecutionWrapper {
  /** Add a new user */
  def addUser(displayName: String, email: String, passwordHash: String): Future[WrappedUser]

  /** Modify an existing user */
  def updateUser(userId: Int, displayName: String, email: String, passwordHash: String): Future[Unit]

  /** Remove an existing user */
  def deleteUser(userId : Int): Future[Unit]

  /** Get a user (plus friends) by e-mail */
  def getUser(email: String): Future[Option[WrappedUser]]

  /** Get a user (plus friends) by user id */
  def getUser(userId: Int): Future[Option[WrappedUser]]

  /** Makes a new friend connection between the user and the friend (or confirms an existing one) */
  def addFriend(userId: Int, friendId: Int) : Future[WrappedFriend]

  /** Delete a friend linkage */
  def deleteFriend(userId: Int, friendId: Int) : Future[Unit]

  /** Prevents a friendship */
  def blockFriend(userId: Int, friendId: Int) : Future[Unit]

  /** Sets a due date */
  def setDueDate(userId: Int, dueDate: LocalDate) : Future[LocalDate]

  /** Removes a due date */
  def deleteDueDate(userId: Int) : Future[Unit]
}
