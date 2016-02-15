package utils

import akka.actor.ActorContext
import com.gettyimages.spray.swagger.SwaggerHttpService
import com.wordnik.swagger.model.ApiInfo
import utils.formats.SwaggerFormats

import scala.reflect.runtime.universe._

/** Wraps up the process of creating a swagger end-point */
object SwaggerServiceWrapper {

  /** Sets up the format converters and builds the Swagger end-point at /api-docs */
  def swaggerService(
    context : ActorContext,
    apiTypeList : Seq[Type],
    apiTitle: String,
    apiDescription: String) : SwaggerHttpService = {

    // Add the swagger formatting support BEFORE CREATING THE HTTP SERVICE
    // (as this parses and caches the API documentation)
    SwaggerFormats.addSwaggerSupport()

    new SwaggerHttpService {
      override def apiTypes = apiTypeList
      override def apiVersion = "2.0"
      override def baseUrl = "/"
      override def docsPath = "api-docs"
      override def actorRefFactory = context
      override def apiInfo = Some(new ApiInfo(
        apiTitle, apiDescription,
        "TOC Url",
        "james@jamesseymour.co.uk",
        "MIT",
        "https://tldrlegal.com/license/mit-license"))
    }
  }
}
