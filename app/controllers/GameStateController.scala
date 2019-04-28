package controllers

import javax.inject.Inject
import models._
import play.api.libs.json._
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
  def getGameInfo(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(GameInfo(game.turn, game.players, game.activePlayer))
      Ok(json)
    }
  }

  /** Retrieves the current game state from a game.
    *
    * @param gameId the ID of the game being queried
    * @return a JSON response containing the data of the game state
    */
  def getGameState(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      Ok(game.gameState.toJson)
    }
  }

  /** Retrieve whether or not the player is waiting
    *
    * @param gameId the ID of the game being queried
    * @param player the ID (turn order) of the player being queried
    * @return a JSON boolean response with whether or not the player is currently waiting
    */
  def getPlayerWaiting(gameId: String, player: Int): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val waiting = game.gameState match {
        case Lobbying | Allotting | Finished(_) => false
        case Assigning(_) | Attacking | Defending(_, _, _) | Relocating | Fortifying if game.activePlayer == player => false
        case _ => true
      }
      Ok(JsBoolean(waiting))
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
      val json: JsValue = game.players(playerOrder).toJson(game.board)
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
      val json: JsValue = JsArray(game.players.map(_.toJson(game.board)))
      Ok(json)
    }
  }

  def getActivePlayer(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      val json: JsValue = JsNumber(game.activePlayer)
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
