package models

sealed trait GameState

case object Lobbying extends GameState

case object Assigning extends GameState

case object Attacking extends GameState

case class Defending(attackerDice: Int, attackingTerritory: Territory, defendingTerritory: Territory) extends GameState

case object Relocating extends GameState

case object Running extends GameState

case class Finished(winner: Player) extends GameState

