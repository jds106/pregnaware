package pregnaware.utils

import spray.http.StatusCodes

/** These are the ONLY StatusCodes supported by the application */
object ResponseCodes {
  val OK = StatusCodes.OK
  val NotFound = StatusCodes.NotFound
  val Conflict = StatusCodes.Conflict
  val BadRequest = StatusCodes.BadRequest
  val ServiceUnavailable = StatusCodes.ServiceUnavailable
}
