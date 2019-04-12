package controllers

import javax.inject.Inject
import models.{Game, GameManager, Player, Running}
import play.api.mvc._

class GameBoardController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) with ControllerUtils {

  val testGame: Game = GameManager.getGameById(GameManager.makeNewGame).get
  val playerCount: Int = 35
  testGame.gameState = Running
  testGame.players = Seq("A", "B", "C", "D").map(Player(_, playerCount, testGame.gameId))
  def boardTest(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.gameboard(testGame))
  }
}
