package pregnaware.utils.formats

import com.wordnik.swagger.converter.ModelConverters

/** Configure the appropriate Swagger formats */
object SwaggerFormats {
  val formats = Seq(LocalDateFormat)

  def addSwaggerSupport() : Unit = {
    // Remove the old Joda converters (as these only confuse the java.time.* mappings)
    val convertersToRemove = ModelConverters.converters.filter(_.getClass.getSimpleName.startsWith("Joda"))
    convertersToRemove.foreach(ModelConverters.removeConverter)

    // Add each of the custom Swagger supporter types
    formats.foreach(_.addSwaggerSupport())
  }
}

/** Swagger formatters extend this type to enable their support to be added */
trait SwaggerFormat {
  def addSwaggerSupport() : Unit
}