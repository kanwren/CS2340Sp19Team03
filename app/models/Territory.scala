package models

import play.api.libs.json._

/** Data type representing a territory on the game's map.
  *
  * @param id     the ID number of the territory
  * @param name   the display name of the territory
  * @param parent the display name of the territory's continent
  * @param armies the number of armies on this territory
  * @param owner  the owner, if one exists
  */
case class Territory(id: Int, name: String, parent: String, var armies: Int = 0, var owner: Option[Player] = None) {

  /** After battle, update number of armies and owner according to armies lost.
    *
    * @param lost  the number of armies lost in the battle
    * @param enemy the territory that attacked the current territory
    */
  def updateAfterBattle(lost: Int, enemy: Territory): Unit = {
    armies = (armies - lost) max 0
    if (armies == 0) {
      owner = enemy.owner
    }
  }

}

/** Utilities and static data related to preset territory information */
object Territory {

  /** The static information about a territory on the map.
    *
    * @param name        the display name of the territory
    * @param parent      the display name of the territory's parent continent
    * @param adjacencies the IDs of the territories adjacent to this territory
    */
  case class TerritoryInfo(name: String, parent: String, adjacencies: List[Int])

  /** Fetch the IDs of territories adjacent to a given territory.
    *
    * @param id the ID of the territory being queried
    * @return a list of the IDs of the adjacent territories
    */
  def adjacencies(id: Int): List[Int] = territoryData(id).adjacencies

  val territoryData: Map[Int, TerritoryInfo] = {
    val contents = scala.io.Source.fromFile("conf/board.json")
    val json: JsValue = Json.parse(contents.mkString)
    contents.close()
    implicit val infoReads: Reads[TerritoryInfo] = Json.reads[TerritoryInfo]
    val result: List[TerritoryInfo] = json.as[List[TerritoryInfo]]
    result.zipWithIndex.map(_.swap)(collection.breakOut)
  }

  val totalTerritories: Int = territoryData.keys.size

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
