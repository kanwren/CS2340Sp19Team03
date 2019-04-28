package controllers

import javax.inject.Inject
import models._
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc._

/** Controller handling all requests relating to querying and modifying the
  * current game state.
  *
  * @param cc Implicitly injected messages controller
  */
class GameStateController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) with ControllerUtils {

  /** Retrieves the current turn and players from a game.
    *
    * @param gameId the ID of the game being queried
    * @return a JSON response containing the current turn and players
    */
  def getGameState(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(GameInfo(game.turn, game.players, game.activePlayer))
      Ok(json)
    }
  }

  /** Retrieves data of a territory by ID from a game.
    *
    * @param gameId      the ID of the game being queried
    * @param territoryId the ID of the territory being fetched
    * @return a JSON response containing the `Territory` data
    */
  def getTerritoryData(gameId: String, territoryId: Int): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(game.board.territories(territoryId))
      Ok(json)
    }
  }

  /** Fetch the data of all territories from a game.
    *
    * @param gameId the ID of the game being queried
    * @return a JSON response containing all `Territory` data in a list
    */
  def getTerritoriesData(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(game.board.territories.values)
      Ok(json)
    }
  }

  /** Fetch the IDs of all territories adjacent to a given territory.
    *
    * @param gameId      the ID of the game being queried
    * @param territoryId the ID of the current territory
    * @return a JSON response containing the IDs of all territories adjacent to a territory
    */
  def getTerritoryAdjacencies(gameId: String, territoryId: Int): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { _ =>
      val json: JsValue = Json.toJson(Territory.adjacencies(territoryId))
      Ok(json)
    }
  }

  /** Fetch the data of a player in a game.
    *
    * @param gameId      the ID of the game being queried
    * @param playerOrder the position of the player in the turn order
    * @return a JSON response containing the corresponding `Player` data
    */
  def getPlayerData(gameId: String, playerOrder: Int): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(game.players(playerOrder))
      Ok(json)
    }
  }

  /**
    * Fetch the data of all players in a game.
    *
    * @param gameId the ID of the game being queried
    * @return a JSON response containing all Player data in a list
    */
  def getPlayersData(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(game.players)
      Ok(json)
    }
  }

  def getActivePlayer(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(game.activePlayer)
      Ok(json)
    }
  }

  def getDefenderConquered(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      Ok(Json.toJson(game.gameState == Relocating))
    }
  }

  implicit val playerData: Writes[Player] = Json.writes[Player]
  implicit val gameInfoData: Writes[GameInfo] = Json.writes[GameInfo]
  implicit val territoryData: Writes[Territory] = Json.writes[Territory]

}
