package utils

import com.typesafe.config.ConfigFactory

/** The single accessor for application config */
object AppConfig {
  val conf = ConfigFactory.load()

  def getDatabaseUrl() = conf.getString("app.database.dev.url")
  def getDatabaseUser() = conf.getString("app.database.dev.user")
  def getDatabasePassword() = conf.getString("app.database.dev.password")
}
