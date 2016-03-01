package pregnaware.database.wrappers.userwrappers

import pregnaware.database.ConnectionManager._
import pregnaware.database.schema.Tables._
import pregnaware.naming.entities.WrappedBabyName
import pregnaware.user.entities.{WrappedFriendToBe, WrappedUser, WrappedFriend}
import pregnaware.utils.ExecutionWrapper
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future

/** Functions used by all of the user wrappers */
trait CommonWrapper extends ExecutionWrapper {

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
        val friendIds = friendRows.map(row => if (row.user1id == userId) row.user2id else row.user1id)

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

  /** Returns the list of WrappedFriendsToBe that the user has sent requests to */
  protected def getFriendRequestsSent(userId: Int) : Future[Seq[WrappedFriendToBe]] = {
    val requestSentFilter : FriendRow => Option[Int] = row => {
      if (row.user1id == userId && row.user1confirmed) {
        // This user requested a friendship with user 2
        Some(row.user2id)
      } else if (row.user2id == userId && row.user2confirmed) {
        // The user requested a friendship with user 1
        Some(row.user1id)
      } else {
        None
      }
    }

    getFriendRequests(userId, requestSentFilter)
  }

  /** Returns the list of WrappedFriendsToBe that the user has received requests from */
  protected def getFriendRequestsReceived(userId: Int) : Future[Seq[WrappedFriendToBe]] = {
    val requestReceivedFilter : FriendRow => Option[Int] = row => {
      if (row.user1id == userId && !row.user1confirmed) {
        // This user received a friendship request from user 2
        Some(row.user2id)
      } else if (row.user2id == userId && !row.user2confirmed) {
        // This user received a friendship request from user 1
        Some(row.user1id)
      } else {
        None
      }
    }

    getFriendRequests(userId, requestReceivedFilter)
  }

  /** Returns unconfirmed friend requests filtered according to whether they have been sent / received */
  private def getFriendRequests(userId: Int, filter: FriendRow => Option[Int]) : Future[Seq[WrappedFriendToBe]] = {
    connection { db =>
      // The list of non-blocked friends where one of the users has not yet confirmed friendship
      val friendsQuery = (Friend join User)
        .on((f, u) => u.id === f.user1id || u.id === f.user2id)
        .filter { case (f, u) => u.id === userId }
        .filter { case (f, u) => !f.isblocked }
        .filter { case (f, u) => !f.user1confirmed || !f.user2confirmed }
        .map { case (f, _) =>  f }

      db.run(friendsQuery.result) flatMap { friendRows =>
        val friendIds = friendRows.flatMap(f => filter(f))
        val friendUsersQuery = User.filter(row => row.id inSet friendIds)

        db.run(friendUsersQuery.result).map { friendsToBe =>
          friendsToBe.map(f => WrappedFriendToBe(f.id, f.displayname, f.email))
        }
      }
    }
  }

  protected def getWrappedBabyNames(userId: Int) : Future[Seq[WrappedBabyName]] = {
    connection { db =>
      val joinQuery = (Babyname join User).on(_.suggestedby === _.id).filter{ case (b, u) => b.userid === userId }

      db.run(joinQuery.result).map { results =>
        results.map {
          case (b, u) => WrappedBabyName(b.id, b.userid, b.suggestedby, u.displayname, b.name, b.isboy)
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

          for {
            babyNames <- babyNamesQuery
            friends <- friendsQuery
            friendRequestsSent <- friendRequestsSentFut
            friendRequestsReceived <- friendRequestsReceivedFut
          } yield {
            val dueDate = user.duedate.map(_.toLocalDate)

            Some(WrappedUser(
              user.id, user.displayname, user.email, dueDate,
              user.joindate.toLocalDate, babyNames, user.passwordhash,
              friends, friendRequestsSent, friendRequestsReceived))
          }
      }
    }
  }
}
