package models


case class Player(name: String,
									var armies: Int,
									gameId: String,
									var numberOfTerritories: Int = 0,
									var awardedArmies: Int = 0) {

  def awardArmies(): Unit = {
		val thisGame: Game = GameManager.getGameById(gameId).get
		val ownedTerritories = thisGame.board.territories.values.filter((t: Territory) => t.owner.contains(this))

		def filledContinent(continent: String): Boolean =  {
			val numberTerritoriesInContinent = ownedTerritories.count((t: Territory) => t.parent == continent)
			awardedArmies = 0
			continent match {
				case "africa" => numberTerritoriesInContinent == 6
				case "asia" => numberTerritoriesInContinent == 12
				case "australia" => numberTerritoriesInContinent == 4
				case "europe" => numberTerritoriesInContinent == 7
				case "northamerica" => numberTerritoriesInContinent == 9
				case "southamerica" => numberTerritoriesInContinent == 4
				case _ => false
			}
		}

		if(filledContinent("africa")) {
			awardedArmies += 3
		}
		if(filledContinent("asia")) {
			awardedArmies += 7
		}
		if(filledContinent("australia")) {
			awardedArmies += 2
		}
		if(filledContinent("europe")) {
			awardedArmies += 5
		}
		if(filledContinent("northamerica")) {
			awardedArmies += 5
		}
		if(filledContinent("southamerica")) {
			awardedArmies += 2
		}

		awardedArmies += (if (numberOfTerritories < 9) 3 else numberOfTerritories / 3)
		armies += awardedArmies

  }

  override def toString: String = name
}

