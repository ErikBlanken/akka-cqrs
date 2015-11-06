package com.productfoundry.akka.cqrs

import java.util.UUID

import com.productfoundry.akka.PassivationConfig
import com.productfoundry.akka.cqrs.UserCommand.{NotifyUser, CreateUser}
import com.productfoundry.akka.cqrs.UserEvent.{UserNotified, UserCreated}

case class UserId(entityId: String) extends EntityId {
  override def toString: String = entityId
}

object UserId {
  def generate = UserId(UUID.randomUUID().toString)
}

trait UserMessage extends AggregateMessage {
  type Id = UserId
}

object UserCommand {

  sealed trait UserCommand extends UserMessage with AggregateCommand

  case class CreateUser(id: UserId, name: String) extends UserCommand

  case class NotifyUser(id: UserId, notification: String) extends UserCommand
}

object UserEvent {

  sealed trait UserEvent extends UserMessage with AggregateEvent

  case class UserCreated(id: UserId, name: String) extends UserEvent

  case class UserNotified(id: UserId, notification: String) extends UserEvent
}

class UserAggregate(override val passivationConfig: PassivationConfig) extends Aggregate {

  type S = UserState

  override val factory: StateModifications = {
    case UserCreated(_, name) => UserState(name)
  }

  case class UserState(name: String) extends AggregateState {

    override def update: StateModifications = {
      case _ => this
    }
  }

  override def handleCommand: Receive = {

    case CreateUser(userId, name) =>
      tryCommit(Right(Changes(UserCreated(userId, name))))

    case NotifyUser(userId, notification) =>
      tryCommit(Right(Changes(UserNotified(userId, notification))))
  }
}
