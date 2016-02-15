import akka.actor.ActorDSL._
import akka.actor._
import akka.io.IO
import akka.io.Tcp.Bound
import app.ProgressHttpService
import com.typesafe.scalalogging.StrictLogging
import spray.can.Http
import spray.routing._
import utils._

import scala.reflect.runtime.universe._

class ProgressServiceActor extends HttpServiceActor with ActorLogging {
  override def actorRefFactory : ActorContext = context

  val progressService = new ProgressHttpService {
    def actorRefFactory = context
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
  val validOrigins = Set("petstore.swagger.io")
  val route = CorsWrapper.allowOrigins(validOrigins) {
    progressService.routes ~ healthService.routes ~ swaggerService.routes
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
    }
  })

  val serviceName = "ProgressSvc"
  val address = ConsulWrapper.getAddress(serviceName)
  logger.info(s"Starting service '$serviceName'. " +
    s"Pid: ${SysUtils.pid}, host: ${address.getHostName}, port: ${address.getPort}")

  IO(Http).tell(Http.Bind(service, address.getHostName, address.getPort), ioListener)
}
