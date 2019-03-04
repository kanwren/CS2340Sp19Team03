package controllers

import javax.inject.Inject
import models.GameManager
import play.api.data._
import play.api.mvc._

class GameController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  def index: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index(JoinForm.form))
  }

  def createGame: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val gameId = GameManager.makeNewGame
    Redirect(routes.PlayerLobbyController.listPlayers(gameId))
  }

  def startGame(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    val game = GameManager.getGameById(gameId)
    game match {
      case None => Redirect(routes.GameController.index()).flashing("ERROR" -> s"Could not find game with game ID '$gameId'")
      case Some(g) =>
        g.startGame()
        Redirect(routes.GameController.showGame(gameId))
    }
  }

  def joinGame: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[JoinForm.GameId] =>
      BadRequest(views.html.index(formWithErrors))
    }

    def successFunction(gameId: JoinForm.GameId): Result = {
      GameManager.getGameById(gameId.id) match {
        case None =>
          Redirect(routes.GameController.index()).flashing("ERROR" -> s"Could not find game with game ID '${gameId.id}'")
        case Some(_) =>
          Redirect(routes.PlayerLobbyController.listPlayers(gameId.id))
      }
    }

    val formValidationResult = JoinForm.form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  def showGame(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    val game = GameManager.getGameById(gameId)
    game match {
      case None => Redirect(routes.GameController.index()).flashing("ERROR" -> s"Could not find game with game ID '$gameId'")
      case Some(g) => Ok(views.html.game(g))
    }
  }

}
