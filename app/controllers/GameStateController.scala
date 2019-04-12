package controllers

import javax.inject.Inject
import models.Game.RollResults
import models._
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc._

class GameStateController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) with ControllerUtils {

  def getGameState(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(GameInfo(game.turn, game.players))
      Ok(json)
    }
  }

  def getTerritoryData(gameId: String, territoryId: Int): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(game.board.territories(territoryId))
      Ok(json)
    }
  }
  def getTerritoriesData(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(game.board.territories.values)
      Ok(json)
    }
  }

  def getTerritoryAdjacencies(gameId: String, territoryId: Int): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(Territory.adjacencies(territoryId))
      Ok(json)
    }
  }

  def getPlayerData(gameId: String, playerOrder: Int): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(game.players(playerOrder))
      Ok(json)
    }
  }
  def getPlayersData(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(game.players)
      Ok(json)
    }
  }

  def simulateDiceRoll(attackerDice: Int, defenderDice: Int, attackingTerritoryId: Int, defendingTerritoryId: Int, gameId: String): Action[AnyContent] =
    Action { implicit request: MessagesRequest[AnyContent] =>
      onGame(gameId) { game: Game =>
        val attackingTerritory = game.board.territories(attackingTerritoryId)
        val defendingTerritory = game.board.territories(defendingTerritoryId)
        val results: RollResults = Game.resolveBattle(attackerDice, defenderDice, attackingTerritory, defendingTerritory)
        val json: JsValue = Json.toJson(results)
        Ok(json)
      }
    }

  implicit val playerData: Writes[Player] = Json.writes[Player]
  implicit val gameInfoData: Writes[GameInfo] = Json.writes[GameInfo]
  implicit val territoryData: Writes[Territory] = Json.writes[Territory]
  implicit val rollResultsData: Writes[RollResults] = Json.writes[RollResults]

}
