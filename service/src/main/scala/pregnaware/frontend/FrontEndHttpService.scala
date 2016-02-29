package pregnaware.frontend

import akka.actor.{ActorContext, ActorRefFactory}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import pregnaware.frontend.services.naming.{NamingServiceBackend, NamingServiceFrontEnd}
import pregnaware.frontend.services.progress.{ProgressServiceFrontEnd, ProgressServiceBackend}
import pregnaware.frontend.services.user.{UserServiceBackend, UserServiceFrontEnd}
import pregnaware.utils.ExecutionWrapper
import spray.routing._

import scala.concurrent.ExecutionContext

/** Support user login */
abstract class FrontEndHttpService extends HttpService
  with UserServiceFrontEnd
  with ProgressServiceFrontEnd
  with NamingServiceFrontEnd
  with ExecutionWrapper
  with StrictLogging {

  /** The routes defined by this service */
  val routes =
    pathPrefix(FrontEndHttpService.serviceName) {
      userServiceRoutes ~ progressServiceRoutes ~ namingServiceRoutes
    }
}

object FrontEndHttpService {
  val serviceName = "FrontEndSvc"

  def apply(
    persistence: SessionPersistence, userSvc: UserServiceBackend,
    progressSvc: ProgressServiceBackend, namingSvc: NamingServiceBackend)
    (implicit ac: ActorContext, ec: ExecutionContext, to: Timeout) : FrontEndHttpService = {

    new FrontEndHttpService {

      // Needed for ExecutionWrapper
      implicit override final def context: ActorContext = ac
      implicit override final def executor: ExecutionContext = ec
      implicit override final def timeout: Timeout = to

      // Needed for HttpService
      implicit override final def actorRefFactory: ActorRefFactory = ac

      override def getSessionPersistence = persistence
      override def getUserService = userSvc
      override def getProgressService = progressSvc
      override def getNamingService = namingSvc
    }
  }
}