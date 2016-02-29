package pregnaware.frontend.services.progress

import java.time.LocalDate

import akka.actor.ActorContext
import akka.util.Timeout
import pregnaware.frontend.services.BackEndFuncs
import pregnaware.utils.Json4sSupport._
import spray.http.HttpMethods._
import spray.httpx.ResponseTransformation._

import scala.concurrent.{ExecutionContext, Future}

/** Client to the UserHttpService */
abstract class ProgressServiceBackend(progressServiceName: String) extends BackEndFuncs(progressServiceName) {

  def putProgress(userId: Int, dueDate: LocalDate) : Future[LocalDate] = {
    send(PUT, s"progress/$userId", (b,u) => b(u, dueDate)).map(r => r ~> unmarshal[LocalDate])
  }

  def deleteProgress(userId: Int) : Future[Unit] = {
    send(DELETE, s"progress/$userId").map(_ => ())
  }
}

object ProgressServiceBackend {
  def apply(progressServiceName: String)
      (implicit ac: ActorContext, ec: ExecutionContext, to: Timeout) : ProgressServiceBackend = {

    new ProgressServiceBackend(progressServiceName) {
      implicit override final def context: ActorContext = ac
      implicit override final def executor: ExecutionContext = ec
      implicit override final def timeout: Timeout = to
    }
  }
}