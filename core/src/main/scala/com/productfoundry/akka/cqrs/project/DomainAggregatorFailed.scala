package com.productfoundry.akka.cqrs.project

import com.productfoundry.akka.cqrs.{DomainError, AggregateRevision}

// TODO [AK] Can be removed when commit aggregator is refactored.
case class DomainAggregatorFailed(revision: AggregateRevision) extends DomainError
