package com.productfoundry.akka.cqrs

import akka.actor.Props
import com.productfoundry.akka.GracefulPassivation.PassivationConfig

/**
 * Creates entities.
 * @tparam E Entity type.
 */
trait EntityFactory[E <: Entity] {

  /**
   * Creates entity props.
   * @param passivationConfig for the entity.
   * @return Props to create the entity.
   */
  def props(passivationConfig: PassivationConfig): Props
}
