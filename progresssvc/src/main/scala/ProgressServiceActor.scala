import java.io.File

import akka.actor.ActorDSL._
import akka.actor._
import akka.io.IO
import akka.io.Tcp.Bound
import com.typesafe.scalalogging.StrictLogging
import frontend.{SessionManager, FileSessionPersister, FrontEndHttpService}
import naming.{FileNamePersister, NamingHttpService}
import progress.{FileProgressPersister, ProgressHttpService}
import spray.can.Http
import spray.routing._
import user.{FileUserPersister, UserHttpService}
import utils._

import scala.reflect.runtime.universe._

class ProgressServiceActor extends HttpServiceActor with ActorLogging {
  override def actorRefFactory : ActorContext = context

  val fileRoot = new File("/Users/james/Programming/scala/graviditate/tmp")
  val fileRoots = Map[String, File](
    NamingHttpService.serviceName -> new File(fileRoot, NamingHttpService.serviceName),
    UserHttpService.serviceName -> new File(fileRoot, UserHttpService.serviceName),
    FrontEndHttpService.serviceName -> new File(fileRoot, FrontEndHttpService.serviceName),
    ProgressHttpService.serviceName -> new File(fileRoot, ProgressHttpService.serviceName)
  )

  // Ensure the file roots exists
  fileRoots.values.filter(!_.exists()).foreach(_.mkdirs())

  val progressService = new ProgressHttpService with FileProgressPersister {
    def root = fileRoots(ProgressHttpService.serviceName)
    def actorRefFactory = context
  }

  val namingService = new NamingHttpService with FileNamePersister {
    def root = fileRoots(NamingHttpService.serviceName)
    def actorRefFactory = context
  }

  val userService = new UserHttpService with FileUserPersister {
    def root = fileRoots(UserHttpService.serviceName)
    def actorRefFactory = context
  }

  // This handles current user sessions
  val currentSessionManager = new SessionManager with FileSessionPersister
  {
    def root = fileRoots(FrontEndHttpService.serviceName)
  }

  val frontEndService = new FrontEndHttpService {
    def userServiceName: String = UserHttpService.serviceName
    def namingServiceName: String = NamingHttpService.serviceName
    def progressServiceName: String = ProgressHttpService.serviceName

    def sessionManager: SessionManager = currentSessionManager

    def actorRefFactory = context

    // Provide access to the IO layer
    import context.system
    implicit def httpRef = IO(Http)
    implicit def ex = context.dispatcher
  }

  val healthService = new HealthHttpService {
    def actorRefFactory = context
  }

  // Initialise the Swagger end-point for documentation */
  val serviceTypes = Seq(typeOf[ProgressHttpService])
  val swaggerService = SwaggerServiceWrapper.swaggerService(
    context, serviceTypes,
    "Pregnancy Progress",
    "A simple service based around the progress of a pregnancy")

  // This ensures that CORS is only permitted for known hosts
  val validOrigins = Set("petstore.swagger.io", "localhost")
  val route = CorsWrapper.allowOrigins(validOrigins) {
    progressService.routes ~
      healthService.routes ~
      namingService.routes ~
      userService.routes ~
      frontEndService.routes ~
      swaggerService.routes
  }

  // Process incoming requests
  def receive : Actor.Receive = runRoute(route)
}

object ProgressServiceActor extends App with StrictLogging {
  implicit val system = ActorSystem()
  val service = system.actorOf(Props[ProgressServiceActor])

  // The IO Listener received Bind update messages from the spray.can.server.HttpListener
  // We can then log simple HTTP connection status messages (or handle things like time-outs / unbinds)
  val ioListener = actor("ioListener")(new Act with ActorLogging {
    become {
      case b @ Bound(connection) => log.info(s"Received message: $b")
      case m => logger.error(s"Unexpected message: $m")
    }
  })

  val serviceName = "ProgressSvc"
  val address = ConsulWrapper.getAddress(serviceName)(IO(Http), system.dispatcher)
  logger.info(s"Starting service '$serviceName'. " +
    s"Pid: ${SysUtils.pid}, host: ${address.getHostName}, port: ${address.getPort}")

  IO(Http).tell(Http.Bind(service, address.getHostName, address.getPort), ioListener)
}
