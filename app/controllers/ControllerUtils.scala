package controllers

import models.{Game, GameManager}
import play.api.mvc.Results._
import play.api.mvc._

trait ControllerUtils {

  def onGame(gameId: String)(handler: Game => Result): Result =
    GameManager.getGameById(gameId).fold(redirectInvalidGameId(gameId))(handler)

  def redirectInvalidGameId(gameId: String): Result =
    Redirect(routes.GameController.index()).flashing("ERROR" -> s"Could not find game with game ID '$gameId'")

}
