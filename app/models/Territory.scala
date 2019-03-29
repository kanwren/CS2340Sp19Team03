package models

import play.api.libs.json._

case class Territory(id: Int, name: String, parent: String, var armies: Int = 0) {
  var owner: Option[Player] = Option(Player("default", 0, ""))
  def updateArmies: Unit = armies += 1
}

object Territory {
  case class TerritoryInfo(name: String, parent: String, adjacencies: List[Int])

  def adjacencies(id: Int): List[Int] = territoryData(id).adjacencies

  val territoryData: Map[Int, TerritoryInfo] = {
    val contents = scala.io.Source.fromFile("conf/board.json").mkString
    val json: JsValue = Json.parse(contents)
    implicit val infoReads: Reads[TerritoryInfo] = Json.reads[TerritoryInfo]
    val result: List[TerritoryInfo] = json.as[List[TerritoryInfo]]
    result.zipWithIndex.map(_.swap)(collection.breakOut)
  }
}
