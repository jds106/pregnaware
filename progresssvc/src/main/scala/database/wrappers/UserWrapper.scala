package database.wrappers

import database.ConnectionManager._
import database.schema.Tables._
import slick.driver.MySQLDriver.api._
import user.UserPersistence
import user.entities.{WrappedFriend, WrappedUser}

import scala.concurrent.{ExecutionContext, Future}

/** User-related functions */
trait UserWrapper extends UserPersistence {

  implicit def executionContext: ExecutionContext

  /** Add a new user */
  def addUser(displayName: String, email: String, passwordHash: String): Future[WrappedUser] = {
    connection { db =>
      val insertQuery = User returning User.map(_.id) into ((user, id) => user.copy(id = id))
      val action = insertQuery += UserRow(-1, displayName, email, passwordHash)
      db.run(action).map { user =>
        WrappedUser(user.id, user.displayname, user.email, user.passwordhash, Seq.empty[WrappedFriend])
      }
    }
  }

  /** Modify an existing user */
  def updateUser(userId: Int, displayName: String, email: String, passwordHash: String): Future[Boolean] = {
    connection { db =>
      val query = User.filter(_.id === userId).map(u => (u.displayname, u.email, u.passwordhash))
      val action = query.update((displayName, email, passwordHash))
      db.run(action).map {
        case 0 => false
        case 1 => true
        case n => throw new Exception(s"Modified $n users with $userId")
      }
    }
  }

  /** Remove an existing user */
  def deleteUser(userId : Int): Future[Boolean] = {
    connection { db =>
      val deletion = User.filter(_.id === userId)
      db.run(deletion.delete).map {
        case 0 => false
        case 1 => true
        case n => throw new Exception(s"Deleted $n users with id $userId")
      }
    }
  }

  /** Get a user (plus friends) by e-mail */
  def getUser(email: String): Future[Option[WrappedUser]] = {
    getRawUser(email).flatMap(getWrappedUser)
  }

  /** Get a user (plus friends) by user id */
  def getUser(userId: Int): Future[Option[WrappedUser]] = {
    getRawUser(userId).flatMap(getWrappedUser)
  }

  /** Get a user by e-mail */
  private def getRawUser(email: String): Future[Option[UserRow]] = {
    connection { db =>
      val query = User.filter(_.email === email)
      db.run(query.result.headOption)
    }
  }

  /** Get a user by user id */
  private def getRawUser(userId: Int): Future[Option[UserRow]] = {
    connection { db =>
      val query = User.filter(_.id === userId)
      db.run(query.result.headOption)
    }
  }

  /** Makes a new friend connection between the user and the friend */
  def addFriend(userId: Int, friendId: Int) : Future[WrappedFriend] = {
    connection { db =>

      // Friends are bi-directional, so compare the friend id to user1 and user2
      val existingFriendQuery = Friend.filter { f =>
        (f.userid1 === userId && f.userid2 === friendId) || (f.userid2 === userId && f.userid1 === friendId)
      }

      // Locate or create a new FriendRow
      val friendRowFut = db.run(existingFriendQuery.result.headOption).flatMap {
        case Some(row) =>
          Future.successful(row)

        case None =>
          val insertQuery = Friend returning Friend.map(_.id) into ((friend, id) => friend.copy(id = id))
          val action = insertQuery += FriendRow(-1, userId, friendId)
          db.run(action)
      }

      // Wrap up the FriendRow
      friendRowFut.flatMap { friendRow =>
        getRawUser(friendId).map {
          case None => throw new Exception(s"Cannot find user for friend id $friendId")
          case Some(friendUser) => WrappedFriend(friendRow.id, friendUser.displayname, friendUser.email)
        }
      }
    }
  }

  /** Delete a friend linkage */
  def deleteFriend(userId: Int, friendId: Int) : Future[Boolean] = {
    connection { db =>
      val existingFriendQuery = Friend.filter { f =>
        (f.userid1 === userId && f.userid2 === friendId) || (f.userid2 === userId && f.userid1 === friendId)
      }

      db.run(existingFriendQuery.delete).map {
        case 0 => false
        case 1 => true
        case n => throw new Exception(s"Deleted $n friend links. UserId: $userId, FriendId: $friendId")
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
            val wrappedUser = WrappedUser(user.id, user.displayname, user.email, user.passwordhash, wrappedFriends)
            Some(wrappedUser)
          }
      }
    }
  }
}
