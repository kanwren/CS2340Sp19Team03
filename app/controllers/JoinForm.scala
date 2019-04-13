package controllers

/** The form on the index page used to join a game */
object JoinForm {

  import play.api.data.Form
  import play.api.data.Forms._

  /** Type representing a request made from this form.
    * @param id the ID of the game to join
    * @param playerName the name of the player
    */
  case class JoinRequest(id: String, playerName: String)

  /** Tests whether a given string is a valid player name.
    * @param name the string to validate
    * @return true if the string represents a valid player name
    */
  def validateName(name: String): Boolean = "=,;".forall(!name.contains(_))

  val form: Form[JoinRequest] = Form(
    mapping(
      "gameId" -> nonEmptyText,
      "name" -> nonEmptyText.verifying(validateName _)
    )(JoinRequest.apply)(JoinRequest.unapply)
  )
}

