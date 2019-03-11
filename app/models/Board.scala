package models

import Territory.TerritoryDatum

class Board {
  val territories: Map[Int, Territory] = {
    Territory.territoryData.map { case (id, TerritoryDatum(name, parent, _)) =>
      id -> new Territory(id, name, parent)
    }
  }
}
