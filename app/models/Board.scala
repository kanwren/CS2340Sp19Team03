package models

class Board {
  val territories: Map[Int, Territory] = {
    Territory.territoryData.map { t: (Int, (String, String, List[Int])) =>
      val (id, (name, parent, _)) = t
      id -> new Territory(id, name, parent)
    }
  }
}
