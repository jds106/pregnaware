package frontend.services

import akka.actor.{ActorContext, ActorRefFactory}
import spray.client.pipelining._
import spray.http.Uri.{Authority, Host, Path}
import spray.http.{HttpMethod, HttpRequest, HttpResponse, Uri}
import spray.httpx.RequestBuilding.RequestBuilder
import utils.ConsulWrapper._
import utils.ExecutionWrapper

import scala.concurrent.{ExecutionContext, Future}

/** Common functions required by the clients of the back-end */
abstract class BackEndFuncs(serviceName: String) extends ExecutionWrapper {

  /** Sends a request with no content to the server */
  def send(method: HttpMethod, requestPath: String) : Future[HttpResponse] = {
    send(method, requestPath, (builder, uri) => builder(uri))
  }

  /* Sends the content request to the server */
  def send[T](
    method: HttpMethod,
    requestPath: String,
    buildRequest: (RequestBuilder, Uri) => HttpRequest) : Future[HttpResponse] = {

    getAddress(serviceName).flatMap { address =>

      val requestUri = Uri(
        scheme = "http",
        authority = Authority(Host(address.getHostName), address.getPort),
        path = Path(requestPath)
      )

      buildRequest(new RequestBuilder(method), requestUri) ~> sendReceive
    }
  }
}
