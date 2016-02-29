package pregnaware.naming

import pregnaware.naming.entities.WrappedBabyName
import pregnaware.utils.ExecutionWrapper

import scala.concurrent.Future

trait NamingPersistence extends ExecutionWrapper {
  /** The list of current baby names */
  def getNames(userId : Int) : Future[Seq[WrappedBabyName]]

  /** Add a baby name */
  def addName(userId: Int, suggestedById: Int, name: String, isBoy: Boolean) : Future[WrappedBabyName]

  /** Delete a baby name */
  def deleteName(userId: Int, babyNameId: Int) : Future[Unit]
}
