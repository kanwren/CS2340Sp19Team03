package models

import play.api.libs.json._

class Territory(val id: Int, val name: String, val parent: String) {
  var armies: Int = 0
  var owner: Option[Player] = Option(Player("default", 0, ""))
}

object Territory {
  case class TerritoryInfo(name: String, parent: String, adjacencies: List[Int])

  def adjacencies(id: Int): List[Int] = territoryData(id).adjacencies

  val territoryData: Map[Int, TerritoryInfo] = {
    val contents = scala.io.Source.fromFile("conf/board.json").mkString
    val json: JsValue = Json.parse(contents)
    implicit val datumReads: Reads[TerritoryInfo] = Json.reads[TerritoryInfo]
    val result: List[TerritoryInfo] = json.as[List[TerritoryInfo]]
    result.zipWithIndex.map(_.swap)(collection.breakOut)
  }
}
