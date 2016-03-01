package pregnaware.database.wrappers

import java.sql.Date
import java.time.{Instant, LocalDate}

import pregnaware.database.ConnectionManager._
import pregnaware.database.schema.Tables._
import pregnaware.database.wrappers.userwrappers.{FriendWrapper, DueDateWrapper}
import pregnaware.naming.entities.WrappedBabyName
import pregnaware.user.UserPersistence
import pregnaware.user.entities.{WrappedFriendToBe, WrappedFriend, WrappedUser}
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future

/** The UserWrapper guarantees to implement everything in UserPersistence - bringing in
  * the required traits to honour this guarantee.
  */
trait UserWrapper extends UserPersistence with DueDateWrapper with FriendWrapper {

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
          Instant.now,
          Seq.empty[WrappedBabyName],
          user.passwordhash,
          Seq.empty[WrappedFriend],
          Seq.empty[WrappedFriendToBe],
          Seq.empty[WrappedFriendToBe])
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
}
