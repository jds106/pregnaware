package pregnaware.database.wrappers.userwrappers

import java.sql.Date
import java.time.LocalDate

import pregnaware.database.ConnectionManager._
import pregnaware.database.schema.Tables._
import pregnaware.user.entities.{WrappedFriend, WrappedFriendToBe}
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future

/** Friend-related functions */
trait FriendWrapper extends CommonWrapper {

  /** Gets the friend row for the user <-> friend relationship */
  private def getFriendRow(userId: Int, friendId: Int): Future[Option[FriendRow]] = {
    connection { db =>
      // Friends are bi-directional, so compare the friend id to user1 and user2
      val existingFriendQuery = Friend.filter { f =>
        (f.user1id === userId && f.user2id === friendId) || (f.user2id === userId && f.user1id === friendId)
      }

      // Locate or create a new FriendRow
      db.run(existingFriendQuery.result.headOption)
    }
  }

  /** Makes a new potential friend connection between the user and the friend */
  def addFriend(userId: Int, friendId: Int): Future[WrappedFriendToBe] = {
    val friendRowFut = getFriendRow(userId, friendId).flatMap {
      case Some(row) =>
        Future.successful(row)

      case None =>
        connection { db =>
          val insertQuery = Friend returning Friend.map(_.id) into ((friend, id) => friend.copy(id = id))
          val row = FriendRow(
            -1, userId, friendId, user1confirmed = true,
            user2confirmed = false, isblocked = false, Date.valueOf(LocalDate.now))

          db.run(insertQuery += row)
        }
    }

    // Wrap up the FriendRow
    friendRowFut.flatMap { friendRow =>
      getRawUser(friendId).flatMap {
        case None => throw new Exception(s"Cannot find user for friend id $friendId")
        case Some(friendUser) =>
          getWrappedBabyNames(friendUser.id).map { babyNames =>
            WrappedFriendToBe(friendUser.id, friendUser.displayname, friendUser.email, friendRow.date.toLocalDate)
          }
      }
    }
  }

  /** The user (userId) is confirming a request made by their friend (friendId) */
  def confirmFriend(userId: Int, friendId: Int): Future[WrappedFriend] = {
    getFriendRow(userId, friendId).flatMap {
      case None =>
        throw new Exception(s"No friend request found for user $userId and friend $friendId")

      case Some(row) =>
        if (row.isblocked)
          throw new Exception(s"Cannot confirm a blocked friend request for user $userId and friend $friendId")

        if (row.user1confirmed && row.user2confirmed) {
          logger.info(s"Ignoring duplicate friend confirmation between $userId and $friendId")
          getWrappedFriend(friendId)

        } else {
          // We know one of user1confirmed == false or user2confirmed == false
          if ((row.user1id == userId && row.user1confirmed) || (row.user2id == userId && row.user2confirmed))
            throw new Exception(s"Friend request sent by user $userId cannot be confirmed by that user")

          connection { db =>
            // We know from the above that the current value of userXconfirmed MUST be false
            val query = if (row.user1id == userId) {
              Friend.filter(_.id === row.id).map(f => f.user1confirmed)
            } else {
              Friend.filter(_.id === row.id).map(f => f.user2confirmed)
            }

            val action = query.update(true)
            db.run(action).flatMap {
              case 1 => getWrappedFriend(friendId)
              case n => throw new Exception(s"Confirmed friend on $n rows with user $userId and friend $friendId")
            }
          }
        }
    }
  }

  def blockFriend(userId: Int, friendId: Int): Future[Unit] = {
    getFriendRow(userId, friendId).flatMap {
      case None =>
        throw new Exception(s"No friend request found for user $userId and friend $friendId")

      case Some(row) =>
        connection { db =>
          val query = Friend.filter(_.id === row.id).map(_.isblocked)
          val action = query.update(true)
          db.run(action).map {
            case 1 => ()
            case n => throw new Exception(s"Blocked friend on $n rows with user $userId and friend $friendId")
          }
        }
    }
  }

  /** Delete a friend linkage */
  def deleteFriend(userId: Int, friendId: Int): Future[Unit] = {
    connection { db =>
      val existingFriendQuery = Friend.filter { f =>
        (f.user1id === userId && f.user2id === friendId) || (f.user2id === userId && f.user1id === friendId)
      }

      db.run(existingFriendQuery.delete).map {
        case 1 => ()
        case n => throw new Exception(s"Deleted $n friend links. UserId: $userId, FriendId: $friendId")
      }
    }
  }
}
