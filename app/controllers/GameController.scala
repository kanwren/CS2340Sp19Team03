package controllers

import java.net.{URLDecoder, URLEncoder}

import akka.actor.ActorSystem
import akka.stream.Materializer
import javax.inject.Inject
import models._
import play.api.data._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._

class GameController @Inject()(cc: MessagesControllerComponents)
                              (implicit system: ActorSystem, mat: Materializer) extends MessagesAbstractController(cc) with ControllerUtils {

  def index: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index(JoinForm.form))
  }

  def createGame: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    val id = GameManager.makeNewGame
    Redirect(routes.GameController.index()).flashing("INFO" -> s"Your game ID is: $id")
  }

  def joinGame: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    def errorFunction(formWithErrors: Form[JoinForm.JoinRequest]) =
      BadRequest(views.html.index(formWithErrors))

    def successFunction(joinRequest: JoinForm.JoinRequest): Result =
      onGame(joinRequest.id) { game =>
        val name = joinRequest.playerName
        if (game.getLobbiedPlayers.contains(name)) {
          Redirect(routes.GameController.index()).flashing("ERROR" -> s"Player with name $name already in queue")
        } else {
          game.addPlayerToLobby(name)
          //          val parameters: mutable.HashMap[String, Seq[String]] = mutable.HashMap()
          //          parameters += "playerName" -> List(name)
          //          Redirect(routes.GameController.showGame(joinRequest.id).absoluteURL(), parameters.toMap)
          Redirect(routes.GameController.showGame(joinRequest.id, Some(name)))
        }
      }

    JoinForm.form.bindFromRequest.fold(errorFunction, successFunction)
  }

  def startAssignment(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      game.startAssignment()
      Redirect(routes.GameController.showGame(gameId))
    }
  }

  def startPlay(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      game.startPlay()
      Redirect(routes.GameController.showGame(gameId))
    }
  }

  def showGame(gameId: String, playerName: Option[String]): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    var pName: String = playerName.getOrElse(URLDecoder.decode(request.cookies("playerName").value, "UTF-8"))
    pName = URLEncoder.encode(pName, "UTF-8")
    onGame(gameId) { game: Game =>
      game.gameState match {
        case Lobbying =>
          Ok(views.html.lobby(game.getLobbiedPlayers, gameId)).withCookies(Cookie("playerName", pName)).bakeCookies()
        case Assigning =>
          Ok(views.html.game(game)).withCookies(Cookie("playerName", pName)).bakeCookies()
        case Running =>
          Ok(views.html.gameboard(game)).withCookies(Cookie("playerName", pName)).bakeCookies()
        case _ =>
          Redirect(routes.GameController.index()).flashing("ERROR" -> "That part of the game hasn't been implemented yet")
      }
    }
  }

  def endTurn(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      game.turn += 1
      Redirect(routes.GameController.showGame(gameId))
    }
  }

  def getGameState(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    implicit val playerData: Writes[Player] = Json.writes[Player]
    implicit val gameInfoData: Writes[GameInfo] = Json.writes[GameInfo]
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(GameInfo(game.turn, game.players))
      Ok(json)
    }
  }

  def getTerritoryData(gameId: String, territoryId: Int): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    implicit val territoryData: Writes[Territory] = (
      (JsPath \ "id").write[Int] and
        (JsPath \ "name").write[String] and
        (JsPath \ "parent").write[String] and
        (JsPath \ "armies").write[Int]
      ) (unlift(Territory.unapply))
    onGame(gameId) { game: Game =>
      val json: JsValue = Json.toJson(game.board.territories(territoryId))
      Ok(json)
    }
  }
}