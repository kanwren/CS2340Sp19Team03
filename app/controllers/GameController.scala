package controllers

import java.net.{URLDecoder, URLEncoder}

import akka.actor.ActorSystem
import akka.stream.Materializer
import javax.inject.Inject
import models._
import play.api.data._
import play.api.mvc._

import scala.util.Random

/** Controller that monitors and manages the pool of running games.
  *
  * @param cc     Implicitly injected `MessagesController`
  * @param system Implicitly injected `ActorSystem`
  * @param mat    Implicitly injected `Materializer`
  */
class GameController @Inject()(cc: MessagesControllerComponents)
                              (implicit system: ActorSystem, mat: Materializer) extends MessagesAbstractController(cc) with ControllerUtils {

  /** Create and redirect to a test game with a given number of players.
    *
    * @param players the number of players to add to the test game
    * @return a redirection to the new game's page
    */
  def testGame(players: Int): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    val game = GameManager.makeNewGame
    val playerNames: Seq[String] = ('A' to 'Z').map(_.toString).take(players)
    playerNames foreach game.addPlayerToLobby
    game.startAllotting()
    game.startPlay()
    Redirect(routes.GameController.showGame(game.gameId, Some("A")))
  }

  /** Randomly select a winner and automatically finish a game
    *
    * @param gameId the ID of the game to end
    * @return a redirection to the game's page
    */
  def randomWin(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    overGame(gameId) { game: Game =>
      val players = game.players
      val randIndex = Random.nextInt(players.size)
      game.gameState = Finished(players(randIndex))
    }
  }

  /** Loads the home page.
    *
    * @return a response that load the home page
    */
  def index: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index(JoinForm.form))
  }

  /** Creates a new game with a random game ID.
    *
    * @return a redirection to the home page with a message containing the game ID
    */
  def createGame: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    val game = GameManager.makeNewGame
    Redirect(routes.GameController.index()).flashing("INFO" -> s"Your game ID is: ${game.gameId}")
  }

  /** Reads form data to let a player join a game.
    *
    * @return a redirection to the game's page, if found, otherwise a redirection to an error page
    */
  def joinGame: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    def errorFunction(formWithErrors: Form[JoinForm.JoinRequest]) =
      BadRequest(views.html.index(formWithErrors))

    def successFunction(joinRequest: JoinForm.JoinRequest): Result =
      onGame(joinRequest.id) { game =>
        val name = joinRequest.playerName
        if (game.getLobbiedPlayers.contains(name)) {
          Redirect(routes.GameController.index()).flashing("ERROR" -> s"Player with name $name already in queue")
        } else {
          game.addPlayerToLobby(name)
          Redirect(routes.GameController.showGame(joinRequest.id, Some(name)))
        }
      }

    JoinForm.form.bindFromRequest.fold(errorFunction, successFunction)
  }

  /** Trigger the territory assignment phase in a given game.
    *
    * @param gameId the ID of the game to advance
    * @return a redirection to the game's page
    */
  def startAllotting(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    overGame(gameId)(_.startAllotting())
  }

  /** Trigger the beginning of gameplay in a given game.
    *
    * @param gameId the ID of the game to advance
    * @return a redirection to the game's page
    */
  def startPlay(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    overGame(gameId)(_.startPlay())
  }

  /** Display the game of a given page.
    *
    * @param gameId     the ID of the game to display
    * @param playerName the current player's name
    * @return a response with the game's page, depending on the state, or an error page if the state is invalid
    */
  def showGame(gameId: String, playerName: Option[String]): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    var pName: String = playerName.getOrElse(URLDecoder.decode(request.cookies("playerName").value, "UTF-8"))
    pName = URLEncoder.encode(pName, "UTF-8")
    onGame(gameId) { game: Game =>
      game.gameState match {
        case Lobbying =>
          Ok(views.html.lobby(game.getLobbiedPlayers, gameId)).withCookies(Cookie("playerName", pName)).bakeCookies()
        case Allotting =>
          Ok(views.html.game(game)).withCookies(Cookie("playerName", pName)).bakeCookies()
        case Assigning(_) | Attacking | Defending(_, _, _) | Relocating | Fortifying =>
          Ok(views.html.gameboard(game)).withCookies(Cookie("playerName", pName)).bakeCookies()
        case Finished(winner) =>
          Ok(views.html.finished(winner.name)).withCookies(Cookie("playerName", pName)).bakeCookies()
      }
    }
  }

  /** Trigger the end of the current turn for a given game.
    *
    * @param gameId the ID of the game to advance
    * @return a redirection to the game's page
    */
  def endTurn(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    overGame(gameId)(_.nextTurn())
  }

  /** Trigger a decrease in the current army count.
    *
    * @param gameId the ID of the game to change
    * @return a redirection to the game's page, or an error page if the army use is invalid
    */
  def useArmy(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      game.gameState match {
        case Assigning(n) if n > 0 =>
          game.gameState = Assigning(n - 1)
          Redirect(routes.GameController.showGame(gameId))
        case _ =>
          redirectInvalidGameState(gameId)
      }
    }
  }

  /** Add a given number of armies to some territory in a game.
    *
    * @param gameId      the ID of the game being queried
    * @param territoryId the ID of the territory to add armies to
    * @param amount      the amount of armies to add to the territory
    * @return a redirection to the game's page
    */
  def addArmiesToTerritory(gameId: String, territoryId: Int, amount: Int): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    overGame(gameId)(_.board.territories(territoryId).armies += amount)
  }

  /** Transfer a variable amount of armies across territories
    *
    * @param gameId   the game in which the transfer is occurring
    * @param sourceId the ID of the armies' source territory
    * @param destId   the ID of the armies' destination territory
    * @param amount   the amount of armies to transfer
    * @return a redirection to the game's page
    */
  def moveArmies(gameId: String, sourceId: Int, destId: Int, amount: Int): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    overGame(gameId) { game: Game =>
      val source = game.board.territories(sourceId)
      val dest = game.board.territories(destId)
      source.armies -= amount
      dest.armies += amount
    }
  }

  /** Trigger the beginning of an attack
    *
    * @param gameId the ID of the game of which to trigger the state change
    * @return a redirection to the game's page
    */
  def startAttackingPhase(gameId: String): Action[AnyContent] =
    Action { implicit request: MessagesRequest[AnyContent] =>
      overGame(gameId)(_.gameState = Attacking)
    }

  /** Resolve a battle by simulating a dice roll, and update territory armies
    * and owners accordingly. Superseded by `setAttackingDice` and
    * `setDefendingDice`.
    *
    * @param attackerDice         the number of dice of the attacker
    * @param defenderDice         the number of dice of the defender
    * @param attackingTerritoryId the ID of the attacking territory
    * @param defendingTerritoryId the ID of the defending territory
    * @param gameId               the ID of the current game
    * @return a JSON response containing the dice rolls and armies lost by each
    *         territory
    */
  def simulateDiceRoll(attackerDice: Int, defenderDice: Int, attackingTerritoryId: Int, defendingTerritoryId: Int, gameId: String): Action[AnyContent] =
    Action { implicit request: MessagesRequest[AnyContent] =>
      gameJsonRequest(gameId) { game: Game =>
        val attackingTerritory = game.board.territories(attackingTerritoryId)
        val defendingTerritory = game.board.territories(defendingTerritoryId)

        Game.resolveBattle(attackerDice, defenderDice, attackingTerritory, defendingTerritory)
      }
    }

  /** Handle information about attacker decision and begin defending stage
    *
    * @param gameId               the ID of the game in which the attack is taking place
    * @param attackerDice         the number of dice the attacker chose
    * @param attackingTerritoryId the ID of the attacking territory
    * @param defendingTerritoryId the ID of the territory being attacked
    * @return a redirection to the game's page
    */
  def setAttackingDice(gameId: String, attackerDice: Int, attackingTerritoryId: Int, defendingTerritoryId: Int): Action[AnyContent] =
    Action { implicit request: MessagesRequest[AnyContent] =>
      overGame(gameId) { game: Game =>
        val attackingTerritory = game.board.territories(attackingTerritoryId)
        val defendingTerritory = game.board.territories(defendingTerritoryId)

        game.activePlayer = game.playerTurn(game.board.territories(defendingTerritoryId).owner.get).get

        game.gameState = Defending(attackerDice, attackingTerritory, defendingTerritory)
      }
    }

  /** Handle information about defender decision and advance game state as necessary. The game begins army relocation if
    * either of the territories were conquered, or returns to the attacking phase otherwise.
    *
    * @param gameId       the ID of the game in which the attack is taking place
    * @param defenderDice the number of dice the defender chose
    * @return a redirection to the game's page
    */
  def setDefendingDice(gameId: String, defenderDice: Int): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    onGame(gameId) { game: Game =>
      game.gameState match {
        case Defending(attackerDice, attackingTerritory, defendingTerritory) =>
          Game.resolveBattle(attackerDice, defenderDice, attackingTerritory, defendingTerritory)

          val attacker: Player = attackingTerritory.owner.get

          game.activePlayer = game.playerTurn(attacker).get

          game.gameState =
            if (game.playerWon(attacker)) {
              Finished(attacker)
            } else if (defendingTerritory.armies == 0) {
              Relocating
            } else {
              Attacking
            }

          Redirect(routes.GameController.showGame(gameId))

        case _ => redirectInvalidGameState(gameId)
      }
    }
  }

  /** Signal the end of the attacking phase and the start of the fortifying phase.
    *
    * @param gameId the ID of the game whose state should be advanced
    * @return a redirection to the game's page
    */
  def startFortifyingPhase(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    overGame(gameId)(_.gameState = Fortifying)
  }

}
