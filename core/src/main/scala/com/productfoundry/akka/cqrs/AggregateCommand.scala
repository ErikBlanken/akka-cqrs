package com.productfoundry.akka.cqrs

/**
 * Base command marker trait.
 */
trait AggregateCommand extends AggregateMessage {

  /**
   * Commands do not require a revision check by default.
   * @return indication if a revision check is required in order to process the command.
   */
  def isRevisionCheckRequired: Boolean = false
}

/**
 * Trait to force revision checks for certain commands.
 */
trait RequiredRevisionCheck {
  self: AggregateCommand =>

  /**
   * @return Indication that revision check is required.
   */
  override def isRevisionCheckRequired: Boolean = true
}

/**
 * Requests a command with additional info for the aggregate.
 */
trait CommandRequest extends AggregateMessage {

  /**
   * @return the command to execute.
   */
  def command: AggregateCommand

  /**
   * Creates a new command request with the expected revision.
   * @param expected revision for the aggregate.
   * @return command request with expected revision.
   */
  def withExpectedRevision(expected: AggregateRevision): CommandRequest

  /**
   * Check if the command is valid for the actual revision.
   * @param actual revision of the aggregate.
   * @param success executed when the revision check passed.
   * @param failed is executed when the revision check fails.
   * @return True when the revision is correct or expected is empty.
   */
  def checkRevision(actual: AggregateRevision)(success: () => Unit)(failed: (AggregateRevision) => Unit): Unit

  /**
   * Specifies headers to store with the event.
   * @param headers to store.
   * @return command request with updated headers.
   */
  def withHeaders(headers: CommitHeaders): CommandRequest

  /**
   * @return Optional headers specified with the command.
   */
  def headersOption: Option[CommitHeaders]
}

object CommandRequest {

  import scala.language.implicitConversions

  /**
   * Create a new command request for the specified command.
   * @param command to request.
   * @return Command request.
   */
  def apply(command: AggregateCommand): CommandRequest = AggregateCommandRequest(command)

  /**
   * Allows implicit conversion of a command into a command request with advanced options.
   * @param command to convert.
   * @return command request.
   */
  implicit def commandToRequest(command: AggregateCommand): CommandRequest = CommandRequest(command)
}

/**
 *
 * @param command to execute.
 * @param expectedOption for revision check.
 * @param headersOption to store in the commit.
 */
private[this] case class AggregateCommandRequest(command: AggregateCommand,
                                                 expectedOption: Option[AggregateRevision] = None,
                                                 headersOption: Option[CommitHeaders] = None) extends CommandRequest {
  type Id = command.Id

  /**
   * @return The id of the root entity.
   */
  override def id = command.id

  /**
   * Creates a new command request with the expected revision.
   * @param expected revision for the aggregate.
   * @return command request with expected revision.
   */
  override def withExpectedRevision(expected: AggregateRevision): CommandRequest = {
    copy(expectedOption = Some(expected))
  }


  /**
   * Check if the command is valid for the actual revision.
   * @param actual revision of the aggregate.
   * @param success executed when the revision check passed.
   * @param failed is executed when the revision check fails.
   * @return True when the revision is correct or expected is empty.
   */
  override def checkRevision(actual: AggregateRevision)(success: () => Unit)(failed: (AggregateRevision) => Unit): Unit = {

    def unspecified() = if (command.isRevisionCheckRequired) throw AggregateRevisionRequiredException(command) else success()

    def specified(expected: AggregateRevision) = if (actual == expected) success() else failed(expected)

    expectedOption.fold(unspecified())(specified)
  }

  /**
    * Specifies headers to store with the event.
    * @param headers to store.
    * @return command request with updated headers.
    */
  override def withHeaders(headers: CommitHeaders): CommandRequest = {
    copy(headersOption = Some(headers))
  }
}