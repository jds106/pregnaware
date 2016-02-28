package naming

import akka.actor.ActorRefFactory
import com.typesafe.scalalogging.StrictLogging
import naming.entities.AddNameRequest
import spray.http.StatusCodes
import spray.routing.{HttpService, Route}
import utils.Json4sSupport._
import utils.{CustomDirectives, HeaderKeys}

import scala.concurrent.ExecutionContext

object NamingHttpService {
  val serviceName = "NamingSvc"
}

/** Support name suggestions and rankings */
case class NamingHttpService(
  persistence: NamingPersistence,
  ar: ActorRefFactory,
  ec: ExecutionContext) extends HttpService with CustomDirectives with StrictLogging {

  implicit def actorRefFactory: ActorRefFactory = ar
  implicit def executionContext : ExecutionContext = ec

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
    path("name" / IntNumber) { userId =>
      entity(as[AddNameRequest]) { request =>
        val addNameFut = persistence.addName(userId, request.suggestedById, request.name, request.isBoy)
        completeWithFailure("putName", addNameFut) { wrappedBabyName =>
          complete(StatusCodes.OK -> wrappedBabyName)
        }
      }
    }
  }

  def deleteName: Route = delete {
    path("name" / IntNumber / IntNumber) { (userId, babyNameId) =>
      completeWithFailure("deleteName", persistence.deleteName(userId, babyNameId)) {
        case false => complete(StatusCodes.NotFound -> s"No baby id $babyNameId for user $userId")
        case true => complete(StatusCodes.OK)
      }
    }
  }
}
