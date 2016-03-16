package pregnaware.database.wrappers

import com.typesafe.scalalogging.StrictLogging
import pregnaware.database.ConnectionManager._
import pregnaware.database.schema.Tables._
import pregnaware.naming.entities.WrappedBabyName
import pregnaware.user.entities.WrappedFriend
import pregnaware.utils.ExecutionWrapper
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future

/** Functions used by all of the user wrappers */
trait CommonWrapper extends ExecutionWrapper with StrictLogging {

  /** Gets the friend row for the user <-> friend relationship (includes blocked & unconfirmed rows) */
  protected def getFriendRow(userId: Int, friendId: Int): Future[Option[FriendRow]] = {
    connection("GetFriendRow") { db =>
      // Friends are bi-directional, so compare the friend id to user1 and user2
      val existingFriendQuery = Friend.filter { f =>
        (f.senderid === userId && f.receiverid === friendId) || (f.receiverid === userId && f.senderid === friendId)
      }

      // Locate or create a new FriendRow
      db.run(existingFriendQuery.result.headOption)
    }
  }

  /** For the userId, builds a single wrapped friend for the specified friend id  */
  protected def getWrappedFriend(userId: Int, friendId: Int): Future[WrappedFriend] = {
    connection("GetWrappedFriends") { db =>
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
  protected def getWrappedFriends(userId: Int): Future[Seq[WrappedFriend]] = {
    connection("WrappedFriends") { db =>
      val friendsQuery = (Friend join User)
        .on((f, u) => u.id === f.senderid || u.id === f.receiverid)
        .filter { case (f, u) => u.id === userId }
        .filter { case (f, u) => !f.isblocked }
        .filter { case (f, u) => f.isconfirmed }
        .map { case (f, _) => f }

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
          .filter { case (b, u) => b.userid inSet friendIds }

        val babyNamesQueryFut = db.run(babyNamesQuery.result)
        val friendUsersQueryFut = db.run(User.filter(row => row.id inSet friendIds).result)

        for {
          babyNames <- babyNamesQueryFut
          friendUsers <- friendUsersQueryFut
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

  /** Returns the list of baby names for the specified user */
  protected def getWrappedBabyNames(userId: Int): Future[Seq[WrappedBabyName]] = {
    connection("WrappedBabyNames") { db =>
      val joinQuery = (Babyname join User).on(_.suggestedby === _.id).filter { case (b, u) => b.userid === userId }

      db.run(joinQuery.result).map { results =>
        results.map {
          case (b, u) =>
            WrappedBabyName(b.id, b.userid, b.suggestedby, u.displayname, b.date.toLocalDate, b.name, b.isboy)
        }
      }
    }
  }
}
