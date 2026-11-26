/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pages

import models.{CheckMode, NormalMode, UserAnswers}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object ForwardLookingThresholdPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "forwardLookingThreshold"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    if (value.contains(false)) {
      userAnswers.remove(ExpectToExceedThresholdPage)
    } else {
      super.cleanup(value, userAnswers)
    }

  override protected def normalModeNextPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ForwardLookingThresholdPage) match {
      case Some(true)  => controllers.liability.routes.ExpectToExceedThresholdController.onPageLoad(NormalMode)
      case Some(false) => controllers.liability.routes.BackwardLookingThresholdController.onPageLoad(NormalMode)
      case _           => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def checkModeNextPage(userAnswers: UserAnswers, hasAnswerChanged: Boolean): Call =
    if (hasAnswerChanged) {
      (userAnswers.get(ForwardLookingThresholdPage), userAnswers.get(BackwardLookingThresholdPage)) match {
        case (Some(true), Some(_))      =>
          controllers.liability.routes.ExpectToExceedThresholdController.onPageLoad(CheckMode)
        case (Some(false), Some(true))  => controllers.liability.routes.LiabilityCheckYourAnswersController.onPageLoad()
        case (Some(false), Some(false)) => controllers.liability.routes.DoNotRegisterController.onPageLoad()
        case _                          => controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    } else {
      controllers.liability.routes.LiabilityCheckYourAnswersController.onPageLoad()
    }
}
