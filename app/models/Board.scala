package models

import Territory.TerritoryInfo

/** Data type representing the map of a Risk game */
class Board {
  val territories: Map[Int, Territory] = {
    Territory.territoryData.map { case (id, TerritoryInfo(name, parent, _)) =>
      id -> new Territory(id, name, parent)
    }
  }

  /** Look up all territories owned by a player
    * @param player the player to query for
    * @return the territories that the player owns on the board
    */
  def territoriesOwnedBy(player: Player): Iterable[Territory] =
    territories.values.filter((t: Territory) => t.owner.contains(player))

  /** Mutator for the number of armies on a given territory
    * @param id the ID of the territory to update
    * @param armies the new number of armies in the territory
    */
  def setArmyCount(id: Int, armies: Int): Unit = {
    territories(id).armies = armies
  }
}

