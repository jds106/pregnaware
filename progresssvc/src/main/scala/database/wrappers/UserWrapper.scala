package database.wrappers

import database.ConnectionManager._
import database.entities.{WrappedFriend, WrappedUser}
import database.schema.Tables._
import slick.driver.MySQLDriver.api._

import scala.concurrent.{ExecutionContext, Future}

/** User-related functions */
trait UserWrapper {

  implicit def executionContext: ExecutionContext

  def addUser(displayName: String, email: String, passwordHash: String): Future[UserRow] = {
    connection { db =>
      val insertQuery = User returning User.map(_.id) into ((user, id) => user.copy(id = id))
      val action = insertQuery += UserRow(-1, displayName, email, passwordHash)
      db.run(action)
    }
  }

  def deleteUser(userId : Int): Future[Boolean] = {
    connection { db =>
      val deletion = User.filter(_.id === userId)
      db.run(deletion.delete).map {
        case 0 => false
        case 1 => true
        case _ => throw new Exception(s"Deleted more than one user with id $userId")
      }
    }
  }

  /** Get a user by e-mail */
  def getUser(email: String): Future[Option[UserRow]] = {
    connection { db =>
      val query = User.filter(_.email === email)
      db.run(query.result.headOption)
    }
  }

  /** Get a user by user id */
  def getUser(userId: Int): Future[Option[UserRow]] = {
    connection { db =>
      val query = User.filter(_.id === userId)
      db.run(query.result.headOption)
    }
  }

  /** Get a user (plus friends) by e-mail */
  def getWrappedUser(email: String): Future[Option[WrappedUser]] = {
    getUser(email).flatMap(getWrappedUser)
  }

  /** Get a user (plus friends) by user id */
  def getWrappedUser(userId: Int): Future[Option[WrappedUser]] = {
    getUser(userId).flatMap(getWrappedUser)
  }

  /** Makes a new friend connection between the user and the friend */
  def addFriend(userId: Int, friendId: Int) : Future[FriendRow] = {
    connection { db =>
      val existingFriendQuery = Friend.filter { f =>
        (f.userid1 === userId && f.userid2 === friendId) || (f.userid2 === userId && f.userid1 === friendId)
      }

      db.run(existingFriendQuery.result.headOption).flatMap {
        case Some(row) =>
          Future.successful(row)

        case None =>
          val insertQuery = Friend returning Friend.map(_.id) into ((friend, id) => friend.copy(id = id))
          val action = insertQuery += FriendRow(-1, userId, friendId)
          db.run(action)
      }
    }
  }

  def deleteFriend(userId: Int, friendId: Int) : Future[Boolean] = {
    connection { db =>
      val existingFriendQuery = Friend.filter { f =>
        (f.userid1 === userId && f.userid2 === friendId) || (f.userid2 === userId && f.userid1 === friendId)
      }

      db.run(existingFriendQuery.delete).map {
        case 0 => false
        case 1 => true
        case _ => throw new Exception(s"Deleted more than one friend link. UserId: $userId, FriendId: $friendId")
      }
    }
  }

  private def getWrappedUser(userOpt: Option[UserRow]): Future[Option[WrappedUser]] = {
    connection { db =>
      userOpt match {
        case None =>
          Future.successful(None)

        case Some(user) =>
          val friendsQuery = (Friend join User)
            .on((f, u) => u.id === f.userid1 || u.id == f.userid2)
            .filter { case (f, u) => u.id === user.id }
            .map { case (_, u) => u }

          db.run(friendsQuery.result).map { friends =>
            val wrappedFriends = friends.map(f => WrappedFriend(f.id, f.displayname, f.email))
            val wrappedUser = WrappedUser(user.id, user.displayname, user.email, wrappedFriends)
            Some(wrappedUser)
          }
      }
    }
  }
}
