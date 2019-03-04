package controllers

import javax.inject.Inject
import models.{Game, GameManager}
import play.api.data._
import play.api.mvc._

class GameController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) with ControllerUtils {

  def index: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index(JoinForm.form))
  }

  def createGame: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Redirect(routes.PlayerLobbyController.listPlayers(GameManager.makeNewGame))
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
        Redirect(routes.PlayerLobbyController.listPlayers(gameId.id))
      }

    JoinForm.form.bindFromRequest.fold(errorFunction, successFunction)
  }

  def showGame(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { g: Game =>
      Ok(views.html.game(g))
    }
  }

}
