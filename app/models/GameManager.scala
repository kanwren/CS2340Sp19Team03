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

import models.Game

import scala.collection.mutable
import scala.util.Random

object GameManager {
  val games: mutable.HashMap[String, Game] = mutable.HashMap[String, Game]()

  def getGameById(gameId: String): Option[Game] = games get gameId

  def makeNewGame: String = {
    val id = generateId
    games += id -> new Game(id)
    id
  }

  private def generateId: String = {
    val id = Random.alphanumeric.take(Game.idLength).mkString("")
    if (games contains id) {
      generateId
    }
    else {
      id
    }
  }
}
