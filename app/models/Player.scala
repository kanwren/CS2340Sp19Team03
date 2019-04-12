package models

case class Player(name: String,
                  var armies: Int,
                  gameId: String,
                  var numberOfTerritories: Int = 0,
                  var awardedArmies: Int = 0) {

  def awardArmies(board: Board): Unit = {
    // Refactor into separate functions; `this` is a code smell
    val ownedTerritories = board.territories.values.filter((t: Territory) => t.owner.contains(this))
    awardedArmies = 0

    for ((continent, armiesReward) <- Territory.continentRewards) {
      val ownedInContinent = ownedTerritories.count(_.parent == continent)
      if (ownedInContinent == Territory.territoriesInContinent(continent)) {
        awardedArmies += armiesReward
      }
    }

    awardedArmies += (if (numberOfTerritories < 9) 3 else numberOfTerritories / 3)
    armies += awardedArmies
  }

  override def toString: String = name
}

