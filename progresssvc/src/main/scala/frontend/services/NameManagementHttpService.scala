package frontend.services

import akka.actor.{ActorRef, ActorRefFactory}
import com.typesafe.scalalogging.StrictLogging
import frontend._
import spray.routing._
import utils.Json4sSupport._

import scala.concurrent.ExecutionContext

/** Manage the set of baby names */
case class NameManagementHttpService(
  sessionManager: SessionManager,
  userServiceName: String,
  context: ActorRefFactory,
  implicit val ex: ExecutionContext,
  implicit val httpRef: ActorRef) extends HttpService with FrontEndFuncs with StrictLogging {

  override def actorRefFactory : ActorRefFactory = context

  /** The routes this service provides */
  val routes : Route = getNames ~ putName ~ deleteName

  private def getNames : Route =
    get {
      path("names") {
        complete("")
      }
    }

  private def putName : Route =
    put {
      path("name") {
        complete("")
      }
    }

  private def deleteName : Route =
    delete {
      path("name") {
        complete("")
      }
    }
}
