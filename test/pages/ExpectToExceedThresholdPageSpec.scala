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

import base.SpecBase
import controllers.routes
import models.*

import java.time.LocalDate

class ExpectToExceedThresholdPageSpec extends SpecBase {

  "ExpectToExceedThresholdPage" when {

    "in Normal mode" must {

      "go to the next page" when {

        "the provided date is valid" in {
          val answers = emptyUserAnswers.withPage(ExpectToExceedThresholdPage, LocalDate.now())
          ExpectToExceedThresholdPage.nextPage(
            NormalMode,
            answers
          ) mustBe controllers.liability.routes.BackwardLookingThresholdController.onPageLoad(NormalMode)
        }

        "there is no date in the user answers" in {
          val answers = emptyUserAnswers
          ExpectToExceedThresholdPage.nextPage(NormalMode, answers) mustBe routes.JourneyRecoveryController
            .onPageLoad()
        }
      }
    }

    "in Check mode" must {

      "go to the next page" when {

        "the provided date is valid" in {
          val answers = emptyUserAnswers.withPage(ExpectToExceedThresholdPage, LocalDate.now())
          ExpectToExceedThresholdPage.nextPage(CheckMode, answers) mustBe
            controllers.liability.routes.LiabilityCheckYourAnswersController.onPageLoad()
        }

        "there is no date in the user answers" in {
          val answers = emptyUserAnswers
          ExpectToExceedThresholdPage.nextPage(CheckMode, answers) mustBe routes.JourneyRecoveryController
            .onPageLoad()
        }
      }
    }
  }
}
