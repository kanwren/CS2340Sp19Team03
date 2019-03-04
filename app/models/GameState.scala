package models

sealed trait GameState

case object Lobbying extends GameState

case object Running extends GameState

case class Finished(winner: Player) extends GameState
