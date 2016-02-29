package naming

import naming.entities.WrappedBabyName

import scala.concurrent.Future

/**
  * Created by james on 28/02/2016.
  */
trait NamingPersistence {
  /** The list of current baby names */
  def getNames(userId : Int) : Future[Seq[WrappedBabyName]]

  /** Add a baby name */
  def addName(userId: Int, suggestedById: Int, name: String, isBoy: Boolean) : Future[WrappedBabyName]

  /** Delete a baby name */
  def deleteName(userId: Int, babyNameId: Int) : Future[Unit]
}
