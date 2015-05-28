package com.productfoundry.akka.cqrs.project

import akka.actor.{Terminated, PoisonPill, Props}
import com.productfoundry.akka.cqrs.project.DomainAggregator.DomainAggregatorRevision
import com.productfoundry.akka.cqrs.{AggregateRevision, Commit, TestId}
import com.productfoundry.support.PersistenceTestSupport

class DomainAggregatorSpec extends PersistenceTestSupport {

  val snapshotInterval = 10

  "Domain aggregator" must {

    "persist commit" in new fixture {
      subject ! createCommit(1)

      expectMsg(DomainAggregatorRevision(DomainRevision(1L)))
    }

    "recover revision without snapshots" in {
      testRecovery(snapshotInterval - 1)
    }

    "recover revision with snapshots" in {
      testRecovery(snapshotInterval * 10)
    }

    def testRecovery(count: Int): Unit = new fixture {
      1 to count foreach { revision =>
        subject ! createCommit(revision)
        expectMsg(DomainAggregatorRevision(DomainRevision(revision.toLong)))
      }

      watch(subject)
      subject ! PoisonPill
      expectMsgType[Terminated]

      val recovered = system.actorOf(domainAggregatorProps)
      recovered ! createCommit(1)
      expectMsgType[DomainAggregatorRevision].revision should be(DomainRevision(count + 1L))
    }
  }

  trait fixture extends {
    val persistenceId = TestId.generate().toString

    val domainAggregatorProps = Props(new DomainAggregator(persistenceId, snapshotInterval))

    val subject = system.actorOf(domainAggregatorProps)

    def createCommit(revision: Int) = new Commit(AggregateRevision(revision.toLong), Seq.empty, System.currentTimeMillis())
  }
}