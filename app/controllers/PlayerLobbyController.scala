package controllers

import javax.inject.Inject
import models.Game
import play.api.data._
import play.api.mvc._

class PlayerLobbyController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) with ControllerUtils {

  def listPlayers(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      Ok(views.html.listPlayers(game.getLobbiedPlayers, gameId, PlayerForm.form))
    }
  }

  def addPlayer(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    def errorFunction(formWithErrors: Form[PlayerForm.PlayerData]) =
      onGame(gameId) { game =>
        BadRequest(views.html.listPlayers(game.getLobbiedPlayers, gameId, formWithErrors))
      }

    def successFunction(playerData: PlayerForm.PlayerData) =
      onGame(gameId) { game: Game =>
        val name = playerData.name
        if (game.getLobbiedPlayers.contains(name)) {
          Redirect(routes.PlayerLobbyController.listPlayers(gameId)).flashing("ERROR" -> s"Player with name $name already in queue")
        } else {
          game.addPlayerToLobby(name)
          Redirect(routes.PlayerLobbyController.listPlayers(gameId))
        }
      }

    PlayerForm.form.bindFromRequest.fold(errorFunction, successFunction)
  }
}

