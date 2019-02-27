// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package controllers

import javax.inject.Inject
import play.api.data._
import play.api.mvc._

class GameController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

	def index: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index(JoinForm.form))
	}

	def createGame: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val gameId = GameManager.makeNewGame
		Redirect(routes.PlayerLobbyController.listPlayers(gameId))
	}

	def startGame(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
		val game = GameManager.getGameById(gameId)
		game match {
			case None => Redirect(routes.GameController.index()).flashing("ERROR" -> s"Could not find game with game ID '$gameId'")
			case Some(g) =>
				g.startGame()
				Redirect(routes.GameController.showGame(gameId))
		}
	}

	def joinGame: Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
		val errorFunction = { formWithErrors: Form[JoinForm.GameId] =>
			BadRequest(views.html.index(formWithErrors))
		}

		def successFunction(gameId: JoinForm.GameId): Result = {
			GameManager.getGameById(gameId.id) match {
				case None =>
					Redirect(routes.GameController.index()).flashing("ERROR" -> s"Could not find game with game ID '${gameId.id}'")
				case Some(_) =>
          Redirect(routes.PlayerLobbyController.listPlayers(gameId.id))
			}
		}
		val formValidationResult = JoinForm.form.bindFromRequest
		formValidationResult.fold(errorFunction, successFunction)
	}

	def showGame(gameId: String): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
		val game = GameManager.getGameById(gameId)
    game match {
			case None => Redirect(routes.GameController.index()).flashing("ERROR" -> s"Could not find game with game ID '$gameId'")
			case Some(g) => Ok(views.html.game(g))
		}
	}

}
