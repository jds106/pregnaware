package pregnaware.database.wrappers.userwrappers

import java.sql.Date
import java.time.LocalDate

import pregnaware.database.ConnectionManager._
import pregnaware.database.schema.Tables._
import pregnaware.user.entities.WrappedFriend
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future

/** Friend-related functions */
trait FriendWrapper extends CommonWrapper {

  /** Makes a new potential friend connection between the user and the friend */
  def addFriend(userId: Int, friendId: Int): Future[WrappedFriend] = {
    if (userId == friendId) {
      throw new Exception(s"User $userId cannot befriend themselves")
    }

    val friendRowFut = getFriendRow(userId, friendId).flatMap {
      case Some(row) =>
        Future.successful(row)

      case None =>
        connection { db =>
          val insertQuery = Friend returning Friend.map(_.id) into ((friend, id) => friend.copy(id = id))
          val row = FriendRow(
            -1, userId, friendId, isconfirmed = false, isblocked = false, Date.valueOf(LocalDate.now))

          db.run(insertQuery += row)
        }
    }

    // Wrap up the FriendRow
    friendRowFut.flatMap { friendRow =>
      if (friendRow.isblocked) {
        // Cannot confirm a blocked friendship
        throw new Exception(s"User $userId cannot befriend $friendId as this friendship has been blocked")

      } else if (friendRow.isconfirmed) {
        // Confirmed friendship - return the wrapped friend
        getWrappedFriend(userId, friendId)

      } else if (userId == friendRow.receiverid) {
        // The user is confirming the friendship sent by their friend
        connection { db =>
          db.run(Friend.filter(_.id === friendRow.id).map(_.isconfirmed).update(true)).flatMap {
            case 1 => getWrappedFriend(userId, friendId)
            case n => throw new Exception(s"Confirmed $n friends for userId $userId and friendId $friendId")
          }
        }
      } else {
        // This a new friendship
        getWrappedFriend(userId, friendId)
      }
    }
  }

  /** The user (userId) is confirming a request made by their friend (friendId) */
  def confirmFriend(userId: Int, friendId: Int): Future[WrappedFriend] = {
    if (userId == friendId) {
      throw new Exception(s"User $userId cannot confirm themselves")
    }

    getFriendRow(userId, friendId).flatMap {
      case None =>
        throw new Exception(s"No friend request found for user $userId and friend $friendId")

      case Some(row) =>
        if (row.isblocked) {
          throw new Exception(s"Cannot confirm a blocked friend request for user $userId and friend $friendId")

        } else if (row.senderid == userId) {
          throw new Exception(s"Friend request sent by user $userId cannot be confirmed by that user")

        } else if (row.isconfirmed) {
          // Previously confirmed - idempotent call
          getWrappedFriend(userId, friendId)

        } else {
          connection { db =>
            val action = Friend.filter(_.id === row.id).map(f => f.isconfirmed).update(true)
            db.run(action).flatMap {
              case 1 => getWrappedFriend(userId, friendId)
              case n => throw new Exception(s"Confirmed friend on $n rows with user $userId and friend $friendId")
            }
          }
        }
    }
  }

  def blockFriend(userId: Int, friendId: Int): Future[Unit] = {
    if (userId == friendId)
      throw new Exception(s"User $userId cannot block themselves")

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
        (f.senderid === userId && f.receiverid === friendId) || (f.senderid === userId && f.receiverid === friendId)
      }

      db.run(existingFriendQuery.delete).map {
        case 1 => ()
        case n => throw new Exception(s"Deleted $n friend links. UserId: $userId, FriendId: $friendId")
      }
    }
  }
}
