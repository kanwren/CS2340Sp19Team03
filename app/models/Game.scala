package models
import scala.collection.mutable
import scala.util.Random

/**
 * Presentation object used for displaying data in a template.
 *
 * Note that it's a good practice to keep the presentation DTO,
 * which are used for reads, distinct from the form processing DTO,
 * which are used for writes.
 */
class Game(val gameId: String) {
  private val lobbiedPlayers: mutable.ArrayBuffer[String] = mutable.ArrayBuffer[String]()
  var players: Seq[Player] = Seq[Player]()
  val turn: Int = 0
  var gameState: GameState = Lobbying
  val board: Board = new Board()

  def addPlayerToLobby(name: String): Unit = lobbiedPlayers += name

  def startGame(): Unit = {
    gameState = Running
    val armies = 50 - 5 * lobbiedPlayers.length
    players = Random.shuffle(for {
      name <- lobbiedPlayers
    } yield Player(name, armies, gameId))
  }

  def getLobbiedPlayers: Seq[String] = lobbiedPlayers.toSeq

}

object Game {
  val idLength: Int = 4
}
