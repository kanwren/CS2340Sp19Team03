package controllers

import javax.inject.Inject

import play.api.data._
import play.api.i18n._
import play.api.mvc._

class BoardController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  def viewBoard = Action { implicit request: MessagesRequest[AnyContent] =>
    // Pass an unpopulated form to the template
    Ok(views.html.gameboard())
  }
}
