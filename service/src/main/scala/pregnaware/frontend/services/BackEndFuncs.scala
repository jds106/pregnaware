package pregnaware.frontend.services

import spray.client.pipelining._
import spray.http.Uri.{Authority, Host, Path}
import spray.http.{HttpMethod, HttpRequest, HttpResponse, Uri}
import spray.httpx.RequestBuilding.RequestBuilder
import pregnaware.utils.ConsulWrapper._
import pregnaware.utils.ExecutionActorWrapper

import scala.concurrent.Future

/** Common functions required by the clients of the back-end */
abstract class BackEndFuncs(serviceName: String) extends ExecutionActorWrapper {

  /** Sends a request with no content to the server */
  def send(method: HttpMethod, requestPath: String): Future[HttpResponse] = {
    send(method, requestPath, (builder, uri) => builder(uri))
  }

  /* Sends the content request to the server */
  def send[T](
    method: HttpMethod,
    requestPath: String,
    buildRequest: (RequestBuilder, Uri) => HttpRequest): Future[HttpResponse] = {

    send(requestPath, uri => buildRequest(new RequestBuilder(method), uri))
  }

  /* Sends the content request to the server (supporting specific HTTP Request construction) */
  def send[T](
    requestPath: String,
    buildRequest: (Uri) => HttpRequest): Future[HttpResponse] = {

    getAddress(serviceName).flatMap { address =>
      val requestUri = Uri(
        scheme = "http",
        authority = Authority(Host(address.getHostName), address.getPort),
        path = Path(s"/$serviceName/$requestPath")
      )

      buildRequest(requestUri) ~> sendReceive
    }
  }
}
