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

package navigation

import controllers.routes
import models.*
import pages.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case ForwardLookingThresholdPage  =>
      userAnswers =>
        userAnswers.get(ForwardLookingThresholdPage) match {
          case Some(true)  => controllers.liability.routes.ExpectToExceedThresholdController.onPageLoad(NormalMode)
          case Some(false) => controllers.liability.routes.BackwardLookingThresholdController.onPageLoad(NormalMode)
          case _           => routes.JourneyRecoveryController.onPageLoad()
        }
    case ExpectToExceedThresholdPage  =>
      userAnswers =>
        userAnswers.get(ExpectToExceedThresholdPage) match {
          case Some(_) => controllers.liability.routes.BackwardLookingThresholdController.onPageLoad(NormalMode)
          case _       => routes.JourneyRecoveryController.onPageLoad()
        }
    case BackwardLookingThresholdPage =>
      userAnswers =>
        (userAnswers.get(BackwardLookingThresholdPage), userAnswers.get(ForwardLookingThresholdPage)) match {
          case (Some(true), Some(_))      =>
            controllers.liability.routes.BackwardLookingDateController.onPageLoad(NormalMode)
          case (Some(false), Some(true))  => Call("GET", "/todo-14") // TODO screen 14
          case (Some(false), Some(false)) => controllers.liability.routes.DoNotRegisterController.onPageLoad()
          case _                          => routes.JourneyRecoveryController.onPageLoad()
        }
    case BackwardLookingDatePage      =>
      userAnswers =>
        userAnswers.get(BackwardLookingDatePage) match {
          case Some(_) => Call("GET", "/todo-14") // TODO: Redirect to screen 14 (CDB-505)
          case _       => routes.JourneyRecoveryController.onPageLoad()
        }
    case _                            => _ => routes.ServiceEntryController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = { case _ =>
    _ => routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
  }
}
