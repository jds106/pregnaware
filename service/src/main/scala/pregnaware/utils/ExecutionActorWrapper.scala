package pregnaware.utils

import akka.actor.ActorContext

/** An additional implicit context for actor-related execution */
trait ExecutionActorWrapper extends ExecutionWrapper {
  implicit def context: ActorContext
}
