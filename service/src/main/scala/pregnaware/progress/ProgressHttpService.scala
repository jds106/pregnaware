package pregnaware.progress

import java.time.LocalDate

import akka.actor.{ActorContext, ActorRefFactory}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.wordnik.swagger.annotations._
import spray.routing.{HttpService, Route}
import pregnaware.utils.CustomDirectives
import pregnaware.utils.Json4sSupport._

import scala.concurrent.ExecutionContext

object ProgressHttpService {
  val serviceName = "ProgressSvc"

  def apply(persistence: ProgressPersistence)
    (implicit ac: ActorContext, ec: ExecutionContext, to: Timeout) : ProgressHttpService = {

    new ProgressHttpService(persistence) {
      // Needed for ExecutionWrapper
      implicit override final def context: ActorContext = ac
      implicit override final def executor: ExecutionContext = ec
      implicit override final def timeout: Timeout = to

      // Needed for HttpService
      implicit override final def actorRefFactory: ActorRefFactory = ac
    }
  }
}

/** Controller for the due date */
@Api(value = "/progress", description = "Pregnancy-progress related end-points", produces = "application/json")
abstract class ProgressHttpService(persistence: ProgressPersistence)
  extends HttpService with CustomDirectives with StrictLogging {

  // Human gestation period
  private val gestationPeriod = 280

  /** The routes defined by this service */
  val routes = pathPrefix(ProgressHttpService.serviceName) {
    putProgress ~ deleteProgress
  }

  def putProgress: Route =
    put {
      path("progress" / IntNumber) { userId =>
        requestInstance { r =>
          entity(as[LocalDate]) { dueDate =>
            routeFuture("putProgress", persistence.setDueDate(userId, dueDate)) { responseDueDate =>
              complete(responseDueDate)
            }
          }
        }
      }
    }

  def deleteProgress: Route =
    delete {
      path("progress" / IntNumber) { userId =>
        completeFuture("deleteProgress", persistence.deleteDueDate(userId))
      }
    }
}
