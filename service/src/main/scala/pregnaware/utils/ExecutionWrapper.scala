package pregnaware.utils

import akka.util.Timeout

import scala.concurrent.ExecutionContext

/** Implicits needed for many of the future / HTTP request methods */
trait ExecutionWrapper {
  implicit def executor: ExecutionContext
  implicit def timeout: Timeout
}

