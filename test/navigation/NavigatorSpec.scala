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

import base.SpecBase
import controllers.routes
import models.*
import pages.*
import play.api.mvc.Call

import java.time.LocalDate

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" when {

    "in Normal mode" must {

      "go from the ForwardLookingThreshold page to the next page" when {

        "the answer is yes" in {
          val answers = emptyUserAnswers.withPage(ForwardLookingThresholdPage, true)
          navigator.nextPage(ForwardLookingThresholdPage, NormalMode, answers) mustBe Call(
            "GET",
            "/cbam/registration/date-expect-to-import"
          )
        }

        "the answer is no" in {
          val answers = emptyUserAnswers.withPage(ForwardLookingThresholdPage, false)
          navigator.nextPage(
            ForwardLookingThresholdPage,
            NormalMode,
            answers
          ) mustBe controllers.liability.routes.BackwardLookingThresholdController.onPageLoad(NormalMode)
        }

        "there is no expected answer" in {
          val answers = emptyUserAnswers
          navigator.nextPage(ForwardLookingThresholdPage, NormalMode, answers) mustBe routes.JourneyRecoveryController
            .onPageLoad()
        }
      }

      "go from the ExpectToExceedThreshold page to the next page" when {

        "the provided date is valid" in {
          val answers = emptyUserAnswers.withPage(ExpectToExceedThresholdPage, LocalDate.now())
          navigator.nextPage(
            ExpectToExceedThresholdPage,
            NormalMode,
            answers
          ) mustBe controllers.liability.routes.BackwardLookingThresholdController.onPageLoad(NormalMode)
        }

        "there is no date in the user answers" in {
          val answers = emptyUserAnswers
          navigator.nextPage(ExpectToExceedThresholdPage, NormalMode, answers) mustBe routes.JourneyRecoveryController
            .onPageLoad()
        }
      }

      "go from the BackwardLookingThreshold page to the next page" when {

        "the answer is yes" in {
          val answers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, true)
            .withPage(BackwardLookingThresholdPage, true)

          navigator.nextPage(BackwardLookingThresholdPage, NormalMode, answers) mustBe Call(
            "GET",
            "/todo-13"
          )
        }

        "the answer is no and the answer for the ForwardLookingThreshold page was yes" in {
          val answers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, true)
            .withPage(BackwardLookingThresholdPage, false)

          navigator.nextPage(BackwardLookingThresholdPage, NormalMode, answers) mustBe Call(
            "GET",
            "/todo-14"
          )
        }

        "the answer is no and the answer for the ForwardLookingThreshold page was no" in {
          val answers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, false)
            .withPage(BackwardLookingThresholdPage, false)

          navigator.nextPage(
            BackwardLookingThresholdPage,
            NormalMode,
            answers
          ) mustBe controllers.liability.routes.DoNotRegisterController.onPageLoad()
        }

        "there is no expected answer for ForwardLookingThreshold page" in {
          val answers = emptyUserAnswers.withPage(BackwardLookingThresholdPage, true)

          navigator.nextPage(BackwardLookingThresholdPage, NormalMode, answers) mustBe routes.JourneyRecoveryController
            .onPageLoad()
        }
      }

      "go from a page that doesn't exist in the route map to ServiceEntryController" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers) mustBe routes.ServiceEntryController.onPageLoad()
      }
    }

    "in Check mode" must {

      "go from the ForwardLookingThreshold page to CheckYourAnswers" in {

        val answers = emptyUserAnswers.withPage(ForwardLookingThresholdPage, true)
        navigator.nextPage(ForwardLookingThresholdPage, CheckMode, answers) mustBe routes.CheckYourAnswersController
          .onPageLoad()
      }

      "go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, emptyUserAnswers) mustBe routes.CheckYourAnswersController
          .onPageLoad()
      }
    }
  }
}
