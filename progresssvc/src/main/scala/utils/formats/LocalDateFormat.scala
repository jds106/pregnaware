package utils.formats

import java.time.LocalDate

import com.wordnik.swagger.converter.{ModelConverters, OverrideConverter}
import com.wordnik.swagger.model.{Model, ModelProperty}
import org.json4s._

import scala.collection.mutable

class LocalDateFormat extends Serializer[LocalDate] {
  val LocalDateClass = classOf[LocalDate]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), LocalDate] = {
    case (TypeInfo(LocalDateClass, _), localDateObj) => localDateObj match {
      case (JObject(JField("year", JInt(y)) :: JField("month", JInt(m)) :: JField("day", JInt(d)) :: Nil)) =>
        LocalDate.of(y.toInt, m.toInt, d.toInt)
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case x: LocalDate => JObject(
      JField("year", JInt(x.getYear)),
      JField("month", JInt(x.getMonthValue)),
      JField("day", JInt(x.getDayOfMonth)))
  }
}

object LocalDateFormat extends SwaggerFormat {
  /** Add a LocalDate Swagger property converter */
  override def addSwaggerSupport() : Unit = {
    val props = Seq(
      "year" -> ModelProperty(classOf[Int].getSimpleName, classOf[Int].getCanonicalName),
      "month" -> ModelProperty(classOf[Int].getSimpleName, classOf[Int].getCanonicalName),
      "day" -> ModelProperty(classOf[Int].getSimpleName, classOf[Int].getCanonicalName))

    val modelProperties = mutable.LinkedHashMap[String, ModelProperty]()
    props foreach { kv => modelProperties += kv }

    val model: Model = new Model(
      id = classOf[LocalDate].getSimpleName,
      name = classOf[LocalDate].getSimpleName,
      qualifiedType = classOf[LocalDate].getCanonicalName,
      properties = modelProperties
    )

    val converter = new OverrideConverter
    converter.add(classOf[LocalDate].getName, model)
    ModelConverters.addConverter(converter, first = true)
  }
}
