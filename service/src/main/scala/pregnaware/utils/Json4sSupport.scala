package pregnaware.utils

import org.json4s._
import spray.httpx.Json4sJacksonSupport
import pregnaware.utils.formats.LocalDateFormat

object Json4sSupport extends Json4sJacksonSupport {
  implicit override def json4sJacksonFormats: Formats = DefaultFormats + new LocalDateFormat
}
