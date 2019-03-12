package controllers

object JoinForm {

  import play.api.data.Form
  import play.api.data.Forms._

  case class JoinRequest(id: String, playerName: String)

  def validateName(name: String): Boolean = "=,;".forall(!name.contains(_))

  val form: Form[JoinRequest] = Form(
    mapping(
      "gameId" -> nonEmptyText,
      "name" -> nonEmptyText.verifying(validateName _)
    )(JoinRequest.apply)(JoinRequest.unapply)
  )
}

