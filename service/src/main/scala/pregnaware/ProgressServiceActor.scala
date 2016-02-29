import akka.actor.ActorDSL._
import akka.actor._
import akka.io.IO
import akka.io.Tcp.Bound
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import pregnaware.database.DatabaseWrapper
import pregnaware.frontend.FrontEndHttpService
import pregnaware.frontend.services.naming.NamingServiceBackend
import pregnaware.frontend.services.progress.ProgressServiceBackend
import pregnaware.frontend.services.user.UserServiceBackend
import pregnaware.naming.NamingHttpService
import pregnaware.progress.ProgressHttpService
import spray.can.Http
import spray.routing._
import pregnaware.user.UserHttpService
import pregnaware.utils._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.reflect.runtime.universe._

class ProgressServiceActor extends HttpServiceActor with ActorLogging with ExecutionWrapper {

  override implicit def executor: ExecutionContext = context.dispatcher
  override implicit def timeout: Timeout = 10.seconds

  private val databasePersistence = DatabaseWrapper.apply

  private val progressService = ProgressHttpService(databasePersistence)
  private val namingService = NamingHttpService(databasePersistence)
  private val userService = UserHttpService(databasePersistence)

  val frontEndService = FrontEndHttpService(
    databasePersistence,
    UserServiceBackend(UserHttpService.serviceName),
    ProgressServiceBackend(ProgressHttpService.serviceName),
    NamingServiceBackend(NamingHttpService.serviceName))

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
  def receive: Actor.Receive = runRoute(route)
}

object ProgressServiceActor extends StrictLogging {
  private val serviceName = "ProgressSvc"

  def main(args: Array[String]) : Unit = {
    implicit val system = ActorSystem()
    implicit val executor: ExecutionContext = system.dispatcher

    val service = system.actorOf(Props[ProgressServiceActor])

    // The IO Listener received Bind update messages from the spray.can.server.HttpListener
    // We can then log simple HTTP connection status messages (or handle things like time-outs / unbinds)
    val ioListener = actor("ioListener")(new Act with ActorLogging {
      become {
        case b@Bound(connection) => log.info(s"Received message: $b")
        case m => logger.error(s"Unexpected message: $m")
      }
    })

    ConsulWrapper.getAddress(serviceName).map { address =>
      logger.info(s"Starting service '$serviceName'. " +
        s"Pid: ${SysUtils.pid}, host: ${address.getHostName}, port: ${address.getPort}")

      IO(Http).tell(Http.Bind(service, address.getHostName, address.getPort), ioListener)
    }.onFailure {
      case e =>
        logger.error("Could not start", e)
        system.shutdown()
        System.exit(-1)
    }
  }
}
