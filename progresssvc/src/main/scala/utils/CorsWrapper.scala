package utils

import spray.http.{HttpHeaders, SomeOrigins}
import spray.routing._

/** Wraps a Route in the appropriate CORS header to allow cross-origin resource sharing */
object CorsWrapper extends Directives {

  def allowOrigins(allowedOrigins : Set[String]): Directive0 = mapInnerRoute { innerRoute =>
    val originHeader = optionalHeaderValueByType[HttpHeaders.Origin](())

    originHeader { originOption =>

      val origin = originOption match {
        case Some(l) =>
          val validHost = l.originList.find(x => allowedOrigins.contains(x.host.host))
          validHost

        case None =>
          None
      }

      origin match {
        case Some(o) =>
          val accessControlAllowHeader =
            HttpHeaders.`Access-Control-Allow-Headers`("Origin", "X-Requested-With", "Content-Type", "Accept")

          val accessControlAllowOrigin =
            HttpHeaders.`Access-Control-Allow-Origin`(SomeOrigins(Seq(o)))

          respondWithHeaders(accessControlAllowHeader, accessControlAllowOrigin) {
            options { complete { "" } } ~ innerRoute
          }

        case None => innerRoute
      }
    }
  }
}
