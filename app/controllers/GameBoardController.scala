package controllers

import javax.inject.Inject

import models.Widget
import play.api.data._
import play.api.i18n._
import play.api.mvc._

class GameBoardController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  def showMap: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.gameboard())
  }
}