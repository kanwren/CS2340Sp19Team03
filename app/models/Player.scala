package models


case class Player(name: String, var armies: Int, gameId: String, var numberOfTerritories: Int = 0) {

  def awardArmies(): Unit = {
		val thisGame: Game = GameManager.getGameById(gameId).get
		val ownedTerritories = thisGame.board.territories.values.filter((t: Territory) => t.owner.contains(this))

		def filledContinent(continent: String): Boolean =  {
			val numberTerritoriesInContinent = ownedTerritories.count((t: Territory) => t.parent == continent)
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
			armies += 3
		}
		if(filledContinent("asia")) {
			armies += 7
		}
		if(filledContinent("australia")) {
			armies += 2
		}
		if(filledContinent("europe")) {
			armies += 5
		}
		if(filledContinent("northamerica")) {
			armies += 5
		}
		if(filledContinent("southamerica")) {
			armies += 2
		}

		armies += (if (numberOfTerritories < 9) 3 else numberOfTerritories / 3)

  }

  override def toString: String = name
}

