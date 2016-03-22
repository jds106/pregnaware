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

  // The max number of names to return in one request
  private val maxNamesResponse = 100

  // The comversion between the gender (as a string) and whether that gender represents a boy or a girl
  private val genderMap = Map("boy" -> true, "girl" -> false)

  /** The routes defined by this service */
  val routes = pathPrefix(NamingHttpService.serviceName) {
    logRequest("REST API", InfoLevel) {
      logResponse("REST API", InfoLevel) {
        getNames ~ putName ~ deleteName ~
          getNameStatsCategories ~ getNameMetaStats ~ getNameStats
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
  def getNameStatsCategories: Route = get {
    path("namestats" / "meta" / "categories") {
      val list = Array("NameStat", "NameStatByCountry", "NameStatByMonth", "NameStatByRegion")
      complete(ResponseCodes.OK -> list)
    }
  }

  /* Meta data: The set of years for which data is available */
  def getNameMetaStats: Route = get {
    pathPrefix("namestats" / "meta") {
      path("years") {
        routeFuture("getNameStatsYears", persistence.getAvailableYears) { years =>
          complete(ResponseCodes.OK -> years)
        }
      } ~
        path("count") {
          routeFuture("getNumBabiesForYear", persistence.getNumBabies) { numBabies =>
            complete(ResponseCodes.OK -> numBabies)
          }
        }
    }
  }

  def getNameStats: Route = get {
    pathPrefix("namestats" / "data" / genderMap) { isBoy =>
      pathPrefix("complete") {
        path("name" / Segment) { name =>
          routeFuture("getNameStats", persistence.getNameStats(name, isBoy)) { stats =>
            complete(ResponseCodes.OK -> stats)
          }
        } ~
          path("summary" / IntNumber) { year =>
            routeFuture("getNameStats", persistence.getNameStats(year, isBoy, maxNamesResponse)) { stats =>
              complete(ResponseCodes.OK -> stats)
            }
          }
      } ~
        pathPrefix("country") {
          path("name" / Segment) { name =>
            routeFuture("getNameStatsByCountry", persistence.getNameStatsByCountry(name, isBoy)) { stats =>
              complete(ResponseCodes.OK -> stats)
            }
          } ~
            path("summary" / IntNumber) { year =>
              routeFuture("getNameStatsByCountry", persistence.getTop10NameStatsByCountry(year, isBoy)) { stats =>
                complete(ResponseCodes.OK -> stats)
              }
            }
        } ~
        pathPrefix("month") {
          path("name" / Segment) { name =>
            routeFuture("getNameStatsByMonth", persistence.getNameStatsByMonth(name, isBoy)) { stats =>
              complete(ResponseCodes.OK -> stats)
            }
          } ~
            path("summary" / IntNumber) { year =>
              routeFuture("getNameStatsByMonth", persistence.getTop10NameStatsByMonth(year, isBoy)) { stats =>
                complete(ResponseCodes.OK -> stats)
              }
            }
        } ~
        pathPrefix("region") {
          path("name" / Segment) { name =>
            routeFuture("getNameStatsByRegion", persistence.getNameStatsByRegion(name, isBoy)) { stats =>
              complete(ResponseCodes.OK -> stats)
            }
          } ~
            path("summary" / IntNumber) { year =>
              routeFuture("getNameStatsByRegion", persistence.getTop10NameStatsByRegion(year, isBoy)) { stats =>
                complete(ResponseCodes.OK -> stats)
              }
            }
        }
    }
  }
}