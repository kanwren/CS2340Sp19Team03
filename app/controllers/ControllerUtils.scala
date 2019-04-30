package controllers

import models.{Game, GameManager}
import play.api.libs.json.{Json, Writes}
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

  /** Like `onGame`, except it redirects to the game's page after modifying it
    * via `handler`
    *
    * @param gameId the ID of the game to modify
    * @param handler strategy representing how the game should be modified
    * @return a redirection to the game's page
    */
  def overGame(gameId: String)(handler: Game => Unit): Result =
    onGame(gameId) { game: Game =>
      handler(game)
      Redirect(routes.GameController.showGame(gameId))
    }

  /** Extracts some JSON-convertible value from a game and returns it as a
    * JSON response
    *
    * @param gameId the ID of the game being queried
    * @param extractor how the JSON-convertible value is obtained from the game
    * @tparam T the JSON-convertible type being extracted
    * @return a JSON response
    */
  def gameJsonRequest[T : Writes](gameId: String)(extractor: Game => T): Result =
    onGame(gameId) { game: Game =>
      Ok(Json.toJson(extractor(game)))
    }

  /** Handles invalid game ID lookups by redirecting to the index with an error
    * message.
    *
    * @param gameId the game ID that did not exist
    * @return a redirection to an error page
    */
  def redirectInvalidGameId(gameId: String): Result =
    Redirect(routes.GameController.index()).flashing("ERROR" ->
      s"Could not find game with game ID '$gameId'")

  /** Handles requests made in an invalid game state by redirecting to an error
    * page
    *
    * @param gameId the ID of the game in the invalid state
    * @return a redirection to an error page
    */
  def redirectInvalidGameState(gameId: String): Result =
    Redirect(routes.GameController.index()).flashing("ERROR" ->
      s"An internal error occurred in game '$gameId': invalid game state")

}
