package user

import user.entities.{WrappedFriend, WrappedUser}

import scala.concurrent.Future

/** User persistence abstraction */
trait UserPersistence {
  /** Add a new user */
  def addUser(displayName: String, email: String, passwordHash: String): Future[WrappedUser]

  /** Modify an existing user */
  def updateUser(userId: Int, displayName: String, email: String, passwordHash: String): Future[Boolean]

  /** Remove an existing user */
  def deleteUser(userId : Int): Future[Boolean]

  /** Get a user (plus friends) by e-mail */
  def getUser(email: String): Future[Option[WrappedUser]]

  /** Get a user (plus friends) by user id */
  def getUser(userId: Int): Future[Option[WrappedUser]]

  /** Makes a new friend connection between the user and the friend */
  def addFriend(userId: Int, friendId: Int) : Future[WrappedFriend]

  /** Delete a friend linkage */
  def deleteFriend(userId: Int, friendId: Int) : Future[Boolean]
}
