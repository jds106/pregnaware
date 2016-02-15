package utils

import java.net.InetSocketAddress

import akka.actor._
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import org.json4s._
import org.json4s.jackson.JsonMethods._
import spray.can.Http
import spray.http.HttpMethods._
import spray.http._

import scala.concurrent.Await
import scala.concurrent.duration._

/** Wraps interactions with Consul */
object ConsulWrapper extends StrictLogging {

  // Bring in the default serialization values
  import utils.Json4sSupport._

  private val ConsulHostname = "localhost"
  private val ConsulPort = 8500

  private case class ConsulService(
    ID: String, Service: String, Tags: Array[String], Address: String, Port: Int)

  /** Returns the address which this service should bind to */
  def getAddress(name: String)(implicit system: ActorSystem) : InetSocketAddress = {
    implicit val timeout: Timeout = 5.seconds

    val request = HttpRequest(GET, s"http://$ConsulHostname:$ConsulPort/v1/agent/services")
    val requestFuture = IO(Http).ask(request).mapTo[HttpResponse]
    val response = Await.result(requestFuture, 10.seconds)
    val responseBody = response.entity.data.asString
    val services = parse(responseBody)

    services \ name match {
      case json : JObject =>
        val svc = json.extract[ConsulService]
        new InetSocketAddress(svc.Address, svc.Port)

      case _ =>
        val available = services match {
          case JObject(list) =>
            list.map{ case (svcName, _) => svcName }
          case _ => List.empty[String]
        }
        throw new Exception(s"No service found - available: ${available.mkString(",")}")
    }
  }
}
