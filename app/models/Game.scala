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

  def nextTurn(): Unit = {
    turn += 1
    players(turn % players.size).awardArmies(board)
  }

  def getLobbiedPlayers: Seq[String] = lobbiedPlayers

  def resolveBattle(attackerDice: Int, defenderDice: Int, attackingTerritory: Territory, defendingTerritory: Territory): Unit = {
    var attackerArmiesLost = 0
    var defenderArmiesLost = 0

    var attackerNums = Seq.fill(attackerDice)(1 + scala.util.Random.nextInt((6)).sorted.reverse
    var defenderNums = Seq.fill(defenderDice)(1 + scala.util.Random.nextInt((6)).sorted.reverse

    if (defenderNums(0)  >= attackerNums(0)) {
      attackerArmiesLost += 1
    } else {
      defenderArmiesLost += 1
    }

    if (attackerDice > 1 && defenderDice > 1) {
      if (defenderNums(1)  >= attackerNums(1)) {
        attackerArmiesLost += 1
      } else {
        defenderArmiesLost += 1
      }
    }
    attackingTerritory.armies -= attackerArmiesLost
    defendingTerritory.armies -= defenderArmiesLost
  }

}

object Game {
  val idLength: Int = 4
}

case class GameInfo(turn: Int, players: Seq[Player])
