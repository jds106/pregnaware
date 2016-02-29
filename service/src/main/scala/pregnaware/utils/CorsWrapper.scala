package pregnaware.utils

import com.typesafe.scalalogging.StrictLogging
import spray.http.{HttpMethods, HttpMethod, HttpHeaders, SomeOrigins}
import spray.routing._

/** Wraps a Route in the appropriate CORS header to allow cross-origin resource sharing */
object CorsWrapper extends Directives with StrictLogging {

  def allowOrigins(allowedOrigins : Set[String]): Directive0 = mapInnerRoute { innerRoute =>
    val originHeader = optionalHeaderValueByType[HttpHeaders.Origin](())

    originHeader { originOption =>

      val origin = originOption match {
        case Some(l) =>
          val validHost = l.originList.find(x => allowedOrigins.contains(x.host.host))

          validHost match {
            case Some(host) => logger.info(s"Allowing cross-origin request from host: $host (mapped to $validHost)")
            case None => logger.info(s"Blocking cross-origin request from host: ${l.originList}")
          }

          validHost

        case None =>
          None
      }

      origin match {
        case Some(o) =>
          val accessControlAllowHeader =
            HttpHeaders.`Access-Control-Allow-Headers`("Origin", "X-Requested-With", "Content-Type", "Accept")

          val accessControlAllowMethods =
            HttpHeaders.`Access-Control-Allow-Methods`(
              HttpMethods.GET, HttpMethods.PUT, HttpMethods.POST, HttpMethods.DELETE)

          val accessControlAllowOrigin =
            HttpHeaders.`Access-Control-Allow-Origin`(SomeOrigins(Seq(o)))

          respondWithHeaders(accessControlAllowHeader, accessControlAllowOrigin, accessControlAllowMethods) {
            options { complete { "" } } ~ innerRoute
          }

        case None => innerRoute
      }
    }
  }
}
