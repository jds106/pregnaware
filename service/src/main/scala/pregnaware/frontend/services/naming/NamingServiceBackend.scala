package pregnaware.frontend.services.naming

import akka.actor.ActorContext
import akka.util.Timeout
import pregnaware.frontend.services.BackEndFuncs
import pregnaware.naming.entities.{AddNameRequest, WrappedBabyName}
import spray.http.HttpMethods._
import spray.httpx.ResponseTransformation._
import pregnaware.utils.Json4sSupport._

import scala.concurrent.{ExecutionContext, Future}

/** Client to the UserHttpService */
abstract class NamingServiceBackend(namingServiceName: String) extends BackEndFuncs(namingServiceName) {

  def getNames(userId: Int) : Future[Seq[WrappedBabyName]] = {
    send(GET, s"names/$userId").map( r => r ~> unmarshal[Seq[WrappedBabyName]])
  }

  def putName(
    suggestedByUserId: Int, suggestedForUserId: Int, name: String, isBoy: Boolean) : Future[WrappedBabyName] = {

    val addNameRequest = AddNameRequest(suggestedByUserId, name, isBoy)
    send(PUT, s"names/$suggestedForUserId", (b,u) => b(u, addNameRequest)).map(r => r ~> unmarshal[WrappedBabyName])
  }

  def deleteName(userId: Int, babyNameId: Int) : Future[Unit] = {
    send(DELETE, s"names/$userId/$babyNameId").map (_ => ())
  }
}

object NamingServiceBackend {
  def apply(namingServiceName: String)
    (implicit ac: ActorContext, ec: ExecutionContext, to: Timeout) : NamingServiceBackend = {

    new NamingServiceBackend(namingServiceName) {
      implicit override final def context: ActorContext = ac
      implicit override final def executor: ExecutionContext = ec
      implicit override final def timeout: Timeout = to
    }
  }
}