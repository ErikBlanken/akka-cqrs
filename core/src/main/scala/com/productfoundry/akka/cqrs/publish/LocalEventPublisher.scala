package com.productfoundry.akka.cqrs.publish

import com.productfoundry.akka.cqrs.Aggregate

/**
 * Mixin for actors to publish all commit messages onto the system event stream.
 */
trait LocalEventPublisher extends EventPublisher {
  this: Aggregate =>

  /**
    * Handles publication.
    * @param eventPublication to publish.
    */
  override def publishEvent(eventPublication: EventPublication): Unit = {
    context.system.eventStream.publish(eventPublication)
  }
}