package controllers

import javax.inject.Inject
import models.Game
import play.api.data._
import play.api.mvc._

class PlayerLobbyController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) with ControllerUtils {

  def addPlayer(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    def errorFunction(formWithErrors: Form[PlayerForm.PlayerData]) =
      onGame(gameId) { game =>
        BadRequest(views.html.lobby(game.getLobbiedPlayers, gameId, formWithErrors))
      }

    def successFunction(playerData: PlayerForm.PlayerData) =
      onGame(gameId) { game: Game =>
        val name = playerData.name
        if (game.getLobbiedPlayers.contains(name)) {
          Redirect(routes.GameController.showGame(gameId)).flashing("ERROR" -> s"Player with name $name already in queue")
        } else {
          game.addPlayerToLobby(name)
          Redirect(routes.GameController.showGame(gameId))
        }
      }

    PlayerForm.form.bindFromRequest.fold(errorFunction, successFunction)
  }

}

