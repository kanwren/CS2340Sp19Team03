package controllers

import javax.inject.Inject
import models._
import play.api.data._
import play.api.mvc._

class GameController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) with ControllerUtils {

  def index: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index(JoinForm.form))
  }

  def createGame: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Redirect(routes.GameController.showGame(GameManager.makeNewGame))
  }

  def startGame(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      game.startGame()
      Redirect(routes.GameController.showGame(gameId))
    }
  }

  def joinGame: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    def errorFunction(formWithErrors: Form[JoinForm.GameId]) =
      BadRequest(views.html.index(formWithErrors))

    def successFunction(gameId: JoinForm.GameId): Result =
      onGame(gameId.id) { _ =>
        Redirect(routes.GameController.showGame(gameId.id))
      }

    JoinForm.form.bindFromRequest.fold(errorFunction, successFunction)
  }

  def showGame(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      game.gameState match {
        case Lobbying =>
          Ok(views.html.lobby(game.getLobbiedPlayers, gameId, PlayerForm.form))
        case Assigning =>
          Ok(views.html.game(game))
        case Running =>
          Ok(views.html.gameboard())
        case _ =>
          Redirect(routes.GameController.index()).flashing("ERROR" -> "That part of the game hasn't been implemented yet")
      }
    }
  }

}
