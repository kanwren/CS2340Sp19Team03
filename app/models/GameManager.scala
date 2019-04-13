package models

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Random

/** Utilities for creating and managing `Game` instances */
object GameManager {
  private val games: mutable.HashMap[String, Game] = mutable.HashMap[String, Game]()

  /** Fetch a game, if it exists.
    *
    * @param gameId the ID of the game to fetch
    * @return the game, if it exists, or `None` otherwise
    */
  def getGameById(gameId: String): Option[Game] = games get gameId

  /** Create and return a new `Game` instance.
    *
    * @return the new `Game` instance
    */
  def makeNewGame: Game = {
    val id = generateId
    val game = new Game(id)
    games += (id -> game)
    game
  }

  /** Generate a new unique fixed-length alphanumeric game ID
    *
    * @return the ID as a string
    */
  @tailrec
  private def generateId: String = {
    val id = Random.alphanumeric.take(Game.idLength).mkString
    val validId = games.get(id).forall(g => g.gameState match {
      case Finished(_) => true
      case _ => false
    })
    if (validId) id else generateId
  }
}
