package controllers

import models.Game

import scala.collection.mutable
import scala.util.Random

object GameManager {
  val games: mutable.HashMap[String, Game] = mutable.HashMap[String, Game]()

  def getGameById(gameId: String): Option[Game] = games get gameId

  def makeNewGame: String = {
    val id = generateId
    games += id -> new Game(id)
    id
  }

  private def generateId: String = {
    val id = Random.alphanumeric.take(Game.idLength).mkString("")
    if (games contains id) {
      generateId
    }
    else {
      id
    }
  }
}
