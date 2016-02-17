package utils

import java.net.InetSocketAddress

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import org.json4s._
import org.json4s.jackson.JsonMethods._
import spray.client.pipelining._
import spray.http._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/** Wraps interactions with Consul */
object ConsulWrapper extends StrictLogging {

  // Bring in the default serialization values
  import utils.Json4sSupport._

  private val ConsulHostname = "localhost"
  private val ConsulPort = 8500

  private case class ConsulService(
    ID: String, Service: String, Tags: Array[String], Address: String, Port: Int)

  private implicit val timeout: Timeout = 5.seconds

  /** Returns the address of the requested service (blocking) */
  def getAddress(name: String)(implicit httpIO: ActorRef, ex: ExecutionContext): InetSocketAddress = {
    val response = Await.result(sendRequest, timeout.duration)
    parseResponse(response, name)
  }

  /** Non-blocking call to get the requested service address */
  def getAddressAsync(name: String)(implicit httpIO: ActorRef, ex: ExecutionContext): Future[InetSocketAddress] = {

    sendRequest.map { response =>
      val responseBody = response.entity.data.asString
      val services = parse(responseBody)

      services \ name match {
        case json: JObject =>
          val svc = json.extract[ConsulService]
          new InetSocketAddress(svc.Address, svc.Port)

        case _ =>
          val available = services match {
            case JObject(list) =>
              list.map { case (svcName, _) => svcName }
            case _ => List.empty[String]
          }
          throw new Exception(s"No service found - available: ${available.mkString(",")}")
      }
    }
  }

  /** Sends the request asynchronously */
  private def sendRequest(implicit httpIO: ActorRef, ex: ExecutionContext): Future[HttpResponse] = {
    val request = Get(s"http://$ConsulHostname:$ConsulPort/v1/agent/services")
    httpIO.ask(request).mapTo[HttpResponse]
  }

  /** Parses the response to a service query from Consul */
  private def parseResponse(response: HttpResponse, name: String) : InetSocketAddress = {
    val responseBody = response.entity.data.asString
    val services = parse(responseBody)

    services \ name match {
      case json: JObject =>
        val svc = json.extract[ConsulService]
        new InetSocketAddress(svc.Address, svc.Port)

      case _ =>
        val available = services match {
          case JObject(list) =>
            list.map { case (svcName, _) => svcName }
          case _ => List.empty[String]
        }
        throw new Exception(s"No service found - available: ${available.mkString(",")}")
    }
  }
}
