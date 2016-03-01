package pregnaware.database.wrappers

import java.sql.Date
import java.time.LocalDate

import pregnaware.database.ConnectionManager._
import pregnaware.database.schema.Tables._
import pregnaware.naming.NamingPersistence
import pregnaware.naming.entities.WrappedBabyName
import slick.driver.MySQLDriver.api._

import scala.concurrent.{ExecutionContext, Future}

trait BabyNameWrapper extends NamingPersistence {

  /** The list of current baby names */
  def getNames(userId : Int) : Future[Seq[WrappedBabyName]] = {
    connection { db =>
      val joinQuery = (Babyname join User).on(_.suggestedby === _.id).filter{ case (b, u) => b.userid === userId }

      db.run(joinQuery.result).map { results =>
        results.map {
          case (b, u) => WrappedBabyName(
            b.id, b.userid, b.suggestedby, u.displayname, u.joindate.toLocalDate, b.name, b.isboy)
        }
      }
    }
  }

  /** Add a baby name */
  def addName(userId: Int, suggestedById: Int, name: String, isBoy: Boolean) : Future[WrappedBabyName] = {
    connection { db =>
      val suggestedUserQuery = User.filter(_.id === suggestedById)
      db.run(suggestedUserQuery.result.headOption).flatMap {
        case None => throw new Exception(s"Cannot add a baby name to an unknown user: $suggestedById")

        case Some(suggestedUser) =>
          val insertQuery = Babyname returning Babyname.map(_.id) into ((user, id) => user.copy(id = id))
          val action = insertQuery += BabynameRow(-1, userId, name, isBoy, suggestedById, Date.valueOf(LocalDate.now()))

          db.run(action).map { row =>
            WrappedBabyName(
              row.id, row.userid, row.suggestedby, suggestedUser.displayname, row.date.toLocalDate, row.name, row.isboy)
          }
      }
    }
  }

  /** Delete a baby name */
  def deleteName(userId: Int, babyNameId: Int) : Future[Unit] = {
    connection { db =>
      val deletion = Babyname.filter(_.userid === userId).filter(_.id === babyNameId)
      db.run(deletion.delete).map {
        case 1 => ()
        case n => throw new Exception(s"Deleted $n baby names with id $babyNameId")
      }
    }
  }
}
