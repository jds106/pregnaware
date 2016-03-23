package pregnaware.database.wrappers

import java.sql.Date
import java.time.LocalDate

import pregnaware.database.ConnectionManager._
import pregnaware.database.schema.Tables._
import pregnaware.naming.NamingPersistence
import pregnaware.naming.entities._
import slick.driver.MySQLDriver.api._

import scala.concurrent.Future

trait BabyNameWrapper extends NamingPersistence {

  /** The list of current baby names */
  def getNames(userId : Int) : Future[Seq[WrappedBabyName]] = {
    connection("GetBabyNames") { db =>
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
    connection("AddBabyName") { db =>
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
    connection("DeleteBabyName") { db =>
      val deletion = Babyname.filter(_.userid === userId).filter(_.id === babyNameId)
      db.run(deletion.delete).map {
        case 1 => ()
        case n => throw new Exception(s"Deleted $n baby names with id $babyNameId")
      }
    }
  }

  /** Gets the top _limit_ of baby names for the specified year */
  def getNameStats(year: Int, isBoy: Boolean, limit : Int) : Future[Seq[NameStat]] = {
    connection("GetNameStats") { db =>
      val gender = toGender(isBoy)
      val query = Namestat.filter(_.year === year).filter(_.gender === gender).sortBy(_.count.desc).take(limit)

      db.run(query.result).map(rows => rows.map(r =>
        NameStat(capitaliseName(r.name), isBoyFromGender(r.gender), r.year, r.count)))
    }
  }

  /** Gets all of the information on the specified name */
  def getNameStats(name: String, isBoy: Boolean) : Future[Seq[NameStat]] = {
    connection(s"GetNameStatsFor_$name") { db =>
      val gender = toGender(isBoy)
      val query = Namestat.filter(_.name === name).filter(_.gender === gender).sortBy(_.year.desc)

      db.run(query.result).map(rows =>
        rows.map(r => NameStat(capitaliseName(r.name), isBoyFromGender(r.gender), r.year, r.count)))
    }
  }

  /** Gets the top 100 baby names for the specified year */
  def getNameStatsByCountry(year: Int, isBoy: Boolean) : Future[Seq[NameStatByCountry]] = {
    connection("GetTop10NameStatsByCountry") { db =>
      val gender = toGender(isBoy)
      val query = Namestatbycountry.filter(_.year === year).filter(_.gender === gender).sortBy(_.count.desc)
      db.run(query.result).map(rows => rows.map(r =>
        NameStatByCountry(capitaliseName(r.name), isBoyFromGender(r.gender), r.year, r.country, r.count)))
    }
  }

  /** Gets all of the information on the specified name */
  def getNameStatsByCountry(name: String, isBoy: Boolean) : Future[Seq[NameStatByCountry]] = {
    connection(s"GetNameStatsByCountryFor_$name") { db =>
      val gender = toGender(isBoy)
      val query = Namestatbycountry.filter(_.name === name).filter(_.gender === gender).sortBy(_.year.desc)
      db.run(query.result).map(rows => rows.map(r =>
        NameStatByCountry(capitaliseName(r.name), isBoyFromGender(r.gender), r.year, r.country, r.count)))
    }
  }

  /** Gets the top 10 baby names for the specified year */
  def getTop10NameStatsByRegion(year: Int, isBoy: Boolean) : Future[Seq[NameStatByRegion]] = {
    connection("GetTop10NameStatsByRegion") { db =>
      val gender = toGender(isBoy)
      val query = Namestatbyregion.filter(_.year === year).filter(_.gender === gender).sortBy(_.count.desc)
      db.run(query.result).map(rows => rows.map(r =>
        NameStatByRegion(capitaliseName(r.name), isBoyFromGender(r.gender), r.year, r.region, r.count)))
    }
  }

  /** Gets all of the information on the specified name */
  def getNameStatsByRegion(name: String, isBoy: Boolean) : Future[Seq[NameStatByRegion]] = {
    connection(s"GetNameStatsByRegionFor_$name") { db =>
      val gender = toGender(isBoy)
      val query = Namestatbyregion.filter(_.name === name).filter(_.gender === gender).sortBy(_.year.desc)
      db.run(query.result).map(rows => rows.map(r =>
        NameStatByRegion(capitaliseName(r.name), isBoyFromGender(r.gender), r.year, r.region, r.count)))
    }
  }

  /** Gets the top 10 baby names for the specified year */
  def getTop10NameStatsByMonth(year: Int, isBoy: Boolean) : Future[Seq[NameStatByMonth]] = {
    connection("GetTop10NameStatsByMonth") { db =>
      val gender = toGender(isBoy)
      val query = Namestatbymonth.filter(_.year === year).filter(_.gender === gender).sortBy(_.count.desc)
      db.run(query.result).map(rows => rows.map(r =>
        NameStatByMonth(capitaliseName(r.name), isBoyFromGender(r.gender), r.year, r.month, r.count)))
    }
  }

  /** Gets all of the information on the specified name */
  def getNameStatsByMonth(name: String, isBoy: Boolean) : Future[Seq[NameStatByMonth]] = {
    connection(s"GetNameStatsByMonthFor_$name") { db =>
      val gender = toGender(isBoy)
      val query = Namestatbymonth.filter(_.name === name).filter(_.gender === gender).sortBy(_.year.desc)
      db.run(query.result).map(rows => rows.map(r =>
        NameStatByMonth(capitaliseName(r.name), isBoyFromGender(r.gender), r.year, r.month, r.count)))
    }
  }

  /** The number of babies born in each year (a slight understatement as babies given unique names are not counted) */
  def getNumBabies : Future[Seq[NameSummaryStat]] = {
    connection("GetNumBabies") { db =>
      val query = Namestat.groupBy(r => (r.year, r.gender)).map(r => (r._1._1, r._1._2, r._2.map(_.count).sum))
      db.run(query.result).map(rows => rows.map {
        case (year, gender, num) => NameSummaryStat(year, isBoyFromGender(gender), num.getOrElse(0))
      })
    }
  }

  private def isBoyFromGender(gender: String) = {
    "boys" == gender
  }

  private def toGender(isBoy: Boolean) = {
    if (isBoy) "boys" else "girls"
  }

  private def capitaliseName(name: String) = {
    if (name.length < 2) {
      name
    } else {
      s"${name.charAt(0).toUpper}${name.substring(1).toLowerCase}"
    }
  }
}
