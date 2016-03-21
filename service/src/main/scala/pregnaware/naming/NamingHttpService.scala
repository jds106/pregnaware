package pregnaware.naming

import akka.actor.{ActorContext, ActorRefFactory}
import akka.event.Logging._
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
    (implicit ac: ActorContext, ec: ExecutionContext, to: Timeout): NamingHttpService = {

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
    logRequest("REST API", InfoLevel) {
      logResponse("REST API", InfoLevel) {
        getNames ~ putName ~ deleteName ~
          getNameStatsCategories ~ getNameStatsYears ~
          getNameStatsByCountry ~ getNameStatsByMonth ~ getNameStatsByRegion
      }
    }
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

  /** Removes a baby name from the list */
  def deleteName: Route = delete {
    path("names" / IntNumber / IntNumber) { (userId, babyNameId) =>
      completeFuture("deleteName", persistence.deleteName(userId, babyNameId))
    }
  }

  /* Meta data: The set of naming categories */
  def getNameStatsCategories : Route = get {
    path("namestats" / "meta" / "categories") {
      val list = Array("NameStat", "NameStatByCountry", "NameStatByMonth", "NameStatByRegion")
      complete(ResponseCodes.OK -> list)
    }
  }

  /* Meta data: The set of years for which data is available */
  def getNameStatsYears : Route = get {
    path("namestats" / "meta" / "years") {
      val list = Range(1996, 2015)
      complete(ResponseCodes.OK -> list)
    }
  }

  def getNameStats : Route = get {
    path("namestats" / "data" / "NameStat") {
      // Returns the position & percentage for the specified name for each year
      path(Segment) { name =>
        complete(ResponseCodes.OK)
      } ~
      // Returns the top 100 names for the specified year & gender
      path(IntNumber / Segment) { (year, gender) =>
        complete(ResponseCodes.OK)
      }
    }
  }

  def getNameStatsByCountry : Route = get {
    path("namestats" / "data" / "NameStatByCountry") {
      // Returns the position for the specified name in each region for each year (if it is in the top 10)
      path(Segment) { name =>
        complete(ResponseCodes.OK)
      } ~
        // Returns all of the top-10 information for the specified year & gender
        path(IntNumber / Segment) { (year, gender) =>
          complete(ResponseCodes.OK)
        }
    }
  }

  def getNameStatsByMonth : Route = get {
    path("namestats" / "data" / "NameStatByMonth") {
      // Returns the position for the specified name for month each year (if it is in the top 10)
      path(Segment) { name =>
        complete(ResponseCodes.OK)
      } ~
        // Returns all of the top-10 information for the specified year & gender
        path(IntNumber / Segment) { (year, gender) =>
          complete(ResponseCodes.OK)
        }
    }
  }

  def getNameStatsByRegion : Route = get {
    path("namestats" / "data" / "NameStatByRegion") {
      // Returns the position & percentage for the specified name for each year (if it is in the top 10)
      path(Segment) { name =>
        complete(ResponseCodes.OK)
      } ~
        // Returns all of the top-10 information for the specified year & gender
        path(IntNumber / Segment) { (year, gender) =>
          complete(ResponseCodes.OK)
        }
    }
  }
}