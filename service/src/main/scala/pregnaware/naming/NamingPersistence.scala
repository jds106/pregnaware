package pregnaware.naming

import pregnaware.naming.entities._
import pregnaware.utils.ExecutionWrapper

import scala.concurrent.Future

trait NamingPersistence extends ExecutionWrapper {
  /** The list of current baby names */
  def getNames(userId : Int) : Future[Seq[WrappedBabyName]]

  /** Add a baby name */
  def addName(userId: Int, suggestedById: Int, name: String, isBoy: Boolean) : Future[WrappedBabyName]

  /** Delete a baby name */
  def deleteName(userId: Int, babyNameId: Int) : Future[Unit]

  /** Gets the top _limit_ of baby names for the specified year */
  def getNameStats(year: Int, isBoy: Boolean, limit : Int) : Future[Seq[NameStat]]

  /** Gets all of the information on the specified name */
  def getNameStats(name: String, isBoy: Boolean) : Future[Seq[NameStat]]

  /** Gets the top 10 baby names for the specified year */
  def getTop10NameStatsByCountry(year: Int, isBoy: Boolean) : Future[Seq[NameStatByCountry]]

  /** Gets all of the information on the specified name */
  def getNameStatsByCountry(name: String, isBoy: Boolean) : Future[Seq[NameStatByCountry]]

  /** Gets the top 10 baby names for the specified year */
  def getTop10NameStatsByRegion(year: Int, isBoy: Boolean) : Future[Seq[NameStatByRegion]]

  /** Gets all of the information on the specified name */
  def getNameStatsByRegion(name: String, isBoy: Boolean) : Future[Seq[NameStatByRegion]]

  /** Gets the top 10 baby names for the specified year */
  def getTop10NameStatsByMonth(year: Int, isBoy: Boolean) : Future[Seq[NameStatByMonth]]

  /** Gets all of the information on the specified name */
  def getNameStatsByMonth(name: String, isBoy: Boolean) : Future[Seq[NameStatByMonth]]

  /** The number of babies born in this year (a slight understatement as babies given unique names are not counted) */
  def getNumBabies : Future[Seq[NameSummaryStat]]

  /** The years with data */
  def getAvailableYears : Future[Seq[Int]]
}
