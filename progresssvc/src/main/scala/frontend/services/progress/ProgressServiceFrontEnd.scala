package frontend.services.progress

import java.time.LocalDate

import frontend.FrontEndDirectives
import spray.routing.Route
import utils.Json4sSupport._

trait ProgressServiceFrontEnd extends FrontEndDirectives {

  def getProgressService: ProgressServiceBackend

  // The routes provided by this service
  val progressServiceRoutes : Route = putDueDate ~ deleteDueDate

  def putDueDate: Route = put {
    path("progress") {
      getUserId("putDueDate") { userId =>
        entity(as[LocalDate]) { dueDate =>
          completeWithFailure("putDueDate", getProgressService.putProgress(userId, dueDate)) { model =>
            complete(model)
          }
        }
      }
    }
  }

  def deleteDueDate: Route = delete {
    path("progress") {
      getUserId("deleteDueDate") { userId =>
        completeWithFailure("deleteDueDate", getProgressService.deleteProgress(userId))
      }
    }
  }
}
