package naming

import java.io.{File, FileWriter}

import com.typesafe.scalalogging.StrictLogging
import org.json4s.jackson.Serialization.{read, write}
import spray.http.StatusCodes
import spray.routing.{HttpService, Route}
import utils.Json4sSupport._

import scala.io.Source

object NamingHttpService {
  val serviceName = "NamingSvc"
}

/** Support name suggestions and rankings */
trait NamingHttpService extends HttpService with NamePersister with StrictLogging {

  type EntryList = scala.collection.mutable.ListBuffer[NamingEntry]
  type EntryHashMap = scala.collection.mutable.HashMap[Int, EntryList]

  /** The routes defined by this service */
  val routes = pathPrefix(NamingHttpService.serviceName) {
    getNames ~ putName ~ deleteName ~ users
  }

  // Load in previous naming suggestions
  private val names = new EntryHashMap()
  loadNames.foreach { case (userId, entries) =>
      val entryList = new EntryList
      entries.foreach(e => entryList += e)
      names.put(userId, entryList)
  }

  private def getEntries(userId: Int) = {
    names.get(userId) match {
      case Some(entryList) =>
        entryList

      case None =>
        val entryList = new EntryList
        names.put(userId, entryList)
        entryList
    }
  }

  /** Returns the current list of known names */
  def getNames : Route = get {
    path("names") {
      parameter('userId.as[Int]) { userId =>
        complete(StatusCodes.OK -> NamingEntries(userId, names.getOrElse(userId, Seq.empty[NamingEntry])))
      }
    }
  }

  /** Adds a new name to the list */
  def putName : Route = put {
    path("name") {
      entity(as[NamingEntry]) { entry =>
        val currentEntries = getEntries(entry.userId)

        currentEntries += entry
        saveNames(entry.userId, currentEntries)
        complete(StatusCodes.OK)
      }
    }
  }

  /** Bulk-adds names - replaces existing names */
  def putNames : Route = put {
    path("names") {
      entity(as[NamingEntries]) { entries =>

        val currentEntries = getEntries(entries.userId)
        currentEntries.clear()
        entries.entries.foreach(e => currentEntries += e)

        saveNames(entries.userId, currentEntries)
        complete(StatusCodes.OK)
      }
    }
  }

  def deleteName : Route = delete {
    path("name") {
      entity(as[NamingEntry]) { entry =>
        names.get(entry.userId) match {
          case None =>
            logger.error(s"Could not find any entries for user ${entry.userId}")
            complete(StatusCodes.BadRequest)

          case Some(currentEntries) =>
            currentEntries.find(_ == entry).foreach(e => currentEntries -= e)
            saveNames(entry.userId, currentEntries)
            complete(StatusCodes.OK)
        }
      }
    }
  }

  def users : Route = get {
    path("users") {
      complete(names.keySet)
    }
  }
}

trait NamePersister {
  /** Load all of the names into memory */
  def loadNames: Map[Int, Seq[NamingEntry]]

  /** Persist changes */
  def saveNames(userId: Int, entryList: Seq[NamingEntry]) : Unit
}

trait FileNamePersister extends NamePersister {

  /** The file root to store the naming suggestions */
  def root : File

  /** Load all of the names into memory */
  def loadNames: Map[Int, Seq[NamingEntry]] = {
    val userIdRegex = "^[0-9]+\\.json$"
    val userFiles = root.listFiles().filter(_.isFile).filter(f => f.getName.matches(userIdRegex))

    userFiles
      .map{ f => (f.getName.replace(".json", "").toInt, Source.fromFile(f).mkString) }
      .map{ case (userId, s) => (userId, read[NamingEntries](s)) }
      .map{ case (userId, nm) => userId -> nm.entries }
      .toMap
  }

  /** Persist changes */
  def saveNames(userId: Int, entryList: Seq[NamingEntry]) : Unit = {
    val entries = NamingEntries(userId, entryList)

    val file = new File(root, s"$userId.json")
    val writer = new FileWriter(file)
    try {
      writer.write(write(entries))
    } finally {
      writer.close()
    }
  }
}