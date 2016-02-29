package naming

import akka.actor.{ActorContext, ActorRefFactory}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import naming.entities.AddNameRequest
import spray.http.StatusCodes
import spray.routing.{HttpService, Route}
import utils.CustomDirectives
import utils.Json4sSupport._

import scala.concurrent.ExecutionContext

object NamingHttpService {
  val serviceName = "NamingSvc"

  def apply(persistence: NamingPersistence)
    (implicit ac: ActorContext, ec: ExecutionContext, to: Timeout) : NamingHttpService = {

    new NamingHttpService(persistence) {

      // Needed for ExecutionWrapper
      implicit override final def context: ActorContext = ac
      implicit override final def executor: ExecutionContext = ec
      implicit override final def timeout: Timeout = to

      // Needed for HttpService
      implicit override final def actorRefFactory: ActorRefFactory = ac
    }
  }
}

/** Support name suggestions and rankings */
abstract class NamingHttpService(persistence: NamingPersistence)
  extends HttpService with CustomDirectives with StrictLogging {

  /** The routes defined by this service */
  val routes = pathPrefix(NamingHttpService.serviceName) {
    getNames ~ putName ~ deleteName
  }

  /** Returns the current list of known names */
  def getNames: Route = get {
    path("names" / IntNumber) { userId =>
      completeWithFailure("getNames", persistence.getNames(userId)) { names =>
        complete(StatusCodes.OK -> names)
      }
    }
  }

  /** Adds a new name to the list */
  def putName: Route = put {
    path("names" / IntNumber) { userId =>
      entity(as[AddNameRequest]) { request =>
        val addNameFut = persistence.addName(userId, request.suggestedByUserId, request.name, request.isBoy)
        completeWithFailure("putName", addNameFut) { wrappedBabyName =>
          complete(StatusCodes.OK -> wrappedBabyName)
        }
      }
    }
  }

  def deleteName: Route = delete {
    path("names" / IntNumber / IntNumber) { (userId, babyNameId) =>
      completeWithFailure("deleteName", persistence.deleteName(userId, babyNameId))
    }
  }
}
