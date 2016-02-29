package frontend

import com.typesafe.scalalogging.StrictLogging
import frontend.services.user.UserServiceBackend
import spray.http.StatusCodes
import spray.routing._
import user.entities.WrappedUser
import utils.CustomDirectives
import utils.Json4sSupport._

trait FrontEndDirectives extends Directives with CustomDirectives with StrictLogging {

  def getSessionPersistence : SessionPersistence
  def getUserService : UserServiceBackend

  def getUserId(name: String)(handler: Int => Route): Route = {
    parameter('sessionId.as[String]) { sessionId =>
      completeWithFailure(name, getSessionPersistence.getUserIdFromSession(sessionId)) {
        case None => complete(StatusCodes.NotFound -> s"No session found for session id $sessionId")
        case Some(userId) => handler(userId)
      }
    }
  }

  def getUser(name: String)(handler: WrappedUser => Route): Route = {
    getUserId(name + "[session]") { userId =>
      completeWithFailure(name + "[user]", getUserService.getUser(userId)) { user =>
        handler(user)
      }
    }
  }
}
