package app

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import org.json4s.Formats
import spray.routing.HttpService
import com.wordnik.swagger.annotations._
import spray.http.StatusCodes.OK
import spray.routing.HttpService

/** Controller for the due date */
trait ProgressHttpService extends HttpService {

  import utils.Json4sSupport._

  // Human gestation period
  private val gestationPeriod = 280

  val routes = getProgress

  @ApiOperation(value = "Gets the current progress", notes = "", nickname = "getPerson", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Person with name", dataType = "Person", required = true, paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "The progress")
  ))
  def getProgress =
    path("progress") {
      get {
        val conceptionDate = LocalDate.of(2015, 10, 15)
        val dueDate = conceptionDate.plusDays(gestationPeriod)
        val passed = ChronoUnit.DAYS.between(conceptionDate, LocalDate.now)
        val remaining = ChronoUnit.DAYS.between(LocalDate.now, dueDate)

        complete(ProgressModel(dueDate, passed, remaining))
      }
    }
}

