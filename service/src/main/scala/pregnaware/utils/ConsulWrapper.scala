package pregnaware.utils

import java.net.InetSocketAddress

import akka.actor.ActorRefFactory
import com.typesafe.scalalogging.StrictLogging
import org.json4s._
import org.json4s.jackson.JsonMethods._
import spray.client.pipelining._
import pregnaware.utils.Json4sSupport._

import scala.concurrent.{ExecutionContext, Future}

/** Wraps interactions with Consul */
object ConsulWrapper extends StrictLogging {

  // The request object sent to Consul to retrieve the list of known services
  private val request = Get(s"http://${AppConfig.getConsulAddress}/v1/agent/services")

  private case class ConsulService(
    ID: String, Service: String, Tags: Array[String], Address: String, Port: Int)

  /** Non-blocking call to get the requested service address */
  def getAddress(name: String)(implicit executor: ExecutionContext, refFactory: ActorRefFactory)
    : Future[InetSocketAddress] = {

    logger.info(s"Resolving service $name")

    (request ~> sendReceive)
      .map { response =>
        val responseBody = response.entity.data.asString
        val services = parse(responseBody)

        services \ name match {
          case json: JObject =>
            val svc = json.extract[ConsulService]
            logger.info(s"Resolved service $name -> $svc")
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
}

