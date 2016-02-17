package frontend

import java.io.{File, FileWriter}
import java.net.InetSocketAddress

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import org.json4s.jackson.Serialization.{read, write}
import org.mindrot.jbcrypt.BCrypt
import spray.client.pipelining._
import spray.http.Uri.{Authority, Host, Path}
import spray.http._
import spray.routing._
import user.UserEntry
import utils.ConsulWrapper
import utils.Json4sSupport._

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.Source
import scala.util.{Failure, Success}

object FrontEndHttpService {
  val serviceName = "FrontEndSvc"
}

/** Support user login */
trait FrontEndHttpService extends HttpService with SessionPersister with StrictLogging {

  /** The name of the User Service */
  def userServiceName: String

  /** The name of the Progress Service */
  def progressServiceName: String

  /** The name of the Naming Service */
  def namingServiceName: String

  /** The HTTP connection */
  implicit def httpRef: ActorRef

  /** The HTTP context */
  implicit def ex: ExecutionContext

  private implicit val timeout: Timeout = 5.seconds

  // The password hashing salt
  val salt = BCrypt.gensalt()

  // Load in the current user sessions
  private val sessions = new scala.collection.mutable.HashMap[String, SessionEntry]()
  loadSessions.foreach(s => sessions.put(s.sessionId, s))

  val newUser = put {
    path("newUser") {
      entity(as[NewUser]) { user =>
        getService(userServiceName) { userUrl =>
          sendRequest(Get(s"$userUrl/findUser/${user.email}")) { response =>
            logger.info(s"Received response to findUser: $response")

            if (response.status.isSuccess) {
              // The user was found - cannot create a new user with this e-mail address
              logger.info(s"User already exists")
              complete(StatusCodes.Conflict -> s"User ${user.email} already exists")

            } else {
              // The user was not found - create it
              val newUserEntry =
                UserEntry(-1, user.displayName, user.email, user.dueDate, BCrypt.hashpw(user.password, salt))

              sendRequest(Put(s"$userUrl/user", newUserEntry)) { response =>
                val persistedUserEntry = response ~> unmarshal[UserEntry]
                val session = SessionEntry(persistedUserEntry.userId, java.util.UUID.randomUUID().toString)

                saveSession(session)
                sessions.put(session.sessionId, session)

                setCookie(makeCookie(session.sessionId)) {
                  complete(StatusCodes.OK)
                }
              }
            }
          }
        }
      }
    }
  }

  val login = get {
    path("login") {
      entity(as[ReturningUser]) { user =>
        getService(userServiceName) { userUrl =>
          sendRequest(Get(s"$userUrl/findUser/${user.email}")) { response =>
            if (response.status.isSuccess) {
              // User found
              val userEntry = response ~> unmarshal[UserEntry]
              val session = sessions.values.find(_.userId == userEntry.userId) match {
                case Some(s) =>
                  s

                case None =>
                  val newSession = SessionEntry(userEntry.userId, java.util.UUID.randomUUID().toString)

                  saveSession(newSession)
                  sessions.put(newSession.sessionId, newSession)
                  newSession
              }

              setCookie(makeCookie(session.sessionId)) {
                complete(StatusCodes.OK)
              }
            } else {
              // The user was not found - cannot log in
              complete(StatusCodes.Unauthorized)
            }
          }
        }
      }
    }
  }

  val getGeneralResponse1 = (get | put | post | delete) {
    pathPrefix(Segment) { serviceName =>
      logger.info(s"Processing request to $serviceName")
      pathSuffix(RestPath) { remainingUrl =>
        logger.info(s"Matched remaining URL: $remainingUrl")
        complete("OK")
      }
    }
  }

  @tailrec
  private def removePath(path: Uri.Path, part: String) : Uri.Path = {
    path match {
      case Path.Slash(tail) =>
        logger.info(s"Removing leading slash from $path -> $tail")
        removePath(tail, part)
      case Path.Segment(head, tail) if head == part =>
        logger.info(s"Removing head from $path -> $tail")
        removePath(tail, part)
      case p =>
        logger.info(s"No change to path: $p")
        p
    }
  }

  val getGeneralResponse = (get | put | post | delete) {
    pathPrefix(Segment) { serviceName =>
      val serviceNames = Seq(userServiceName, progressServiceName, namingServiceName)

      if (!serviceNames.contains(serviceName)) {
        logger.info(s"Rejecting unknown service: $serviceName - must be one of: $serviceNames")
        reject
      } else {
        logger.info(s"Processing request to $serviceName")

        requestInstance { request =>

          val remainingUrl = removePath(removePath(request.uri.path, FrontEndHttpService.serviceName), serviceName)

          logger.info(s"Routing to service $serviceName with remaining url: $remainingUrl")

          getServiceAddress(serviceName) { address =>

            // Scheme://authority/path?query#fragment ( see 3.1 of http://tools.ietf.org/html/rfc3986)
            val newAuthority = Authority(Host(address.getHostName), address.getPort)
            val newUri = request.uri.copy(
              scheme = "http",
              authority = newAuthority,
              path = Path./ + serviceName ++ Path./ ++ remainingUrl)

            val forwardingRequest = request.copy(uri = newUri)

            logger.info(s"Forwarding on request: $forwardingRequest")
            onComplete(httpRef.ask(forwardingRequest).mapTo[HttpResponse]) {
              case Failure(ex) =>
                val msg = s"Failed when making request: ${request.uri.toString}"
                logger.error(msg, ex)
                complete(StatusCodes.InternalServerError -> s"$msg - ${ex.getMessage}")

              case Success(response) =>
                logger.info(s"Had a response: $response")
                complete(response.entity.asString)
            }
          }
        }
      }
    }
  }

  //  val getProgress = get {
  //    path("progress") {
  //      cookie("sessionId") { sessionCookie =>
  //        sessions.get(sessionCookie.content) match {
  //          case None =>
  //            complete(StatusCodes.Unauthorized)
  //
  //          case Some(session) =>
  //            getService(userServiceName) { userUrl =>
  //              sendRequest(Get(s"$userUrl/user/${session.sessionId}")) { response =>
  //                if (response.status.isSuccess) {
  //                  val user = response ~> unmarshal[UserEntry]
  //                  val query = s"year=${user.dueDate.getYear}&month=${user.dueDate.getMonthValue}&day=${user.dueDate.getDayOfMonth}"
  //                  sendRequest(Get(s"$progressUrl/progress?$query")) { response =>
  //                  }
  //                } else {
  //
  //                }
  //              }
  //            }
  //        }
  //      }
  //    }
  //  }

  /** The routes defined by this service */
  val routes = pathPrefix(FrontEndHttpService.serviceName) {
    newUser ~ login ~ getGeneralResponse
  }

  private def getServiceAddress(serviceName: String)(responseHandler: InetSocketAddress => Route): Route = {
    onComplete(ConsulWrapper.getAddressAsync(serviceName)) {
      case Failure(ex) => onFail("Failed on user service address request", ex)
      case Success(address) => responseHandler(address)
    }
  }

  private def getService(serviceName: String)(responseHandler: String => Route): Route = {
    getServiceAddress(serviceName) { address =>
      responseHandler(s"http://${address.getHostName}:${address.getPort}/$serviceName")
    }
  }

  private def sendRequest(request: HttpRequest)(responseHandler: HttpResponse => Route): Route = {
    onComplete(httpRef.ask(request).mapTo[HttpResponse]) {
      case Failure(ex) =>
        onFail(s"Failed when making request: ${request.uri.toString}", ex)

      case Success(response) =>
        responseHandler(response)
    }
  }

  private def onFail(msg: String, ex: Throwable): Route = {
    logger.error(msg, ex)
    complete(StatusCodes.InternalServerError -> s"$msg: ${ex.getMessage}")
  }

  private def makeCookie(sessionId: String): HttpCookie = {
    HttpCookie("sessionId", content = sessionId)
  }
}

trait SessionPersister {
  /** Load all of the sessions into memory */
  def loadSessions: Seq[SessionEntry]

  /** Persist changes */
  def saveSession(session: SessionEntry): Unit
}

trait FileSessionPersister extends SessionPersister {

  /** The file root to store the naming suggestions */
  def root: File

  /** Load all of the names into memory */
  def loadSessions: Seq[SessionEntry] = {
    val userIdRegex = "^[0-9]+\\.json$"
    val userFiles = root.listFiles().filter(_.isFile).filter(f => f.getName.matches(userIdRegex))

    userFiles
      .map(f => Source.fromFile(f).mkString)
      .map(s => read[SessionEntry](s))
  }

  /** Persist changes */
  def saveSession(session: SessionEntry): Unit = {
    val file = new File(root, s"${session.userId}.json")
    val writer = new FileWriter(file)
    try {
      writer.write(write(session))
    } finally {
      writer.close()
    }
  }
}