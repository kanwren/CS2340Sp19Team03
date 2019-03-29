package models

import scala.collection.mutable
import scala.util.Random

class Game(val gameId: String) {
  var turn: Int = 0
  val board: Board = new Board()
  private val lobbiedPlayers: mutable.ArrayBuffer[String] = mutable.ArrayBuffer[String]()
  var players: Seq[Player] = Seq[Player]()
  var gameState: GameState = Lobbying

  def addPlayerToLobby(name: String): Unit = lobbiedPlayers += name

  def startAssignment(): Unit = {
    if (gameState == Lobbying) {
      gameState = Assigning
      val armies = 50 - 5 * lobbiedPlayers.length
      players = Random.shuffle(for {
        name <- lobbiedPlayers
      } yield Player(name, armies, gameId))

      val playerCycle = Stream.continually(players.toStream).flatten
      for ((t, p) <- Random.shuffle(board.territories.values).zip(playerCycle)) {
        t.owner = Some(p)
        p.numberOfTerritories += 1
      }

      for (player <- players) {
        val armyDist = player.armies / player.numberOfTerritories
        val leftOver = player.armies % player.numberOfTerritories
        val owned = Random.shuffle(board.territories.values.filter(_.owner.forall(_ == player)))
        owned.take(leftOver).foreach(_.armies = armyDist + 1)
        owned.drop(leftOver).foreach(_.armies = armyDist)
      }

    }
  }

  def startPlay(): Unit = {
    if (gameState == Assigning) {
      gameState = Running
    }
  }

  def getLobbiedPlayers: Seq[String] = lobbiedPlayers

}

object Game {
  val idLength: Int = 4
}

case class GameInfo(turn: Int, players: Seq[Player])
