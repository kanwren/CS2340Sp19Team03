package controllers

import javax.inject.Inject
import models.{Game, GameManager}
import play.api.data._
import play.api.mvc._

class GameController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  def index: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index(JoinForm.form))
  }

  def createGame: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Redirect(routes.PlayerLobbyController.listPlayers(GameManager.makeNewGame))
  }

  def startGame(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    GameManager.getGameById(gameId).fold {
      Redirect(routes.GameController.index()).flashing("ERROR" -> s"Could not find game with game ID '$gameId'")
    } { g: Game =>
      g.startGame()
      Redirect(routes.GameController.showGame(gameId))
    }
  }

  def joinGame: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[JoinForm.GameId] =>
      BadRequest(views.html.index(formWithErrors))
    }

    def successFunction(gameId: JoinForm.GameId): Result = {
      GameManager.getGameById(gameId.id).fold {
        Redirect(routes.GameController.index()).flashing("ERROR" -> s"Could not find game with game ID '${gameId.id}'")
      } { _ =>
        Redirect(routes.PlayerLobbyController.listPlayers(gameId.id))
      }
    }

    val formValidationResult = JoinForm.form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  def showGame(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    GameManager.getGameById(gameId).fold {
      Redirect(routes.GameController.index()).flashing("ERROR" -> s"Could not find game with game ID '$gameId'")
    } { g: Game =>
      Ok(views.html.game(g))
    }
  }

}
