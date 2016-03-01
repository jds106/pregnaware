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
  /** Makes a new friend connection between the user and the friend */
  def addFriend(userId: Int, friendId: Int) : Future[WrappedFriend] = {
    connection { db =>

      // Friends are bi-directional, so compare the friend id to user1 and user2
      val existingFriendQuery = Friend.filter { f =>
        (f.user1id === userId && f.user2id === friendId) || (f.user2id === userId && f.user1id === friendId)
      }

      // Locate or create a new FriendRow
      val friendRowFut = db.run(existingFriendQuery.result.headOption).flatMap {
        case Some(row) =>
          Future.successful(row)

        case None =>
          val insertQuery = Friend returning Friend.map(_.id) into ((friend, id) => friend.copy(id = id))
          val row = FriendRow(
            -1, userId, friendId, user1confirmed = true,
            user2confirmed = false, isblocked = false, Date.valueOf(LocalDate.now))

          db.run(insertQuery += row)
      }

      // Wrap up the FriendRow
      friendRowFut.flatMap { friendRow =>
        getRawUser(friendId).flatMap {
          case None => throw new Exception(s"Cannot find user for friend id $friendId")
          case Some(friendUser) =>
            getWrappedBabyNames(friendUser.id).map { babyNames =>
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
        (f.user1id === userId && f.user2id === friendId) || (f.user2id === userId && f.user1id === friendId)
      }

      db.run(existingFriendQuery.delete).map {
        case 1 => ()
        case n => throw new Exception(s"Deleted $n friend links. UserId: $userId, FriendId: $friendId")
      }
    }
  }
}
