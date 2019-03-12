package controllers

import javax.inject.Inject
import models.{Game, GameManager, Player, Running}
import play.api.mvc._

class GameBoardController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) with ControllerUtils {

  val testGame: Game = GameManager.getGameById(GameManager.makeNewGame).get
  val playerCount: Int = 35
  testGame.gameState = Running
  testGame.playerCount = Seq(
    new Player("A", playerCount, "abcd"),
    new Player("B", playerCount, "abcd"),
    new Player("C", playerCount, "abcd"),
    new Player("D", playerCount, "abcd")
  )
  def boardTest(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.gameboard(testGame))
  }
}
