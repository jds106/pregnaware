package pregnaware.frontend.services.naming

import pregnaware.frontend.FrontEndDirectives
import pregnaware.frontend.entities.AddNameRequest
import spray.routing.Route
import pregnaware.utils.Json4sSupport._

trait NamingServiceFrontEnd extends FrontEndDirectives {

  def getNamingService: NamingServiceBackend

  // The routes provided by this service
  val namingServiceRoutes : Route = putName ~ deleteName ~ getNameStats ~ getNameStatsForName

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
    path("namestats" / "meta" / "categories") {
      completeFuture("nameStats", getNamingService.getNameStats)
    }
  }

  def getNameStatsForName: Route = get {
    path("namestats" / Segment) { name =>
      completeFuture("nameStatsForName", getNamingService.getNameStats(name))
    }
  }
}
