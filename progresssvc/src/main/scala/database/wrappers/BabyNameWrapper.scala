package database.wrappers

import database.ConnectionManager._
import database.schema.Tables._
import slick.driver.MySQLDriver.api._

import scala.concurrent.{ExecutionContext, Future}

/** Baby name functions */
trait BabyNameWrapper {

  implicit def executionContext: ExecutionContext

  def getNames(userId : Int) : Future[Seq[BabynameRow]] = {
    connection { db =>
      db.run(Babyname.filter(_.userid === userId).result)
    }
  }

  //def addName(userId Int, suggestedById: Int, )
}
