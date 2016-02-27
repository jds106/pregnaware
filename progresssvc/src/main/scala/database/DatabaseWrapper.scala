package database

import database.wrappers.{BabyNameWrapper, ProgressWrapper, SessionWrapper, UserWrapper}

import scala.concurrent.ExecutionContext

/** Wraps up the required database interactions */
case class DatabaseWrapper(ec: ExecutionContext)
  extends SessionWrapper
  with UserWrapper
  with ProgressWrapper
  with BabyNameWrapper {

  implicit def executionContext: ExecutionContext = ec

}
