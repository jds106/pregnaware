package pregnaware.utils

import com.typesafe.config.ConfigFactory

/** The single accessor for application config */
object AppConfig {
  val conf = ConfigFactory.load()

  def getConsulAddress : String =
    s"${conf.getString("consul.hostname")}:${conf.getString("consul.port")}"

  def getDatabaseUrl : String =
    conf.getString("app.database.dev.url")

  def getDatabaseUser : String =
    conf.getString("app.database.dev.user")

  def getDatabasePassword : String =
    conf.getString("app.database.dev.password")
}
