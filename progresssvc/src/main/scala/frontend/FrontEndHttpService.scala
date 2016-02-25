package frontend

import akka.pattern.ask
import com.typesafe.scalalogging.StrictLogging
import frontend.services.UserManagementHttpService
import spray.client.pipelining._
import spray.http.Uri.{Authority, Host, Path}
import spray.http._
import spray.routing._
import utils.HeaderKeys
import utils.Json4sSupport._

import scala.util.{Failure, Success}

object FrontEndHttpService {
  val serviceName = "FrontEndSvc"
}

/** Support user login */
trait FrontEndHttpService extends HttpService with FrontEndFuncs with StrictLogging {

  /** The name of the Progress Service */
  def progressServiceName: String

  /** The name of the Naming Service */
  def namingServiceName: String

  /** The session manager */
  def sessionManager: SessionManager

  /** Delegate user funcitons to the user management service */
  private val userMgmtSvc = new UserManagementHttpService(
    sessionManager, userServiceName, actorRefFactory, this.ex, this.httpRef)

  /** The routes defined by this service */
  val routes =
    pathPrefix(FrontEndHttpService.serviceName) {
      userMgmtSvc.routes ~ getGeneralResponse
    }

  private def getGeneralResponse : Route = (get | put | post | delete) {
    logRequestResponse("GeneralRequest", akka.event.Logging.ErrorLevel) {
      pathPrefix(Segment) { serviceName =>
        val serviceNames = Seq(userServiceName, progressServiceName, namingServiceName)

        if (!serviceNames.contains(serviceName)) {
          logger.info(s"Rejecting unknown service: $serviceName - must be one of: $serviceNames")
          reject
        } else {
          requestInstance { request =>
              val remainingUrl = removePath(removePath(request.uri.path, FrontEndHttpService.serviceName), serviceName)

              logger.info(s"Routing to service $serviceName with remaining url: $remainingUrl")
              fetchUser { user =>
                logger.info(s"Routing user ${user.userId} / ${user.email} to $serviceName")

                getServiceAddress(serviceName) { address =>

                  // Scheme://authority/path?query#fragment (see 3.1 of http://tools.ietf.org/html/rfc3986)
                  val newAuthority = Authority(Host(address.getHostName), address.getPort)
                  val newUri = request.uri.copy(
                    scheme = "http",
                    authority = newAuthority,
                    path = Path./ + serviceName ++ Path./ ++ remainingUrl)

                  val forwardingRequest =
                    request.copy(uri = newUri) ~>
                      addHeader(HeaderKeys.EntryId, user.userId.toString)

                  logger.info(s"Forwarding on request: $forwardingRequest")
                  onComplete(httpRef.ask(forwardingRequest).mapTo[HttpResponse]) {
                    case Failure(ex) =>
                      val msg = s"Failed when making request: ${request.uri.toString}"
                      logger.error(msg, ex)
                      complete(StatusCodes.InternalServerError -> s"$msg - ${ex.getMessage}")

                    case Success(response) =>
                      logger.info(s"Had a response: $response")
                      complete(HttpResponse(status = response.status, entity = response.entity))
                  }
                }
            }
          }
        }
      }
    }
  }
}

