package pregnaware.database.wrappers

import java.sql.Date
import java.time.{Instant, LocalDate}

import pregnaware.database.ConnectionManager._
import pregnaware.database.schema.Tables._
import pregnaware.naming.entities.WrappedBabyName
import pregnaware.user.UserPersistence
import pregnaware.user.entities.{WrappedFriend, WrappedUser}
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future

/** The UserWrapper guarantees to implement everything in UserPersistence - bringing in
  * the required traits to honour this guarantee.
  */
trait UserWrapper extends UserPersistence with CommonWrapper {

  /** Get a user (plus friends) by e-mail */
  def getUser(email: String): Future[Option[WrappedUser]] = {
    connection { db =>
      db.run(User.filter(_.email === email).result.headOption).flatMap {
        case None => Future.successful(None)
        case Some(user) => getWrappedUser(user)
      }
    }
  }

  /** Get a user (plus friends) by user id */
  def getUser(userId: Int): Future[Option[WrappedUser]] = {
    connection { db =>
      db.run(User.filter(_.id === userId).result.headOption).flatMap {
        case None => Future.successful(None)
        case Some(user) => getWrappedUser(user)
      }
    }
  }

  /** Add a new user */
  def addUser(displayName: String, email: String, passwordHash: String): Future[WrappedUser] = {
    connection { db =>
      val joinedDate = LocalDate.now
      val insertQuery = User returning User.map(_.id) into ((user, id) => user.copy(id = id))
      val action = insertQuery += UserRow(-1, displayName, email, passwordHash, None, Date.valueOf(joinedDate))
      db.run(action).map { user =>
        WrappedUser(
          user.id,
          user.displayname,
          user.email,
          None,
          joinedDate,
          Instant.now.toEpochMilli,
          Seq.empty[WrappedBabyName],
          user.passwordhash,
          Seq.empty[WrappedFriend],
          Seq.empty[WrappedFriend],
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

  def getUserState(userId: Int) : Future[String] = {
    connection { db =>
      db.run(Userstate.filter(_.userid === userId).map(_.state).result.headOption).map {
        case None => "{ }"
        case Some(data) => data
      }
    }
  }

  /** Stores user data (treated as an un-processed blob of JSON */
  def setUserState(userId: Int, data: String) : Future[Unit] = {
    connection { db =>
      db.run(Userstate.filter(_.userid === userId).map(_.state).result.headOption).flatMap {
        case Some(_) =>
          db.run(Userstate.filter(_.userid === userId).map(_.state).update(data)).map(_ => ())

        case None =>
          db.run(Userstate += UserstateRow(userId, data)).map(_ => ())
      }
    }
  }

  /** Gets the user for the specified user row (used by the getUser methods) */
  private def getWrappedUser(user: UserRow): Future[Option[WrappedUser]] = {
    connection { db =>
      val friendsQuery = getWrappedFriends(user.id)
      val babyNamesQuery = getWrappedBabyNames(user.id)
      val friendRequestsSentFut = getFriendRequestsSent(user.id)
      val friendRequestsReceivedFut = getFriendRequestsReceived(user.id)
      val sessionFut = db.run(Session.filter(_.userid === user.id).map(_.accesstime).result.headOption)

      for {
        babyNames <- babyNamesQuery
        friends <- friendsQuery
        friendRequestsSent <- friendRequestsSentFut
        friendRequestsReceived <- friendRequestsReceivedFut
        session <- sessionFut
      } yield {
        val dueDate = user.duedate.map(_.toLocalDate)
        val lastAccessTime = session.getOrElse(Instant.now.toEpochMilli)

        Some(WrappedUser(
          user.id, user.displayname, user.email, dueDate, user.joindate.toLocalDate,
          lastAccessTime, babyNames, user.passwordhash, friends, friendRequestsSent, friendRequestsReceived))
      }
    }
  }

  /** Returns the list of WrappedFriendsToBe that the user has sent requests to */
  private def getFriendRequestsSent(userId: Int): Future[Seq[WrappedFriend]] = {
    getFriendRequests(userId) { row =>
      if (row.senderid == userId) {
        Some(row.receiverid -> row.date.toLocalDate)
      } else {
        None
      }
    }
  }

  /** Returns the list of WrappedFriendsToBe that the user has received requests from */
  private def getFriendRequestsReceived(userId: Int): Future[Seq[WrappedFriend]] = {
    getFriendRequests(userId) { row =>
      if (row.receiverid == userId) {
        Some(row.senderid -> row.date.toLocalDate)
      } else {
        None
      }
    }
  }

  /** Returns unconfirmed friend requests filtered according to whether they have been sent / received */
  private def getFriendRequests(userId: Int)(filter: FriendRow => Option[(Int, LocalDate)])
    : Future[Seq[WrappedFriend]] = {

    connection { db =>
      // The list of non-blocked unconfirmed friends
      val friendsQuery = (Friend join User)
        .on((f, u) => u.id === f.senderid || u.id === f.receiverid)
        .filter { case (f, u) => u.id === userId }
        .filter { case (f, u) => !f.isblocked }
        .filter { case (f, u) => !f.isconfirmed }
        .map { case (f, _) => f }

      db.run(friendsQuery.result) flatMap { friendRows =>
        val friendDatesById = friendRows.flatMap(f => filter(f)).toMap
        val friendIds = friendDatesById.map { case (id, date) => id }
        val friendUsersQuery = User.filter(row => row.id inSet friendIds)

        db.run(friendUsersQuery.result).map { friendsToBe =>
          friendsToBe.map(f => WrappedFriend(
            f.id, f.displayname, f.email, None, Seq.empty[WrappedBabyName], friendDatesById(f.id)))
        }
      }
    }
  }
}
