package models

import play.api.libs.json._

class Territory(val id: Int, val name: String, val parent: String) {
  var armies: Int = 0
  var owner: Option[Player] = None
}

object Territory {
  case class TerritoryDatum(name: String, parent: String, adjacencies: List[Int])

  def adjacencies(id: Int): List[Int] = territoryData(id).adjacencies

  val territoryData: Map[Int, TerritoryDatum] = {
    val contents = scala.io.Source.fromFile("../../conf/board.json").mkString
    val json: JsValue = Json.parse(contents)
    implicit val datumReads: Reads[TerritoryDatum] = Json.reads[TerritoryDatum]
    val result: List[TerritoryDatum] = json.as[List[TerritoryDatum]]
    result.zipWithIndex.map(_.swap)(collection.breakOut)
  }
}
