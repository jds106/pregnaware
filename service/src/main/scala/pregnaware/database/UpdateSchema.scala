package pregnaware.database

import com.typesafe.scalalogging.StrictLogging
import pregnaware.utils.AppConfig
import slick.codegen.SourceCodeGenerator

/** Simple tool to refresh the database schema code */
object UpdateSchema extends StrictLogging {

  case class Project(outputDir: String, pkg: String)

  def main(args: Array[String]): Unit = {

    val projects = Seq(
      Project("src/main/scala", "pregnaware.database.schema")
    )

    projects foreach { project =>
      val slickDriver = "slick.driver.MySQLDriver"
      val jdbcDriver  = "com.mysql.jdbc.Driver"
      val url         = AppConfig.getDatabaseUrl
      val user        = AppConfig.getDatabaseUser
      val password    = AppConfig.getDatabasePassword

      val projectDir = new java.io.File(project.outputDir)
      logger.info(s"Generating database schema for project ${project.pkg} to ${projectDir.getAbsoluteFile}...")

      SourceCodeGenerator.main(
        Array(slickDriver, jdbcDriver, url, project.outputDir, project.pkg, user, password))
    }

    logger.info("Complete")
  }
}