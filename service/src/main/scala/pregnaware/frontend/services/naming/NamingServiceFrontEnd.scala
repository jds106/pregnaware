package pregnaware.frontend.services.naming

import pregnaware.frontend.FrontEndDirectives
import pregnaware.frontend.entities.AddNameRequest
import spray.routing.Route
import pregnaware.utils.Json4sSupport._

trait NamingServiceFrontEnd extends FrontEndDirectives {

  def getNamingService: NamingServiceBackend

  // The routes provided by this service
  val namingServiceRoutes: Route = putName ~ deleteName ~ getNameStats ~ getNameStats

  def putName: Route = put {
    path("names" / IntNumber) { suggestedForUserId =>
      getUserId("putName") { userId =>
        entity(as[AddNameRequest]) { addNameRequest =>

          val putNameFut = getNamingService.putName(
            userId, suggestedForUserId, addNameRequest.name, addNameRequest.isBoy)

          routeFuture("putName", putNameFut) { model =>
            complete(model)
          }
        }
      }
    }
  }

  def deleteName: Route = delete {
    path("names" / IntNumber) { babyNameId =>
      getUserId("deleteName") { userId =>
        completeFuture("deleteName", getNamingService.deleteName(userId, babyNameId))
      }
    }
  }

  def getNameStats: Route = get {
    // The end points are gender-specific
    val genderMap = Map("boy" -> "boy", "girl" -> "girl")

    path("namestats" / "meta" / "years") {
      dynamic { routeFuture("nameStatsYears", getNamingService.getNameStatsYears) { r => complete(r) } }
    } ~
      path("namestats" / "meta" / "count") {
        dynamic { routeFuture("nameStatsCount", getNamingService.getNameStatsCount) { r => complete(r) } }
      } ~
      path("namestats" / "data" / genderMap / "complete" / "name" / Segment) { (gender, name) =>
        routeFuture("nameStats01", getNamingService.getNameStatsComplete(name, gender)) { r => complete(r) }
      } ~
      path("namestats" / "data" / genderMap / "complete" / "summary" / IntNumber) { (gender, year) =>
        routeFuture("nameStats02", getNamingService.getNameStatsComplete(year, gender)) { r => complete(r) }
      } ~
      path("namestats" / "data" / genderMap / "country" / "name" / Segment) { (gender, name) =>
        routeFuture("nameStats03", getNamingService.getNameStatsByCountry(name, gender)) { r => complete(r) }
      } ~
      path("namestats" / "data" / genderMap / "country" / "summary" / IntNumber) { (gender, year) =>
        routeFuture("nameStats04", getNamingService.getNameStatsByCountry(year, gender)) { r => complete(r) }
      } ~
      path("namestats" / "data" / genderMap / "month" / "name" / Segment) { (gender, name) =>
        routeFuture("nameStats05", getNamingService.getNameStatsByMonth(name, gender)) { r => complete(r) }
      } ~
      path("namestats" / "data" / genderMap / "month" / "summary" / IntNumber) { (gender, year) =>
        routeFuture("nameStats06", getNamingService.getNameStatsByMonth(year, gender)) { r => complete(r) }
      } ~
      path("namestats" / "data" / genderMap / "region" / "name" / Segment) { (gender, name) =>
        routeFuture("nameStats07", getNamingService.getNameStatsByRegion(name, gender)) { r => complete(r) }
      } ~
      path("namestats" / "data" / genderMap / "region" / "summary" / IntNumber) { (gender, year) =>
        routeFuture("nameStats08", getNamingService.getNameStatsByRegion(year, gender)) { r => complete(r) }
      }
    }
}
