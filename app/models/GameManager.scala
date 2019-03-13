package models

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Random

object GameManager {
  private val games: mutable.HashMap[String, Game] = mutable.HashMap[String, Game]()

  def getGameById(gameId: String): Option[Game] = games get gameId

  def makeNewGame: String = {
    val id = generateId
    games += (id -> new Game(id))
    id
  }

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
