package models

import Territory.TerritoryInfo

class Board {
  val territories: Map[Int, Territory] = {
    Territory.territoryData.map { case (id, TerritoryInfo(name, parent, _)) =>
      id -> new Territory(id, name, parent)
    }
  }
}
