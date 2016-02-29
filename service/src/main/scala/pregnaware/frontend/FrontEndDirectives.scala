package pregnaware.frontend

import com.typesafe.scalalogging.StrictLogging
import pregnaware.frontend.services.user.UserServiceBackend
import spray.routing._
import pregnaware.user.entities.WrappedUser
import pregnaware.utils.Json4sSupport._
import pregnaware.utils.{CustomDirectives, ResponseCodes}

trait FrontEndDirectives extends Directives with CustomDirectives with StrictLogging {

  def getSessionPersistence : SessionPersistence
  def getUserService : UserServiceBackend

  def getUserId(name: String)(handler: Int => Route): Route = {
    parameter('sessionId.as[String]) { sessionId =>
      routeFuture(name, getSessionPersistence.getUserIdFromSession(sessionId)) {
        case None => complete(ResponseCodes.NotFound -> s"No session found for session id $sessionId")
        case Some(userId) => handler(userId)
      }
    }
  }

  def getUser(name: String)(handler: WrappedUser => Route): Route = {
    getUserId(name + "[session]") { userId =>
      routeFuture(name + "[user]", getUserService.getUser(userId)) { user =>
        handler(user)
      }
    }
  }
}
