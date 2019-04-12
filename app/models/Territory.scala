package models

import play.api.libs.json._


case class Territory(id: Int, name: String, parent: String, var armies: Int = 0, var owner: Option[Player] = None) {

  def updateAfterBattle(lost: Int, enemy: Territory): Unit = {
    armies = (armies - lost) max 0
    if (armies == 0) {
      owner = enemy.owner
    }
  }

}

object Territory {
  case class TerritoryInfo(name: String, parent: String, adjacencies: List[Int])

  def adjacencies(id: Int): List[Int] = territoryData(id).adjacencies

  val territoryData: Map[Int, TerritoryInfo] = {
    val contents = scala.io.Source.fromFile("conf/board.json")
    val json: JsValue = Json.parse(contents.mkString)
    contents.close()
    implicit val infoReads: Reads[TerritoryInfo] = Json.reads[TerritoryInfo]
    val result: List[TerritoryInfo] = json.as[List[TerritoryInfo]]
    result.zipWithIndex.map(_.swap)(collection.breakOut)
  }

  val territoriesInContinent: Map[String, Int] = Map(
    "africa" -> 6,
    "asia" -> 12,
    "australia" -> 4,
    "europe" -> 7,
    "northamerica" -> 9,
    "southamerica" -> 4
  )

  val continentRewards: Map[String, Int] = Map(
    "africa" -> 3,
    "asia" -> 7,
    "australia" -> 2,
    "europe" -> 5,
    "northamerica" -> 5,
    "southamerica" -> 2
  )
}
