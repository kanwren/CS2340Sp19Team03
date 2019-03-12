package models

case class Player(name: String, armies: Int, gameId: String) {
  var numberOfTerritories: Int = 0

  override def toString: String = name
}

