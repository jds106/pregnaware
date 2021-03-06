package pregnaware.utils

import com.typesafe.scalalogging.StrictLogging
import spray.routing._

import scala.concurrent.Future
import scala.util.{Failure, Success}

/** Useful directives for the HTTP services */
trait CustomDirectives extends Directives with ExecutionActorWrapper with StrictLogging {

  /** Runs a future, and wraps up the Try Success / Failure block */
  def routeFuture[T](name: String, f: Future[T])(handler: T => Route) : Route = {
    logger.info(s"[$name] Starting to route future...")
    onComplete(f) {
      case Failure(error) =>
        logger.error(s"$name failed with error", error)
        complete(ResponseCodes.BadRequest -> s"$name failed with error ${error.getMessage}")

      case Success(t) =>
        logger.info(s"[$name] Routed future")
        handler(t)
    }
  }

  /** Runs a future that simply succeeds or fails, and wraps up the Try Success / Failure block */
  def completeFuture(name: String, f: Future[Unit]) : Route = {
    logger.info(s"[$name] Starting to complete future...")
    onComplete(f) {
      case Failure(error) =>
        logger.error(s"$name failed with error", error)
        complete(ResponseCodes.BadRequest -> s"$name failed with error ${error.getMessage}")

      case Success(t) =>
        logger.info(s"[$name] Completed future")
        complete(ResponseCodes.OK)
    }
  }
}
