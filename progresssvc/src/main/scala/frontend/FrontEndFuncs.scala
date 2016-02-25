package frontend

import java.net.InetSocketAddress

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import spray.client.pipelining._
import spray.http.Uri.Path
import spray.http.{StatusCodes, Uri, _}
import spray.routing._
import user.UserEntry
import utils.ConsulWrapper
import utils.Json4sSupport._

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by james on 22/02/2016.
  */
trait FrontEndFuncs extends HttpService with StrictLogging {

  /** The session manager */
  def sessionManager: SessionManager

  /** The name of the User Service */
  def userServiceName: String

  /** The HTTP connection */
  implicit def httpRef: ActorRef

  /** The HTTP context */
  implicit def ex: ExecutionContext

  /** The default request timeout */
  implicit val timeout: Timeout = 5.seconds

  /** Strips the "part" from the path (and any leading slashes) */
  @tailrec
  protected final def removePath(path: Uri.Path, part: String): Uri.Path = {
    path match {
      case Path.Slash(tail) =>
        removePath(tail, part)
      case Path.Segment(head, tail) if head == part =>
        removePath(tail, part)
      case p =>
        p
    }
  }

  protected final def getServiceAddress(serviceName: String)(responseHandler: InetSocketAddress => Route): Route = {
    onComplete(ConsulWrapper.getAddressAsync(serviceName)) {
      case Failure(error) => onFail("Failed on user service address request", error)
      case Success(address) => responseHandler(address)
    }
  }

  protected final def getService(serviceName: String)(responseHandler: String => Route): Route = {
    getServiceAddress(serviceName) { address =>
      responseHandler(s"http://${address.getHostName}:${address.getPort}/$serviceName")
    }
  }

  protected final def sendRequest(request: HttpRequest)(responseHandler: HttpResponse => Route): Route = {
    onComplete(httpRef.ask(request).mapTo[HttpResponse]) {
      case Failure(error) =>
        onFail(s"Failed when making request: ${request.uri.toString}", error)

      case Success(response) =>
        responseHandler(response)
    }
  }

  protected final def onFail(msg: String, ex: Throwable): Route = {
    logger.error(msg, ex)
    complete(StatusCodes.InternalServerError -> s"$msg: ${ex.getMessage}")
  }

  /** Fetches the user entry for the specified userId parameter (which must be either the session user or a friend) */
  def fetchUser(userHandler: UserEntry => Route): Route = {
    parameter('sessionId.as[String]) { sessionId =>
      parameter('userId.as[Option[Int]]) { requestedUserIdOpt =>
        fetchSessionUser { sessionUser =>
          val requestedUserId = requestedUserIdOpt.getOrElse(sessionUser.userId)

          if (sessionUser.userId == requestedUserId) {
            userHandler(sessionUser)

          } else if (sessionUser.friends.contains(requestedUserId)) {
            getService(userServiceName) { userUrl =>
              sendRequest(Get(s"$userUrl/user/$requestedUserId")) { friendResponse =>
                userHandler(friendResponse ~> unmarshal[UserEntry])
              }
            }
          } else {
            complete(StatusCodes.Forbidden -> s"User ${sessionUser.userId} not allowed to access user $requestedUserId")
          }
        }
      }
    }
  }

  /** Fetches the user entry for the specified session */
  def fetchSessionUser(userHandler: UserEntry => Route): Route = {
    parameter('sessionId.as[String]) { sessionId =>
      sessionManager.getSession(sessionId) match {
        case None =>
          complete(StatusCodes.Unauthorized -> "User not logged in")

        case Some(session) =>
          getService(userServiceName) { userUrl =>
            sendRequest(Get(s"$userUrl/user/${session.userId}")) { response =>
              userHandler(response ~> unmarshal[UserEntry])
            }
          }
      }
    }
  }
}
