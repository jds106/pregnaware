package utils

import com.typesafe.scalalogging.StrictLogging
import spray.http.StatusCodes
import spray.routing._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

/** Useful directives for the HTTP services */
trait CustomDirectives extends Directives with StrictLogging {

  implicit def executionContext : ExecutionContext

  /** Runs a future, and wraps up the Try Success / Failure block */
  def completeWithFailure[T](name: String, f: Future[T])(handler: T => Route) : Route = {
    onComplete(f) {
      case Failure(error) =>
        logger.error(s"$name failed with error", error)
        complete(StatusCodes.BadRequest -> s"$name failed with error ${error.getMessage}")

      case Success(t) =>
        handler(t)
    }
  }
}
