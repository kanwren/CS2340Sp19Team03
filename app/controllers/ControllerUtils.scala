package controllers

import models.{Game, GameManager}
import play.api.mvc.Results._
import play.api.mvc._

/** A mixin containing common routing utilities for the app's controllers */
trait ControllerUtils {

  /** Loan pattern to lookup a game by ID and perform some continuation on the
    * result, if it exists, redirecting to an error page if it does not.
    *
    * @param gameId  the ID of the game to look up
    * @param handler continuation to get a result from a game, if it exists
    * @return the result of `handler`, if the game exists, or an error
    *         redirection otherwise
    */
  def onGame(gameId: String)(handler: Game => Result): Result =
    GameManager.getGameById(gameId).fold(redirectInvalidGameId(gameId))(handler)

  /** Handles invalid game ID lookups by redirecting to the index with an error
    * message.
    *
    * @param gameId the game ID that did not exist
    * @return an error redirection
    */
  def redirectInvalidGameId(gameId: String): Result =
    Redirect(routes.GameController.index()).flashing("ERROR" -> s"Could not find game with game ID '$gameId'")

}
