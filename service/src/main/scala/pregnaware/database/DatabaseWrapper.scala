package pregnaware.database

import akka.util.Timeout
import pregnaware.database.wrappers.{BabyNameWrapper, SessionWrapper, UserWrapper}

import scala.concurrent.ExecutionContext

/** Wraps up the required database interactions */
trait DatabaseWrapper
  extends SessionWrapper
  with UserWrapper
  with BabyNameWrapper

object DatabaseWrapper {
  def apply(implicit ec: ExecutionContext, to: Timeout) : DatabaseWrapper = {

    new DatabaseWrapper {
      implicit override final def executor: ExecutionContext = ec
      implicit override final def timeout: Timeout = to
    }
  }
}