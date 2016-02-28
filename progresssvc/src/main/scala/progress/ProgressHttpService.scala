package progress

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import akka.actor.ActorRefFactory
import com.typesafe.scalalogging.StrictLogging
import com.wordnik.swagger.annotations._
import spray.http.StatusCodes
import spray.routing.{HttpService, Route}
import utils.CustomDirectives
import utils.Json4sSupport._

import scala.concurrent.ExecutionContext

object ProgressHttpService {
  val serviceName = "ProgressSvc"
}

/** Controller for the due date */
@Api(value = "/progress", description = "Pregnancy-progress related end-points", produces = "application/json")
case class ProgressHttpService(
  persistence: ProgressPersistence,
  ar: ActorRefFactory,
  ec: ExecutionContext) extends HttpService with CustomDirectives with StrictLogging {

  implicit def actorRefFactory: ActorRefFactory = ar
  implicit def executionContext : ExecutionContext = ec

  // Human gestation period
  private val gestationPeriod = 280

  /** The routes defined by this service */
  val routes = pathPrefix(ProgressHttpService.serviceName) {
    getProgress ~ putProgress ~ deleteProgress
  }

  @ApiOperation(value = "Gets the current progress", nickname = "getPerson", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Progress information", response = classOf[ProgressModel])
  ))
  def getProgress: Route =
    get {
      path("progress" / IntNumber) { userId =>
        completeWithFailure("getProgress", persistence.getDueDate(userId)) {
          case None =>
            complete(StatusCodes.NotFound -> s"No due date found for user $userId")

          case Some(dueDate) =>
            complete(calcModel(dueDate))
        }
      }
    }

  def putProgress: Route =
    put {
      path("progress" / IntNumber) { userId =>
        requestInstance { r =>
          entity(as[LocalDate]) { dueDate =>
            completeWithFailure("putProgress", persistence.setDueDate(userId, dueDate)) {
              case false => complete(StatusCodes.BadRequest -> s"Could not set due date $dueDate on user $userId")
              case true => complete(calcModel(dueDate))
            }
          }
        }
      }
    }

  def deleteProgress: Route =
    delete {
      path("progress" / IntNumber) { userId =>
        completeWithFailure("deleteProgress", persistence.deleteDueDate(userId)) {
          case false => complete(StatusCodes.BadRequest -> s"Could not delete due date for user $userId")
          case true => complete(StatusCodes.OK)
        }
      }
    }

  private def calcModel(dueDate: LocalDate): ProgressModel = {
    val conceptionDate = dueDate.minusDays(gestationPeriod)
    val passed = ChronoUnit.DAYS.between(conceptionDate, LocalDate.now)
    val remaining = ChronoUnit.DAYS.between(LocalDate.now, dueDate)
    ProgressModel(dueDate, passed, remaining)
  }
}
