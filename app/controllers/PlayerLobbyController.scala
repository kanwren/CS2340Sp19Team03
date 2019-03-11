package controllers

import javax.inject.Inject
import models.Game
import play.api.data._
import play.api.mvc._

import scala.collection.mutable

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
          val parameters: mutable.HashMap[String, Seq[String]] = mutable.HashMap()
          parameters += "playerName" -> List(name)
          Redirect(routes.GameController.showGame(gameId).absoluteURL(), parameters.toMap)
        }
      }

    PlayerForm.form.bindFromRequest.fold(errorFunction, successFunction)
  }

}

