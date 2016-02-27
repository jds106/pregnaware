package database.wrappers

import database.ConnectionManager._
import database.entities.WrappedBabyName
import database.schema.Tables._
import slick.driver.MySQLDriver.api._

import scala.concurrent.{ExecutionContext, Future}

/** Baby name functions */
trait BabyNameWrapper {

  implicit def executionContext: ExecutionContext

  def getNames(userId : Int) : Future[Seq[WrappedBabyName]] = {
    connection { db =>
      val joinQuery = (Babyname join User).on(_.suggestedby === _.id).filter{ case (b, u) => b.userid === userId }

      db.run(joinQuery.result).map { results =>
        results.map {
          case (b, u) => WrappedBabyName(b.id, b.userid, b.suggestedby, u.displayname, b.name, b.isboy)
        }
      }
    }
  }

  def addName(userId: Int, suggestedById: Int, name: String, isBoy: Boolean) : Future[BabynameRow] = {
    connection { db =>
      val insertQuery = Babyname returning Babyname.map(_.id) into ((user, id) => user.copy(id = id))
      val action = insertQuery += BabynameRow(-1, userId, name, isBoy, suggestedById)
      db.run(action)
    }
  }

  def deleteName(babyNameId: Int) : Future[Boolean] = {
    connection { db =>
      val deletion = Babyname.filter(_.id === babyNameId)
      db.run(deletion.delete).map {
        case 0 => false
        case 1 => true
        case _ => throw new Exception(s"Deleted more than one baby name with id $babyNameId")
      }
    }
  }
}
