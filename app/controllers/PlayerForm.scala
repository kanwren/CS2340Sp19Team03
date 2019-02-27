package controllers

object PlayerForm {
    import play.api.data.Forms._
    import play.api.data.Form

    case class PlayerData(name: String)

    /**
      * The form definition for the "create a widget" form.
      * It specifies the form fields and their types,
      * as well as how to convert from a Data to form data and vice versa.
      */
    val form: Form[PlayerData] = Form(
        mapping(
            "name" -> nonEmptyText
        )(PlayerData.apply)(PlayerData.unapply)
    )
}
