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

  val getUsers : Route = get {
    path("users") {
      complete {
        userMap.values
      }
    }
  }

  val getUser : Route = get {
    path("user" / IntNumber) { userId =>
      userMap.get(userId) match {
        case None => complete(StatusCodes.NotFound)
        case Some(u) => complete(u)
      }
    }
  }

  val findUser : Route = get {
    path("findUser" / Segment) { email =>
      userMap.values.find(_.email.equalsIgnoreCase(email)) match {
        case None =>
          complete(StatusCodes.NotFound)
        case Some(user) =>
          complete(user)
      }
    }
  }

  val putUser : Route = put {
    path("user") {
      entity(as[UserEntry]) { entry =>

        userMap.values.find(_.email.equalsIgnoreCase(entry.email)) match {
          case Some(user) =>
            complete(StatusCodes.Conflict -> "User already exists")

          case None =>
            val nextUserId =
              if (userMap.isEmpty) {
                1
              } else {
                userMap.values.map(_.userId).max + 1
              }

            val persistedEntry = entry.copy(userId = nextUserId)
            userMap.put(persistedEntry.userId, persistedEntry)
            saveUser(persistedEntry)
            complete(persistedEntry)
        }
      }
    }
  }

  /** The routes defined by this service */
  val routes =
    pathPrefix(UserHttpService.serviceName) {
      getUsers ~ getUser ~ findUser ~ putUser
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