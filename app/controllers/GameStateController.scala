package controllers

import javax.inject.Inject
import models.Game.BattleResults
import models._
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc._

/**
  * Controller handling all requests relating to querying and modifying the
  * current game state
  * @param cc Implicitly injected messages controller
  */
class GameStateController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) with ControllerUtils {

  /**
    * Retrieves the current turn and players from a game.
    * @param gameId the ID of the game being queried
    * @return a JSON response containing the current turn and players
    */
  def getGameState(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(GameInfo(game.turn, game.players))
      Ok(json)
    }
  }

  /**
    * Retrieves data of a territory by ID from a game.
    * @param gameId the ID of the game being queried
    * @param territoryId the ID of the territory being fetched
    * @return a JSON response containing the Territory data
    */
  def getTerritoryData(gameId: String, territoryId: Int): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(game.board.territories(territoryId))
      Ok(json)
    }
  }

  /**
    * Fetch the data of all territories from a game.
    * @param gameId the ID of the game being queried
    * @return a JSON response containing all Territory data in a list
    */
  def getTerritoriesData(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(game.board.territories.values)
      Ok(json)
    }
  }

  /**
    * Fetch the IDs of all territories adjacent to a given territory.
    * @param gameId the ID of the game being queried
    * @param territoryId the ID of the current territory
    * @return a JSON response containing the IDs of all territories adjacent to a territory
    */
  def getTerritoryAdjacencies(gameId: String, territoryId: Int): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { _ =>
      val json: JsValue = Json.toJson(Territory.adjacencies(territoryId))
      Ok(json)
    }
  }

  /**
    * Fetch the data of a player in a game.
    * @param gameId the ID of the game being queried
    * @param playerOrder the position of the player in the turn order
    * @return a JSON response containing the corresponding Player data
    */
  def getPlayerData(gameId: String, playerOrder: Int): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(game.players(playerOrder))
      Ok(json)
    }
  }

  /**
    * Fetch the data of all players in a game.
    * @param gameId the ID of the game being queried
    * @return a JSON response containing all Player data in a list
    */
  def getPlayersData(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(game.players)
      Ok(json)
    }
  }

  /**
    * Resolve a battle by simulating a dice roll, and update territory armies and owners accordingly.
    * @param attackerDice the number of dice of the attacker
    * @param defenderDice the number of dice of the defender
    * @param attackingTerritoryId the ID of the attacking territory
    * @param defendingTerritoryId the ID of the defending territory
    * @param gameId the ID of the current game
    * @return a JSON response containing the dice rolls and armies lost by each territory
    */
  def simulateDiceRoll(attackerDice: Int, defenderDice: Int, attackingTerritoryId: Int, defendingTerritoryId: Int, gameId: String): Action[AnyContent] =
    Action { implicit request: MessagesRequest[AnyContent] =>
      onGame(gameId) { game: Game =>
        val attackingTerritory = game.board.territories(attackingTerritoryId)
        val defendingTerritory = game.board.territories(defendingTerritoryId)

        val results: BattleResults = Game.resolveBattle(attackerDice, defenderDice, attackingTerritory, defendingTerritory)

        attackingTerritory.updateAfterBattle(results.attackerLost, defendingTerritory)
        defendingTerritory.updateAfterBattle(results.defenderLost, attackingTerritory)

        val json: JsValue = Json.toJson(results)
        Ok(json)
      }
    }

  implicit val playerData: Writes[Player] = Json.writes[Player]
  implicit val gameInfoData: Writes[GameInfo] = Json.writes[GameInfo]
  implicit val territoryData: Writes[Territory] = Json.writes[Territory]
  implicit val battleResultsData: Writes[BattleResults] = Json.writes[BattleResults]

}
