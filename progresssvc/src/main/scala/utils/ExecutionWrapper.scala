package utils

import akka.actor.ActorContext
import akka.util.Timeout

import scala.concurrent.ExecutionContext

/** These implicits are needed for many of the future / HTTP request methods */
trait ExecutionWrapper {
  implicit def context: ActorContext
  implicit def executor: ExecutionContext
  implicit def timeout: Timeout
}
