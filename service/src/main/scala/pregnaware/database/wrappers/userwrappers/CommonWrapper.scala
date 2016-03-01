package pregnaware.database.wrappers.userwrappers

import java.time.{Instant, LocalDate}

import com.typesafe.scalalogging.StrictLogging
import pregnaware.database.ConnectionManager._
import pregnaware.database.schema.Tables._
import pregnaware.naming.entities.WrappedBabyName
import pregnaware.user.entities.{WrappedFriendToBe, WrappedUser, WrappedFriend}
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

  /** Builds a single wrapped friend for the specified friend id */
  protected def getWrappedFriend(friendId: Int) : Future[WrappedFriend] = {
    connection { db =>

      val friendRowQuery = (Friend join User)
        .on((f, u) => u.id === f.user1id || u.id === f.user2id)
        .filter { case (f, u) => u.id === friendId }
        .map { case (f , _) => f }

      val friendRowFut = db.run(friendRowQuery.result.head)
      val friendFut = db.run(User.filter(_.id === friendId).result.head)
      val babyNamesFut = getWrappedBabyNames(friendId)

      for {
        friendRow <- friendRowFut
        friend <- friendFut
        babyNames <- babyNamesFut
      } yield {
        val dueDate = friend.duedate.map(_.toLocalDate)
        val friendDate = friendRow.date.toLocalDate
        WrappedFriend(friend.id, friend.displayname, friend.email, dueDate, babyNames, friendDate)
      }
    }
  }

  /** Returns friends of the userId where the blocked flag is false and both friends have confirmed each other */
  protected def getWrappedFriends(userId: Int) : Future[Seq[WrappedFriend]] = {
    connection { db =>
      val friendsQuery = (Friend join User)
        .on((f, u) => u.id === f.user1id || u.id === f.user2id)
        .filter { case (f, u) => u.id === userId }
        .filter { case (f, u) => !f.isblocked }
        .filter { case (f, u) => f.user1confirmed && f.user2confirmed }
        .map { case (f, _) =>  f }

      db.run(friendsQuery.result) flatMap { friendRows =>

        // The id of the friend, along with the date of the friendship
        val friendDatesById = friendRows.map { row =>
          val id = if (row.user1id == userId) row.user2id else row.user1id
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
  protected def getFriendRequestsSent(userId: Int) : Future[Seq[WrappedFriendToBe]] = {
    val requestSentFilter : FriendRow => Option[(Int, LocalDate)] = row => {
      if (row.user1id == userId && row.user1confirmed) {
        // This user requested a friendship with user 2
        Some((row.user2id, row.date.toLocalDate))
      } else if (row.user2id == userId && row.user2confirmed) {
        // The user requested a friendship with user 1
        Some((row.user1id, row.date.toLocalDate))
      } else {
        None
      }
    }

    getFriendRequests(userId, requestSentFilter)
  }

  /** Returns the list of WrappedFriendsToBe that the user has received requests from */
  protected def getFriendRequestsReceived(userId: Int) : Future[Seq[WrappedFriendToBe]] = {
    val requestReceivedFilter : FriendRow => Option[(Int, LocalDate)] = row => {
      if (row.user1id == userId && !row.user1confirmed) {
        // This user received a friendship request from user 2
        Some((row.user2id, row.date.toLocalDate))
      } else if (row.user2id == userId && !row.user2confirmed) {
        // This user received a friendship request from user 1
        Some((row.user1id, row.date.toLocalDate))
      } else {
        None
      }
    }

    getFriendRequests(userId, requestReceivedFilter)
  }

  /** Returns unconfirmed friend requests filtered according to whether they have been sent / received */
  private def getFriendRequests(
    userId: Int, filter: FriendRow => Option[(Int, LocalDate)]) : Future[Seq[WrappedFriendToBe]] = {

    connection { db =>
      // The list of non-blocked friends where one of the users has not yet confirmed friendship
      val friendsQuery = (Friend join User)
        .on((f, u) => u.id === f.user1id || u.id === f.user2id)
        .filter { case (f, u) => u.id === userId }
        .filter { case (f, u) => !f.isblocked }
        .filter { case (f, u) => !f.user1confirmed || !f.user2confirmed }
        .map { case (f, _) =>  f }

      db.run(friendsQuery.result) flatMap { friendRows =>
        val friendDatesById = friendRows.flatMap(f => filter(f)).toMap
        val friendIds = friendDatesById.map { case(id, date) => id }
        val friendUsersQuery = User.filter(row => row.id inSet friendIds)

        db.run(friendUsersQuery.result).map { friendsToBe =>
          friendsToBe.map(f => WrappedFriendToBe(f.id, f.displayname, f.email, friendDatesById(f.id)))
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
