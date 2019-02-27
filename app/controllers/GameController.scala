package controllers

import javax.inject._

import play.api._
import play.api.i18n._
import play.api.mvc._

import scala.collection.mutable.HashMap
import scala.util.Random
import PlayerInput._


class GameController @Inject()(cc: ControllerComponents) extends AbstractController(cc)  {


	val names = Random.shuffle(players)

	val playerMap = HashMap[String, Int]()

	val initialAllotment = 5 * (6 - names.length) + 20

	for (i <- names) {
		playerMap(i) = initialAllotment
	}

	def showGame = Action { implicit request: Request[AnyContent] =>
		val names = Random.shuffle(players)

	    val playerMap = HashMap[String, Int]()

	    val initialAllotment = 5 * (6 - names.length) + 20

	    for (i <- names) {
	    	playerMap(i) = initialAllotment
	    }
		Ok(views.html.game(playerMap, names))
	}

}