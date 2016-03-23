package pregnaware.frontend.services.naming

import akka.actor.ActorContext
import akka.util.Timeout
import pregnaware.frontend.services.BackEndFuncs
import pregnaware.naming.entities._
import spray.http.HttpMethods._
import spray.httpx.ResponseTransformation._
import pregnaware.utils.Json4sSupport._

import scala.concurrent.{ExecutionContext, Future}

/** Client to the UserHttpService */
abstract class NamingServiceBackend(namingServiceName: String) extends BackEndFuncs(namingServiceName) {

  def getNames(userId: Int) : Future[Seq[WrappedBabyName]] = {
    send(GET, s"names/$userId").map( r => r ~> unmarshal[Seq[WrappedBabyName]])
  }

  def putName(
    suggestedByUserId: Int, suggestedForUserId: Int, name: String, isBoy: Boolean) : Future[WrappedBabyName] = {

    val addNameRequest = AddNameRequest(suggestedByUserId, name, isBoy)
    send(PUT, s"names/$suggestedForUserId", (b,u) => b(u, addNameRequest)).map(r => r ~> unmarshal[WrappedBabyName])
  }

  def deleteName(userId: Int, babyNameId: Int) : Future[Unit] = {
    send(DELETE, s"names/$userId/$babyNameId").map (_ => ())
  }

  def getNameStatsCount : Future[Seq[NameSummaryStat]] = {
    send(GET, s"namestats/meta/count").map( r => r ~> unmarshal[Seq[NameSummaryStat]])
  }

  def getNameStatsComplete(name: String, gender: String) : Future[Seq[NameStat]] = {
    send(GET, s"namestats/data/$gender/complete/name/$name").map( r => r ~> unmarshal[Seq[NameStat]])
  }

  def getNameStatsComplete(year: Int, gender: String) : Future[Seq[NameStat]] = {
    send(GET, s"namestats/data/$gender/complete/summary/$year").map( r => r ~> unmarshal[Seq[NameStat]])
  }

  def getNameStatsByCountry(name: String, gender: String) : Future[Seq[NameStatByCountry]] = {
    send(GET, s"namestats/data/$gender/country/name/$name").map( r => r ~> unmarshal[Seq[NameStatByCountry]])
  }

  def getNameStatsByCountry(year: Int, gender: String) : Future[Seq[NameStatByCountry]] = {
    send(GET, s"namestats/data/$gender/country/summary/$year").map( r => r ~> unmarshal[Seq[NameStatByCountry]])
  }

  def getNameStatsByMonth(name: String, gender: String) : Future[Seq[NameStatByMonth]] = {
    send(GET, s"namestats/data/$gender/month/name/$name").map( r => r ~> unmarshal[Seq[NameStatByMonth]])
  }

  def getNameStatsByMonth(year: Int, gender: String) : Future[Seq[NameStatByMonth]] = {
    send(GET, s"namestats/data/$gender/month/summary/$year").map( r => r ~> unmarshal[Seq[NameStatByMonth]])
  }

  def getNameStatsByRegion(name: String, gender: String) : Future[Seq[NameStatByRegion]] = {
    send(GET, s"namestats/data/$gender/region/name/$name").map( r => r ~> unmarshal[Seq[NameStatByRegion]])
  }

  def getNameStatsByRegion(year: Int, gender: String) : Future[Seq[NameStatByRegion]] = {
    send(GET, s"namestats/data/$gender/region/summary/$year").map( r => r ~> unmarshal[Seq[NameStatByRegion]])
  }
}

object NamingServiceBackend {
  def apply(namingServiceName: String)
    (implicit ac: ActorContext, ec: ExecutionContext, to: Timeout) : NamingServiceBackend = {

    new NamingServiceBackend(namingServiceName) {
      implicit override final def context: ActorContext = ac
      implicit override final def executor: ExecutionContext = ec
      implicit override final def timeout: Timeout = to
    }
  }
}