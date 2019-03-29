package models

case class Player(name: String, var armies: Int, gameId: String) {
  var numberOfTerritories: Int = 0
  var northamerica: Int = 0
  var southamerica: Int = 0
  var europe: Int = 0
  var africa: Int = 0
  var asia: Int = 0
  var australia: Int = 0

  def updateArmies(board: Board): Unit = {

  	board.territories.foreach(kv => if (kv._2.owner == Some(this)) kv._2.parent match {
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

  	armies += {
  		if (numberOfTerritories < 9) 
  			3 
  		else 
  			numberOfTerritories / 3
  	}
  } 

  def decrementArmies: Boolean = {
  	armies -= 1
  	if (armies == 0) {
  		false
  	} else {
  		true
  	}
  }

  override def toString: String = name
}

