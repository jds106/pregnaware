package frontend.services.naming

import frontend.FrontEndDirectives
import frontend.entities.AddNameRequest
import spray.http.StatusCodes
import spray.routing.Route
import utils.Json4sSupport._

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

          completeWithFailure("putDueDate", putNameFut) { model =>
            complete(model)
          }
        }
      }
    }
  }

  def deleteName: Route = delete {
    path("names" / IntNumber) { babyNameId =>
      getUserId("deleteName") { userId =>
        completeWithFailure("deleteDueDate", getNamingService.deleteName(userId, babyNameId))
      }
    }
  }
}
