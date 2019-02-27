package controllers

import javax.inject.Inject
import models.{Game, GameManager}
import play.api.data._
import play.api.mvc._

class PlayerLobbyController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

    def listPlayers(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
      GameManager.getGameById(gameId).map {
          game: Game => game.getLobbiedPlayers
      } match {
        case None =>
          Redirect(routes.GameController.index()).flashing("ERROR" -> s"Could not find game with game ID '$gameId'")
        case Some(players) =>
          Ok(views.html.listPlayers(players, gameId, PlayerForm.form))
      }
    }

    // This will be the action that handles our form post
    def addPlayer(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
      val errorFunction = { formWithErrors: Form[PlayerForm.PlayerData] =>
        BadRequest(views.html.listPlayers(GameManager.getGameById(gameId).get.getLobbiedPlayers, gameId, formWithErrors))
      }

      def successFunction(playerData: PlayerForm.PlayerData) = {
        val name = playerData.name
        GameManager.getGameById(gameId) match {
          case None =>
            Redirect(routes.GameController.index()).flashing("ERROR" -> s"Could not find game with game ID '$gameId'")
          case Some(g) =>
            if (g.getLobbiedPlayers.contains(name)) {
              Redirect(routes.PlayerLobbyController.listPlayers(gameId)).flashing("ERROR" -> s"Player with name $name already in queue")
            } else {
              g.addPlayerToLobby(name)
              Redirect(routes.PlayerLobbyController.listPlayers(gameId))
            }
        }
      }
      val formValidationResult = PlayerForm.form.bindFromRequest
      formValidationResult.fold(errorFunction, successFunction)
    }
}

