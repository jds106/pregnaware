package progress

import java.io.{File, FileWriter}
import java.time.LocalDate
import java.time.temporal.ChronoUnit

import com.wordnik.swagger.annotations._
import org.json4s.jackson.Serialization._
import spray.http.StatusCodes
import spray.routing.{HttpService, Route}
import utils.{HeaderKeys, Json4sSupport}

import scala.collection.mutable
import scala.io.Source

object ProgressHttpService {
  val serviceName = "ProgressSvc"
}

/** Controller for the due date */
@Api(value = "/progress", description = "Pregnancy-progress related end-points", produces = "application/json")
trait ProgressHttpService extends HttpService with ProgressPersister {
  import Json4sSupport._

  // Read in the due dates
  private val dueDateMap = new mutable.HashMap[Int, LocalDate]()
  loadDueDates foreach { case (userId, dueDate) => dueDateMap.put(userId, dueDate) }

  // Human gestation period
  private val gestationPeriod = 280

  /** The routes defined by this service */
  val routes = pathPrefix(ProgressHttpService.serviceName) {
    getProgress
  }

  @ApiOperation(value = "Gets the current progress", nickname = "getPerson", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code=200, message="Progress information", response=classOf[ProgressModel])
  ))
  def getProgress : Route =
    path("progress") {
      get {
        headerValueByName(HeaderKeys.EntryId) { entryId =>
          dueDateMap.get(entryId.toInt) match {
            case None =>
              complete(StatusCodes.NotFound -> s"No due date found for user $entryId")

            case Some(dueDate) =>
              val conceptionDate = dueDate.minusDays (gestationPeriod)
              val passed = ChronoUnit.DAYS.between (conceptionDate, LocalDate.now)
              val remaining = ChronoUnit.DAYS.between (LocalDate.now, dueDate)

              complete (ProgressModel (dueDate, passed, remaining) )
          }
        }
      }
    }
}

trait ProgressPersister {
  /** Load all of the names into memory */
  def loadDueDates: Map[Int, LocalDate]

  /** Persist changes */
  def saveDueDate(userId: Int, dueDate: LocalDate) : Unit
}

trait FileProgressPersister extends ProgressPersister {
  import utils.Json4sSupport._

  /** The file root to store the naming suggestions */
  def root : File

  /** Load all of the names into memory */
  def loadDueDates: Map[Int, LocalDate] = {
    val userIdRegex = "^[0-9]+\\.json$"
    val userFiles = root.listFiles().filter(_.isFile).filter(f => f.getName.matches(userIdRegex))

    userFiles
      .map{ f => (f.getName.replace(".json", "").toInt, Source.fromFile(f).mkString) }
      .map{ case (userId, s) => userId -> read[LocalDate](s) }
      .toMap
  }

  /** Persist changes */
  def saveDueDate(userId: Int, dueDate: LocalDate) : Unit = {
    val file = new File(root, s"$userId.json")
    val writer = new FileWriter(file)
    try {
      writer.write(write(dueDate))
    } finally {
      writer.close()
    }
  }
}