package models

case class Player(name: String,
                  var armies: Int,
                  gameId: String,
                  var numberOfTerritories: Int = 0,
                  var awardedArmies: Int = 0) {

  def awardArmies(board: Board): Unit = {
    // Refactor; `this` is a code smell
    val ownedTerritories = board.territoriesOwnedBy(this)
    awardedArmies = 0

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

