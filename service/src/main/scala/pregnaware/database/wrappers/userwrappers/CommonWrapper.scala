package pregnaware.database.wrappers.userwrappers

import java.time.{Instant, LocalDate}

import com.typesafe.scalalogging.StrictLogging
import pregnaware.database.ConnectionManager._
import pregnaware.database.schema.Tables._
import pregnaware.naming.entities.WrappedBabyName
import pregnaware.user.entities.{WrappedUser, WrappedFriend}
import pregnaware.utils.ExecutionWrapper
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future

/** Functions used by all of the user wrappers */
trait CommonWrapper extends ExecutionWrapper with StrictLogging {

  /** Get a user by e-mail */
  protected def getRawUser(email: String): Future[Option[UserRow]] = {
    connection { db =>
      db.run(User.filter(_.email === email).result.headOption)
    }
  }

  /** Get a user by user id */
  protected def getRawUser(userId: Int): Future[Option[UserRow]] = {
    connection { db =>
      db.run(User.filter(_.id === userId).result.headOption)
    }
  }

  /** Gets the friend row for the user <-> friend relationship (includes blocked & unconfirmed rows) */
  protected def getFriendRow(userId: Int, friendId: Int): Future[Option[FriendRow]] = {
    connection { db =>
      // Friends are bi-directional, so compare the friend id to user1 and user2
      val existingFriendQuery = Friend.filter { f =>
        (f.senderid === userId && f.receiverid === friendId) || (f.receiverid === userId && f.senderid === friendId)
      }

      // Locate or create a new FriendRow
      db.run(existingFriendQuery.result.headOption)
    }
  }

  /** For the userId, builds a single wrapped friend for the specified friend id  */
  protected def getWrappedFriend(userId: Int, friendId: Int) : Future[WrappedFriend] = {
    connection { db =>
      val friendRowFut = getFriendRow(userId, friendId)
      val friendFut = db.run(User.filter(_.id === friendId).result.head)
      val babyNamesFut = getWrappedBabyNames(friendId)

      for {
        friendRowOpt <- friendRowFut
        friend <- friendFut
        babyNames <- babyNamesFut
      } yield {
        if (friendRowOpt.isEmpty) {
          throw new Exception(s"No friendship between $userId <-> $friendId")
        }

        val friendRow = friendRowOpt.get

        if (friendRowOpt.get.isblocked) {
          throw new Exception(s"Friendship is blocked ${friendRow.senderid} <-> ${friendRow.receiverid}")

        } else if (friendRow.isconfirmed) {
          // Full friendship
          val dueDate = friend.duedate.map(_.toLocalDate)
          val friendDate = friendRow.date.toLocalDate
          WrappedFriend(friend.id, friend.displayname, friend.email, dueDate, babyNames, friendDate)

        } else {
          // Restricted friendship
          val friendDate = friendRow.date.toLocalDate
          WrappedFriend(friend.id, friend.displayname, friend.email, None, Seq.empty[WrappedBabyName], friendDate)
        }
      }
    }
  }

  /** Returns non-blocked confirmed friends of the userId */
  protected def getWrappedFriends(userId: Int) : Future[Seq[WrappedFriend]] = {
    connection { db =>
      val friendsQuery = (Friend join User)
        .on((f, u) => u.id === f.senderid || u.id === f.receiverid)
        .filter { case (f, u) => u.id === userId }
        .filter { case (f, u) => !f.isblocked }
        .filter { case (f, u) => f.isconfirmed }
        .map { case (f, _) =>  f }

      db.run(friendsQuery.result) flatMap { friendRows =>

        // The id of the friend, along with the date of the friendship
        val friendDatesById = friendRows.map { row =>
          val id = if (row.senderid == userId) row.receiverid else row.senderid
          id -> row.date.toLocalDate
        }.toMap

        // The friend ids used for filtering
        val friendIds = friendDatesById.map { case (id, date) => id }

        val babyNamesQuery = (Babyname join User)
          .on(_.suggestedby === _.id)
          .filter{ case (b, u) => b.userid inSet friendIds }

        val friendUsersQuery = User.filter(row => row.id inSet friendIds)

        for {
          babyNames <- db.run(babyNamesQuery.result)
          friendUsers <- db.run(friendUsersQuery.result)
        } yield {

          val babyNamesById = babyNames.map {
            case (babyNameRow, userRow) =>
              WrappedBabyName(
                babyNameRow.id, babyNameRow.userid, babyNameRow.suggestedby,
                userRow.displayname, babyNameRow.date.toLocalDate, babyNameRow.name, babyNameRow.isboy)
          }.groupBy(_.userId)

          friendUsers.map { friend =>
            val babyNames = babyNamesById.getOrElse(friend.id, Seq.empty[WrappedBabyName])
            val dueDate = friend.duedate.map(_.toLocalDate)
            val friendDate = friendDatesById(friend.id)

            WrappedFriend(friend.id, friend.displayname, friend.email, dueDate, babyNames, friendDate)
          }
        }
      }
    }
  }

  /** Returns the list of WrappedFriendsToBe that the user has sent requests to */
  protected def getFriendRequestsSent(userId: Int) : Future[Seq[WrappedFriend]] = {
    val requestSentFilter : FriendRow => Option[(Int, LocalDate)] = row => {
      if (!row.isconfirmed && row.senderid == userId) {
        Some(row.receiverid -> row.date.toLocalDate)
      } else {
        None
      }
    }

    getFriendRequests(userId, requestSentFilter)
  }

  /** Returns the list of WrappedFriendsToBe that the user has received requests from */
  protected def getFriendRequestsReceived(userId: Int) : Future[Seq[WrappedFriend]] = {
    val requestReceivedFilter : FriendRow => Option[(Int, LocalDate)] = row => {
      if (!row.isconfirmed && row.receiverid == userId) {
        Some(row.senderid -> row.date.toLocalDate)
      } else {
        None
      }
    }

    getFriendRequests(userId, requestReceivedFilter)
  }

  /** Returns unconfirmed friend requests filtered according to whether they have been sent / received */
  private def getFriendRequests(
    userId: Int, filter: FriendRow => Option[(Int, LocalDate)]) : Future[Seq[WrappedFriend]] = {

    connection { db =>
      // The list of non-blocked unconfirmed friends
      val friendsQuery = (Friend join User)
        .on((f, u) => u.id === f.senderid|| u.id === f.receiverid)
        .filter { case (f, u) => u.id === userId }
        .filter { case (f, u) => !f.isblocked }
        .filter { case (f, u) => !f.isconfirmed }
        .map { case (f, _) =>  f }

      db.run(friendsQuery.result) flatMap { friendRows =>
        val friendDatesById = friendRows.flatMap(f => filter(f)).toMap
        val friendIds = friendDatesById.map { case(id, date) => id }
        val friendUsersQuery = User.filter(row => row.id inSet friendIds)

        db.run(friendUsersQuery.result).map { friendsToBe =>
          friendsToBe.map(f => WrappedFriend(
            f.id, f.displayname, f.email, None, Seq.empty[WrappedBabyName], friendDatesById(f.id)))
        }
      }
    }
  }

  protected def getWrappedBabyNames(userId: Int) : Future[Seq[WrappedBabyName]] = {
    connection { db =>
      val joinQuery = (Babyname join User).on(_.suggestedby === _.id).filter{ case (b, u) => b.userid === userId }

      db.run(joinQuery.result).map { results =>
        results.map {
          case (b, u) =>
            WrappedBabyName(b.id, b.userid, b.suggestedby, u.displayname, b.date.toLocalDate, b.name, b.isboy)
        }
      }
    }
  }

  protected def getWrappedUser(userOpt: Option[UserRow]): Future[Option[WrappedUser]] = {
    connection { db =>
      userOpt match {
        case None =>
          Future.successful(None)

        case Some(user) =>
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
            val lastAccessTime = session match { case None => Instant.now case Some(t) => Instant.ofEpochMilli(t) }

            Some(WrappedUser(
              user.id, user.displayname, user.email, dueDate, user.joindate.toLocalDate,
              lastAccessTime, babyNames, user.passwordhash, friends, friendRequestsSent, friendRequestsReceived))
          }
      }
    }
  }
}
