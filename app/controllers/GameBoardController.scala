package controllers

import javax.inject.Inject
import models.{Game, GameManager, Player, Running}
import play.api.mvc._

class GameBoardController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) with ControllerUtils {

  val testGame: Game = GameManager.getGameById(GameManager.makeNewGame).get
  testGame.gameState = Running
  testGame.players = Seq(new Player("A", 35, "abcd"), new Player("B", 35, "abcd"), new Player("C", 35, "abcd"), new Player("D", 35, "abcd"))
  def boardTest(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.gameboard(testGame))
  }
}
