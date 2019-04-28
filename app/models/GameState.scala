package models

import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue}

/** Represents the current stage of gameplay */
sealed trait GameState {
  def toJson: JsValue
}

/** Represents when players are joining the game */
case object Lobbying extends GameState {
  override def toJson: JsValue = JsObject(Seq("state" -> JsString("LOBBYING")))
}

/** Represents when the player order and territories are assigned and territories are allocated */
case object Allotting extends GameState {
  override def toJson: JsValue = JsObject(Seq("state" -> JsString("ALLOTTING")))
}

/** Represents when a player assigns rewarded armies to territories */
case object Assigning extends GameState {
  override def toJson: JsValue = JsObject(Seq("state" -> JsString("ASSIGNING")))
}

/** Represents when a player is choosing options for attacking */
case object Attacking extends GameState {
  override def toJson: JsValue = JsObject(Seq("state" -> JsString("ATTACKING")))
}

/** Represents when a player is choosing options for defending.
  *
  * @param attackerDice       the number of dice the attacker chose
  * @param attackingTerritory the ID of the attacking territory
  * @param defendingTerritory the ID of the defending territory
  */
case class Defending(attackerDice: Int, attackingTerritory: Territory, defendingTerritory: Territory) extends GameState {
  override def toJson: JsValue = JsObject(Seq(
    "state" -> JsString("DEFENDING"),
    "attackerDice" -> JsNumber(attackerDice),
    "attackingTerritoryId" -> JsNumber(attackingTerritory.id),
    "defendingTerritoryId" -> JsNumber(defendingTerritory.id)))
}

/** Represents when a player is moving armies into a newly conquered territory */
case object Relocating extends GameState {
  override def toJson: JsValue = JsObject(Seq("state" -> JsString("RELOCATING")))
}

/** Represents when a player is moving around armies at the end of the turn */
case object Fortifying extends GameState {
  override def toJson: JsValue = JsObject(Seq("state" -> JsString("FORTIFYING")))
}

/** Represents when the game has been won by a player.
  *
  * @param winner the player who won
  */
case class Finished(winner: Player) extends GameState {
  override def toJson: JsValue = JsObject(Seq(
    "state" -> JsString("FINISHED"),
    "winner" -> JsString(winner.name)))
}

