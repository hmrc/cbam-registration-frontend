package pages

import models.UserAnswers
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object $className$Page extends QuestionPage[BigDecimal] {
  
  override def path: JsPath = JsPath \ toString
  
  override def toString: String = "$className;format="decap"$"

  // TODO: Replace with the page to go to next in NormalMode
  override protected def normalModeNextPage(userAnswers: UserAnswers): Call =
    controllers.routes.ServiceEntryController.onPageLoad()

  // TODO: Replace with the page to go to next in CheckMode
  override protected def checkModeNextPage(userAnswers: UserAnswers, hasAnswerChanged: Boolean): Call =
    controllers.routes.JourneyRecoveryController.onPageLoad()
}
