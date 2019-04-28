package models

sealed trait GameState {
  def asString: String
}

case object Lobbying extends GameState {
  def asString: String = "lobbying"
}

case object Assigning extends GameState {
  def asString: String = "assigning"
}

case object Attacking extends GameState {
  def asString: String = "attacking"
}

case class Defending(attackerDice: Int, attackingTerritory: Territory, defendingTerritory: Territory) extends GameState {
  def asString: String = "defending"
}

case object Relocating extends GameState {
  def asString: String = "relocating"
}

case object Running extends GameState {
  def asString: String = "running"
}

case class Finished(winner: Player) extends GameState {
  def asString: String = "finished"
}
