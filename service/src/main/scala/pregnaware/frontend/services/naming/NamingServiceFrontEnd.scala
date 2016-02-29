package pregnaware.frontend.services.naming

import pregnaware.frontend.FrontEndDirectives
import pregnaware.frontend.entities.AddNameRequest
import spray.routing.Route
import pregnaware.utils.Json4sSupport._

trait NamingServiceFrontEnd extends FrontEndDirectives {

  def getNamingService: NamingServiceBackend

  // The routes provided by this service
  val namingServiceRoutes : Route = putName ~ deleteName

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
}
