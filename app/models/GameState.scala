package models

/** Represents the current stage of gameplay */
sealed trait GameState

/** The stage when players are joining the lobby */
case object Lobbying extends GameState

/** The stage when territories, armies, and turn orders are assigned to the player */
case object Assigning extends GameState

/** The main portion of the game */
case object Running extends GameState

/** The stage representing a game that has ended, and the player that won.
  *
  * @param winner the player that won the game
  */
case class Finished(winner: Player) extends GameState
