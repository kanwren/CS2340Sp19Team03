package models

import scala.collection.mutable
import scala.util.Random

/** Represents an instance of a game of Risk between players.
  *
  * @param gameId the ID of this game
  */
class Game(val gameId: String) {
  var turn: Int = 0
  val board: Board = new Board()
  private val lobbiedPlayers: mutable.ArrayBuffer[String] = mutable.ArrayBuffer[String]()
  var players: Seq[Player] = _
  var gameState: GameState = Lobbying
  var activePlayer: Int = 0

  /** Add player to lobby during assignment.
    *
    * @param name the display name of the player to lobby
    */
  def addPlayerToLobby(name: String): Unit = lobbiedPlayers += name

  /** Change the current game state to assigning */
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

  /** Change the current game state to running */
  def startPlay(): Unit = {
    if (gameState == Assigning) {
      gameState = Running
    }
  }

  /** Advance the turn of the current game and award armies accordingly */
  def nextTurn(): Unit = {
    turn += 1
    players(turn % players.size).awardArmies(board)
  }

  /** Fetch the lobbied players for the current game */
  def getLobbiedPlayers: Seq[String] = lobbiedPlayers

  def playerTurn(player: Player): Option[Int] = players.zipWithIndex.find {
    _._1 == player
  }.map(_._2)

}

/** Utilities for implementing gameplay mechanics */
object Game {
  val idLength: Int = 4

  /** Data type representing the results of a battle.
    *
    * @param attackerRolls the dice rolls of the attacker
    * @param defenderRolls the dice rolls of the defender
    * @param attackerLost  the number of armies lost in the attacking territory
    * @param defenderLost  the number of armies lost in the defending territory
    */
  case class BattleResults(attackerRolls: Seq[Int], defenderRolls: Seq[Int], attackerLost: Int, defenderLost: Int)

  /** Resolve battle based on random dice rolls, and update territories accordingly.
    *
    * @param attackerDice       the number of dice of the attacker
    * @param defenderDice       the number of dice of the defender
    * @param attackingTerritory the attacking territory
    * @param defendingTerritory the defending territory
    */
  def resolveBattle(attackerDice: Int, defenderDice: Int, attackingTerritory: Territory, defendingTerritory: Territory): Unit = {
    val results = simulateDiceRoll(attackerDice, defenderDice)
    attackingTerritory.updateAfterBattle(results.attackerLost, defendingTerritory)
    defendingTerritory.updateAfterBattle(results.defenderLost, attackingTerritory)
  }

  /** Randomly roll dice to decide the winner of a battle
    *
    * @param attackerDice the number of dice of the attacker
    * @param defenderDice the number of dice of the defender
    * @return the lists of rolls and the armies lost by both territories
    */
  def simulateDiceRoll(attackerDice: Int, defenderDice: Int): BattleResults = {
    val attackerRolls = rollDice(attackerDice)
    val defenderRolls = rollDice(defenderDice)

    val (attackerLost, defenderLost) =
      attackerRolls.zip(defenderRolls)
        .take(2)
        .map { case (a, b) => if (b >= a) (1, 0) else (0, 1) }
        .foldLeft((0, 0)) { case ((a1, a2), (b1, b2)) => (a1 + b1, a2 + b2) }

    BattleResults(attackerRolls, defenderRolls, attackerLost, defenderLost)
  }

  /** Simulate randomly rolling n dice.
    *
    * @param dice the number of dice to roll
    * @return a list of die rolls
    */
  def rollDice(dice: Int): Seq[Int] = {
    val dieSize = 6
    Seq.fill(dice)(1 + Random.nextInt(dieSize)).sorted(Ordering[Int].reverse)
  }
}

/** Data type representing the current state of the game
  *
  * @param turn    the current position in the turn order
  * @param players the players in the game
  */
case class GameInfo(turn: Int, players: Seq[Player], activePlayer: Int, gameState: String)

