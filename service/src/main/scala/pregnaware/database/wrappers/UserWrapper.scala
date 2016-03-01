package pregnaware.database.wrappers

import java.sql.Date
import java.time.LocalDate

import pregnaware.database.ConnectionManager._
import pregnaware.database.schema.Tables._
import pregnaware.naming.entities.WrappedBabyName
import pregnaware.user.UserPersistence
import pregnaware.user.entities.{WrappedFriend, WrappedUser}
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future

trait UserWrapper extends UserPersistence {

  /** Add a new user */
  def addUser(displayName: String, email: String, passwordHash: String): Future[WrappedUser] = {
    connection { db =>
      val insertQuery = User returning User.map(_.id) into ((user, id) => user.copy(id = id))
      val action = insertQuery += UserRow(-1, displayName, email, passwordHash)
      db.run(action).map { user =>
        WrappedUser(
          user.id,
          user.displayname,
          user.email,
          None,
          Seq.empty[WrappedBabyName],
          user.passwordhash,
          Seq.empty[WrappedFriend])
      }
    }
  }

  /** Modify an existing user */
  def updateUser(userId: Int, displayName: String, email: String, passwordHash: String): Future[Unit] = {
    connection { db =>
      val query = User.filter(_.id === userId).map(u => (u.displayname, u.email, u.passwordhash))
      val action = query.update((displayName, email, passwordHash))
      db.run(action).map {
        case 1 => ()
        case n => throw new Exception(s"Modified $n users with $userId")
      }
    }
  }

  /** Remove an existing user */
  def deleteUser(userId : Int): Future[Unit] = {
    connection { db =>
      val deletion = User.filter(_.id === userId)
      db.run(deletion.delete).map {
        case 1 => ()
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
        getRawUser(friendId).flatMap {
          case None => throw new Exception(s"Cannot find user for friend id $friendId")
          case Some(friendUser) =>
            getWrappedBabyNames(friendUser.id) map { babyNames =>
              val dueDate = friendUser.duedate.map(_.toLocalDate)
              WrappedFriend(friendUser.id, friendUser.displayname, friendUser.email, dueDate, babyNames)
            }
        }
      }
    }
  }

  /** Delete a friend linkage */
  def deleteFriend(userId: Int, friendId: Int) : Future[Unit] = {
    connection { db =>
      val existingFriendQuery = Friend.filter { f =>
        (f.userid1 === userId && f.userid2 === friendId) || (f.userid2 === userId && f.userid1 === friendId)
      }

      db.run(existingFriendQuery.delete).map {
        case 1 => ()
        case n => throw new Exception(s"Deleted $n friend links. UserId: $userId, FriendId: $friendId")
      }
    }
  }

  /** Sets a due date (either adding a due date, or replacing an existing one) */
  def setDueDate(userId: Int, dueDate: LocalDate) : Future[LocalDate] = {
    connection { db =>
      val query = User.filter(_.id === userId).map(_.duedate)
      val action = query.update(Some(Date.valueOf(dueDate)))
      db.run(action).map {
        case 1 => dueDate
        case n => throw new Exception(s"Set due date on $n users with $userId")
      }
    }
  }

  /** Removes a due date */
  def deleteDueDate(userId: Int) : Future[Unit] = {
    connection { db =>
      val query = User.filter(_.id === userId).map(_.duedate)
      val action = query.update(None)
      db.run(action).map {
        case 1 => ()
        case n => throw new Exception(s"Deleted due date on $n users with $userId")
      }
    }
  }

  private def getWrappedFriends(userId: Int) : Future[Seq[WrappedFriend]] = {
    connection { db =>
      val friendsQuery = (Friend join User)
        .on((f, u) => u.id === f.userid1 || u.id === f.userid2)
        .filter { case (f, u) => u.id === userId }
        .map { case (f, _) =>  f }

      db.run(friendsQuery.result) flatMap { friendRows =>
        val friendIds = friendRows.map(row => if (row.userid1 == userId) row.userid2 else row.userid1)

        val babyNamesQuery =
          (Babyname join User).on(_.suggestedby === _.id).filter{ case (b, u) => b.userid inSet friendIds }

        val friendUsersQuery = User.filter(row => row.id inSet friendIds)

        for {
          babyNames <- db.run(babyNamesQuery.result)
          friendUsers <- db.run(friendUsersQuery.result)
        } yield {

          val babyNamesByUser = babyNames.map {
            case (babyNameRow, userRow) =>
              WrappedBabyName(
                babyNameRow.id, babyNameRow.userid, babyNameRow.suggestedby,
                userRow.displayname, babyNameRow.name, babyNameRow.isboy)
          }.groupBy(_.userId)

          friendUsers.map { friend =>
            val babyNames = babyNamesByUser.getOrElse(friend.id, Seq.empty[WrappedBabyName])
            val dueDate = friend.duedate.map(_.toLocalDate)

            WrappedFriend(friend.id, friend.displayname, friend.email, dueDate, babyNames)
          }
        }
      }
    }
  }

  private def getWrappedBabyNames(userId: Int) : Future[Seq[WrappedBabyName]] = {
    connection { db =>
      val joinQuery = (Babyname join User).on(_.suggestedby === _.id).filter{ case (b, u) => b.userid === userId }

      db.run(joinQuery.result).map { results =>
        results.map {
          case (b, u) => WrappedBabyName(b.id, b.userid, b.suggestedby, u.displayname, b.name, b.isboy)
        }
      }
    }
  }

  private def getWrappedUser(userOpt: Option[UserRow]): Future[Option[WrappedUser]] = {
    connection { db =>
      userOpt match {
        case None =>
          Future.successful(None)

        case Some(user) =>
          val friendsQuery = getWrappedFriends(user.id)
          val babyNamesQuery = getWrappedBabyNames(user.id)

          for {
            babyNames <- babyNamesQuery
            friends <- friendsQuery
          } yield {
            val dueDate = user.duedate.map(_.toLocalDate)
            Some(WrappedUser(user.id, user.displayname, user.email, dueDate, babyNames, user.passwordhash, friends))
          }
      }
    }
  }
}
