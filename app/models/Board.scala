package models

import Territory.TerritoryInfo

class Board {
  val territories: Map[Int, Territory] = {
    Territory.territoryData.map { case (id, TerritoryInfo(name, parent, _)) =>
      id -> new Territory(id, name, parent)
    }
  }

  def setArmyCount(id: Int, armies: Int): Unit = {
    territories(id).armies = armies
  }
}

