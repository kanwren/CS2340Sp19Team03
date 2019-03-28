package models

case class Player(name: String, var armies: Int, gameId: String) {
  var numberOfTerritories: Int = 0

  def calculateAdditionalArmies(board: Board): Unit = {
  	northamerica: Int = 0
  	southamerica: Int = 0
  	europe: Int = 0
  	africa: Int = 0
  	asia: Int = 0
  	australia: Int = 0

  	board.territories.foreach(kv => if (kv._1.owner == Some(this)) kv._1.parent match {
  		case "northamerica" => northamerica += 1
  		case "southamerica" => southamerica += 1
  		case "europe" => europe += 1
  		case "africa" => africa += 1
  		case "asia" => asia += 1
  		case "australia" => australia += 1
  	})

  	if (northamerica == 9)
  		armies += 5
  	if (southamerica == 4)
  		armies += 2
  	if (europe == 7)
  		armies += 5
  	if (africa == 6)
  		armies += 3
  	if (asia == 12)
  		armies += 7
  	if (australia == 4)
  		armies += 2

  	armies += if (numberOfTerritories < 9) 3 else numberOfTerritories / 3
  } 

  override def toString: String = name
}

