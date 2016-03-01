package pregnaware.naming

import akka.actor.{ActorContext, ActorRefFactory}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.wordnik.swagger.annotations.{ApiResponse, ApiResponses, ApiOperation, Api}
import pregnaware.naming.entities.{WrappedBabyName, AddNameRequest}
import spray.routing.{HttpService, Route}
import pregnaware.utils.Json4sSupport._
import pregnaware.utils.{CustomDirectives, ResponseCodes}

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
@Api(value = "/names", description = "Pregnancy-progress related end-points", produces = "application/json")
abstract class NamingHttpService(persistence: NamingPersistence)
  extends HttpService with CustomDirectives with StrictLogging {

  /** The routes defined by this service */
  val routes = pathPrefix(NamingHttpService.serviceName) {
    getNames ~ putName ~ deleteName
  }

  @ApiOperation(value = "Gets the current list of names", nickname = "getNames", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Name list", response = classOf[Seq[WrappedBabyName]])
  ))
  /** Returns the current list of known names */
  def getNames: Route = get {
    path("names" / IntNumber) { userId =>
      routeFuture("getNames", persistence.getNames(userId)) { names =>
        complete(ResponseCodes.OK -> names)
      }
    }
  }

  /** Adds a new name to the list */
  def putName: Route = put {
    path("names" / IntNumber) { userId =>
      entity(as[AddNameRequest]) { request =>
        val addNameFut = persistence.addName(userId, request.suggestedByUserId, request.name, request.isBoy)
        routeFuture("putName", addNameFut) { wrappedBabyName =>
          complete(ResponseCodes.OK -> wrappedBabyName)
        }
      }
    }
  }

  def deleteName: Route = delete {
    path("names" / IntNumber / IntNumber) { (userId, babyNameId) =>
      completeFuture("deleteName", persistence.deleteName(userId, babyNameId))
    }
  }
}
