package utils

import java.net.InetSocketAddress

import akka.actor.ActorRefFactory
import com.typesafe.scalalogging.StrictLogging
import spray.client.pipelining._
import utils.Json4sSupport._

import scala.concurrent.{ExecutionContext, Future}

/** Wraps interactions with Consul */
object ConsulWrapper extends StrictLogging {

  private val ConsulHostname = "localhost"
  private val ConsulPort = 8500

  private case class ConsulService(
    ID: String, Service: String, Tags: Array[String], Address: String, Port: Int)

  /** Non-blocking call to get the requested service address */
  def getAddress(name: String)(implicit executor: ExecutionContext, refFactory: ActorRefFactory): Future[InetSocketAddress] = {

    val request = Get(s"http://$ConsulHostname:$ConsulPort/v1/agent/services")

    (request ~> sendReceive)
      .map(r => r ~> unmarshal[Seq[ConsulService]])
      .map { services =>
        services.find(_.Service == name) match {
          case Some(svc) => new InetSocketAddress(svc.Address, svc.Port)
          case None => throw new Exception(s"No service found - available: ${services.map(_.Service).mkString(",")}")
        }
      }
  }
}

