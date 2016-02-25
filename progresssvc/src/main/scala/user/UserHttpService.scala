package user

import java.io.{File, FileWriter}

import com.typesafe.scalalogging.StrictLogging
import org.json4s.jackson.Serialization.{read, write}
import spray.http.StatusCodes
import spray.routing.{HttpService, Route}
import utils.Json4sSupport._

import scala.io.Source

object UserHttpService {
  val serviceName = "UserSvc"
}

/** Support user creation */
trait UserHttpService extends HttpService with UserPersister with StrictLogging {

  private val userMap = new scala.collection.mutable.HashMap[Int, UserEntry]()
  loadUsers.foreach(u => userMap.put(u.userId, u))

  /** The routes defined by this service */
  val routes =
    pathPrefix(UserHttpService.serviceName) {
      getUser ~ findUser ~ putUser ~ editUser ~ putFriend ~ deleteFriend
    }

  def getUser : Route = get {
    path("user" / IntNumber) { userId =>
      userMap.get(userId) match {
        case None => complete(StatusCodes.NotFound)
        case Some(u) => complete(u)
      }
    }
  }

  def findUser : Route = get {
    path("findUser" / Segment) { email =>
      userMap.values.find(_.email.equalsIgnoreCase(email)) match {
        case None =>
          complete(StatusCodes.NotFound)
        case Some(user) =>
          complete(user)
      }
    }
  }

  def putUser : Route = put {
    path("user") {
      entity(as[UserEntry]) { entry =>

        userMap.values.find(_.email.equalsIgnoreCase(entry.email)) match {
          case Some(user) =>
            complete(StatusCodes.Conflict -> "User already exists")

          case None =>
            val persistedEntry = entry.copy(userId = nextId(u => u.userId, userMap.values.toSeq))
            userMap.put(persistedEntry.userId, persistedEntry)
            saveUser(persistedEntry)
            complete(persistedEntry)
        }
      }
    }
  }

  def editUser : Route = put {
    path("editUser") {
      entity(as[ModifiedUser]) { modifiedUser =>
      userMap.get(modifiedUser.userId) match {
          case None =>
            complete(StatusCodes.NotFound)

          case Some(u) =>
            assert(
              modifiedUser.userId == u.userId,
              s"Cannot change the user id of the modified user: $modifiedUser.userId != ${u.userId}")

            val newUser = UserEntry(
              u.userId, modifiedUser.displayName, modifiedUser.email, modifiedUser.passwordHash, u.friends)

            userMap.put(u.userId, newUser)
            saveUser(newUser)
            complete(newUser)
        }
      }
    }
  }

  def putFriend : Route = put {
    path("friend") {
      entity(as[LinkUsers]) { userLink =>
        userMap.get(userLink.userId) match {
          case None =>
            complete(StatusCodes.NotFound -> s"Could not find user for id ${userLink.userId}")

          case Some(user) =>
            val userWithFriend = user.copy(friends = userLink.friendUserId +: user.friends)
            userMap.put(userWithFriend.userId, userWithFriend)
            saveUser(userWithFriend)
            complete(StatusCodes.OK)
        }
      }
    }
  }

  def deleteFriend : Route = delete {
    path("friend") {
      entity(as[LinkUsers]) { userLink =>
        userMap.get(userLink.userId) match {
          case None =>
            complete(StatusCodes.NotFound -> s"Could not find user for id ${userLink.userId}")

          case Some(user) =>
            val userWithFriend = user.copy(friends = user.friends.filter(_ != userLink.friendUserId))
            userMap.put(userWithFriend.userId, userWithFriend)
            saveUser(userWithFriend)
            complete(StatusCodes.OK)
        }
      }
    }
  }

  private def nextId(selector: UserEntry => Int, users: Iterable[UserEntry]) = {
    users match {
      case Nil => 1
      case list => list.map(selector).max + 1
    }
  }
}

trait UserPersister {
  /** Load all of the names into memory */
  def loadUsers: Seq[UserEntry]

  /** Persist changes */
  def saveUser(entry: UserEntry) : Unit
}

trait FileUserPersister extends UserPersister {

  /** The file root to store the naming suggestions */
  def root : File

  /** Load all of the names into memory */
  def loadUsers: Seq[UserEntry] = {
    val userIdRegex = "^[0-9]+\\.json$"
    val userFiles = root.listFiles().filter(_.isFile).filter(f => f.getName.matches(userIdRegex))

    userFiles
      .map{ f => (f.getName.replace(".json", "").toInt, Source.fromFile(f).mkString) }
      .map{ case (userId, s) => read[UserEntry](s) }
  }

  /** Persist changes */
  def saveUser(entry: UserEntry) : Unit = {
    val file = new File(root, s"${entry.userId}.json")
    val writer = new FileWriter(file)
    try {
      writer.write(write(entry))
    } finally {
      writer.close()
    }
  }
}