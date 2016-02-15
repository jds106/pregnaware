package utils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import com.wordnik.swagger.annotations._
import spray.routing.{HttpService, Route}

/** Controller for the due date */
@Api(value = "/progress", description = "Pregnancy-progress related end-points", produces = "application/json")
trait HealthHttpService extends HttpService {
  import utils.Json4sSupport._

  // Human gestation period
  private val gestationPeriod = 280

  /** The routes defined by this service */
  val routes = getProgress

  @ApiOperation(value = "Gets the current progress", nickname = "getPerson", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name="year", value="Due date (year)", dataType="int", required=true, paramType="query"),
    new ApiImplicitParam(name="month", value="Due date (month)", dataType="int", required=true, paramType="query"),
    new ApiImplicitParam(name="day", value="Due date (day)", dataType="int", required=true, paramType="query")
  ))
  @ApiResponses(Array(
    new ApiResponse(code=200, message="Progress information", response=classOf[ProgressModel])
  ))
  def getProgress : Route =
    path("progress") {
      parameters('year.as[Int], 'month.as[Int], 'day.as[Int]) { (year, month, day) =>
        get {
          val conceptionDate = LocalDate.of(year, month, day)
          val dueDate = conceptionDate.plusDays(gestationPeriod)
          val passed = ChronoUnit.DAYS.between(conceptionDate, LocalDate.now)
          val remaining = ChronoUnit.DAYS.between(LocalDate.now, dueDate)

          complete(ProgressModel(dueDate, passed, remaining))
        }
      }
    }
}
