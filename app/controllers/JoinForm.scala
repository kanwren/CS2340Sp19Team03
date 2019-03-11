package controllers

object JoinForm {

  import play.api.data.Form
  import play.api.data.Forms._

  case class JoinRequest(id: String, playerName: String)

  val form: Form[JoinRequest] = Form(
    mapping(
      "gameId" -> nonEmptyText,
      "name" -> nonEmptyText
    )(JoinRequest.apply)(JoinRequest.unapply)
  )
}

