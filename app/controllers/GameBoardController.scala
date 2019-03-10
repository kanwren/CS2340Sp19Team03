package controllers

import javax.inject.Inject

import play.api.mvc._

class GameBoardController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) with ControllerUtils
