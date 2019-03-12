package controllers

import javax.inject.Inject
import models._
import play.api.data._
import play.api.mvc._

class GameController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) with ControllerUtils {

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
          Redirect(routes.GameController.showGame(joinRequest.id)).flashing("ERROR" -> s"Player with name $name already in queue")
        } else {
          game.addPlayerToLobby(name)
          Redirect(routes.GameController.showGame(joinRequest.id))
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

  def showGame(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      game.gameState match {
        case Lobbying =>
          Ok(views.html.lobby(game.getLobbiedPlayers, gameId))
        case Assigning =>
          Ok(views.html.game(game))
        case Running =>
          Ok(views.html.gameboard(game))
        case _ =>
          Redirect(routes.GameController.index()).flashing("ERROR" -> "That part of the game hasn't been implemented yet")
      }
    }
  }

}
