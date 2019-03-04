package controllers

object JoinForm {

  import play.api.data.Form
  import play.api.data.Forms._

  case class GameId(id: String)

  val form: Form[GameId] = Form(
    mapping(
      "gameId" -> nonEmptyText
    )(GameId.apply)(GameId.unapply)
  )
}

