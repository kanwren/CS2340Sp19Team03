package models

/** Type representing a player in a game.
  * @param name the display name of the player
  * @param armies the number of armies the player owns
  * @param gameId the ID of the game the player is in
  * @param numberOfTerritories the number of territories the player owns
  */
case class Player(name: String,
                  var armies: Int,
                  gameId: String,
                  var numberOfTerritories: Int = 0) {

  /** Increase number of armies based on current state of the game's board.
    * @param board the board of the current game
    */
  def awardArmies(board: Board): Unit = {
    // Refactor; `this` is a code smell
    val ownedTerritories = board.territoriesOwnedBy(this)

    val baseReward: Int = if (numberOfTerritories < 9) 3 else numberOfTerritories / 3

    val extraAward: Int = Territory.continentRewards.map { case (continent, armiesReward) =>
      val ownedInContinent = ownedTerritories.count(_.parent == continent)
      val required = Territory.territoriesInContinent(continent)
      if (ownedInContinent == required) armiesReward else 0
    }.sum

    armies += baseReward + extraAward
  }

  override def toString: String = name
}

