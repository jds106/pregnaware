package pregnaware.utils

import java.time.LocalDate

import com.wordnik.swagger.annotations._
import spray.routing.{HttpService, Route}

/** Controller for the due date */
@Api(value = "/health", description = "Health end-point", produces = "application/json")
trait HealthHttpService extends HttpService {

  import Json4sSupport._

  case class DetailedHealthResponse(component: String, detail: String)

  case class HealthResponse(ok: Boolean, detail: Seq[DetailedHealthResponse])

  /** Override this with a more detailed response */
  def getHealthResponse: HealthResponse = HealthResponse(ok = true, Seq.empty[DetailedHealthResponse])

  /** The routes defined by this service */
  val routes = getHealth

  @ApiOperation(value = "Health end-point", nickname = "getHealth", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "All systems operational", response = classOf[LocalDate])
  ))
  def getHealth: Route =
    path("health") {
      get {
        val response = getHealthResponse
        if (response.ok) {
          complete {
            ResponseCodes.OK -> response
          }
        } else {
          complete {
            ResponseCodes.ServiceUnavailable -> response
          }
        }
      }
    }
}
