package com.productfoundry.akka.cqrs

/**
 * Event that is persisted and applied to an aggregate.
 *
 * @param headers with info about the aggregate related to the event.
 * @param event with the actual change.
 */
case class AggregateEventRecord(headers: AggregateEventHeaders, event: AggregateEvent)
