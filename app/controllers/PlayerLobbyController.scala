package controllers

import javax.inject.Inject
import models.{Game, GameManager}
import play.api.data._
import play.api.mvc._

class PlayerLobbyController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  def listPlayers(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    GameManager.getGameById(gameId).map {
      _.getLobbiedPlayers
    }.fold {
      Redirect(routes.GameController.index()).flashing("ERROR" -> s"Could not find game with game ID '$gameId'")
    } { players =>
      Ok(views.html.listPlayers(players, gameId, PlayerForm.form))
    }
  }

  // This will be the action that handles our form post
  def addPlayer(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    def errorFunction(formWithErrors: Form[PlayerForm.PlayerData]) =
      BadRequest(views.html.listPlayers(GameManager.getGameById(gameId).get.getLobbiedPlayers, gameId, formWithErrors))

    def successFunction(playerData: PlayerForm.PlayerData) =
      GameManager.getGameById(gameId).fold {
        Redirect(routes.GameController.index()).flashing("ERROR" -> s"Could not find game with game ID '$gameId'")
      } { g: Game =>
        val name = playerData.name
        if (g.getLobbiedPlayers.contains(name)) {
          Redirect(routes.PlayerLobbyController.listPlayers(gameId)).flashing("ERROR" -> s"Player with name $name already in queue")
        } else {
          g.addPlayerToLobby(name)
          Redirect(routes.PlayerLobbyController.listPlayers(gameId))
        }
      }

    val formValidationResult = PlayerForm.form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
}

