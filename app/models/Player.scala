package models

import play.api.libs.json._

/** Type representing a player in a game.
  *
  * @param name                the display name of the player
  * @param armies              the number of armies the player owns
  * @param gameId              the ID of the game the player is in
  * @param numberOfTerritories the number of territories the player owns
  */
case class Player(name: String,
                  var armies: Int,
                  gameId: String,
                  var numberOfTerritories: Int = 0) {

  /** Calculate armies to award this player
    *
    * @param board the board of the current game
    * @return the number of armies to award the player
    */
  def calculateReward(board: Board): Int = {
    // Refactor; `this` is a code smell
    val ownedTerritories = board.territoriesOwnedBy(this)

    val baseReward: Int = if (numberOfTerritories < 9) 3 else numberOfTerritories / 3

    val extraAward: Int = Territory.continentRewards.map { case (continent, armiesReward) =>
      val ownedInContinent = ownedTerritories.count(_.parent == continent)
      val required = Territory.territoriesInContinent(continent)
      if (ownedInContinent == required) armiesReward else 0
    }.sum

    baseReward + extraAward
  }

  override def toString: String = name

  /** Convert information about a player to a JSON object based on the current
    * state of the board
    *
    * @param board the board of the game that the player is in
    * @return a JSON value representing the player's state
    */
  def stateToJson(board: Board): JsValue = JsObject(Seq(
    "name" -> JsString(name),
    "armies" -> JsNumber(armies),
    "gameId" -> JsString(gameId),
    "numberOfTerritories" -> JsNumber(numberOfTerritories),
    "awardedArmies" -> JsNumber(calculateReward(board))))
}

