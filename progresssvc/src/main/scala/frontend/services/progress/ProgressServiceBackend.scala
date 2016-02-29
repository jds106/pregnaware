package frontend.services.progress

import java.time.LocalDate

import akka.actor.ActorContext
import akka.util.Timeout
import frontend.services.BackEndFuncs
import progress.ProgressModel
import spray.http.HttpMethods._
import spray.httpx.ResponseTransformation._
import utils.Json4sSupport._

import scala.concurrent.{ExecutionContext, Future}

/** Client to the UserHttpService */
abstract class ProgressServiceBackend(progressServiceName: String) extends BackEndFuncs(progressServiceName) {

  def getProgress(userId: Int) : Future[ProgressModel] = {
    send(GET, s"/progress/$userId").map(r => r ~> unmarshal[ProgressModel])
  }

  def putProgress(userId: Int, dueDate: LocalDate) : Future[ProgressModel] = {
    send(PUT, s"/progress/$userId", (b,u) => b(u, dueDate)).map(r => r ~> unmarshal[ProgressModel])
  }

  def deleteProgress(userId: Int) : Future[Unit] = {
    send(DELETE, s"/progress/$userId").map(_ => ())
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