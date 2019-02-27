package controllers

object JoinForm {
    import play.api.data.Forms._
    import play.api.data.Form

    case class GameId(id: String)

    /**
      * The form definition for the "create a widget" form.
      * It specifies the form fields and their types,
      * as well as how to convert from a Data to form data and vice versa.
      */
    val form: Form[GameId] = Form(
        mapping(
            "gameId" -> nonEmptyText
        )(GameId.apply)(GameId.unapply)
    )
}

