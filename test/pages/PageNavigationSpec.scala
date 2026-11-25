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
import play.api.mvc.Call

import java.time.LocalDate

class PageNavigationSpec extends SpecBase {

  "Page navigation" when {

    "in Normal mode" must {

      "go from the ForwardLookingThreshold page to the next page" when {

        "the answer is yes" in {
          val answers = emptyUserAnswers.withPage(ForwardLookingThresholdPage, true)
          ForwardLookingThresholdPage.nextPage(NormalMode, answers) mustBe Call(
            "GET",
            "/cbam/registration/date-expect-to-import"
          )
        }

        "the answer is no" in {
          val answers = emptyUserAnswers.withPage(ForwardLookingThresholdPage, false)
          ForwardLookingThresholdPage.nextPage(
            NormalMode,
            answers
          ) mustBe controllers.liability.routes.BackwardLookingThresholdController.onPageLoad(NormalMode)
        }

        "there is no expected answer" in {
          val answers = emptyUserAnswers
          ForwardLookingThresholdPage.nextPage(NormalMode, answers) mustBe routes.JourneyRecoveryController
            .onPageLoad()
        }
      }

      "go from the ExpectToExceedThreshold page to the next page" when {

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

      "go from the BackwardLookingThreshold page to the next page" when {

        "the answer is yes" in {
          val answers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, true)
            .withPage(BackwardLookingThresholdPage, true)

          BackwardLookingThresholdPage.nextPage(NormalMode, answers) mustBe
            controllers.liability.routes.BackwardLookingDateController.onPageLoad(NormalMode)
        }

        "the answer is no and the answer for the ForwardLookingThreshold page was yes" in {
          val answers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, true)
            .withPage(BackwardLookingThresholdPage, false)

          BackwardLookingThresholdPage.nextPage(NormalMode, answers) mustBe
            controllers.liability.routes.LiabilityCheckYourAnswersController.onPageLoad()
        }

        "the answer is no and the answer for the ForwardLookingThreshold page was no" in {
          val answers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, false)
            .withPage(BackwardLookingThresholdPage, false)

          BackwardLookingThresholdPage.nextPage(
            NormalMode,
            answers
          ) mustBe controllers.liability.routes.DoNotRegisterController.onPageLoad()
        }

        "there is no expected answer for ForwardLookingThreshold page" in {
          val answers = emptyUserAnswers.withPage(BackwardLookingThresholdPage, true)

          BackwardLookingThresholdPage.nextPage(NormalMode, answers) mustBe routes.JourneyRecoveryController
            .onPageLoad()
        }

        "there are no expected answers" in {
          val answers = emptyUserAnswers
          BackwardLookingThresholdPage.nextPage(NormalMode, answers) mustBe routes.JourneyRecoveryController
            .onPageLoad()
        }
      }

      "go from the BackwardLookingDate page to the next page" when {

        "the provided date is valid" in {
          val answers = emptyUserAnswers.withPage(BackwardLookingDatePage, LocalDate.now())
          BackwardLookingDatePage.nextPage(NormalMode, answers) mustBe
            controllers.liability.routes.LiabilityCheckYourAnswersController.onPageLoad()
        }

        "there is no date in the user answers" in {
          val answers = emptyUserAnswers
          BackwardLookingDatePage.nextPage(NormalMode, answers) mustBe routes.JourneyRecoveryController
            .onPageLoad()
        }
      }
    }

    "in Check mode" must {

      "go from the ForwardLookingThreshold page to the next page" when {

        "the answer has not changed" in {
          val answers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, true)
            .withPage(ExpectToExceedThresholdPage, LocalDate.now())
            .withPage(BackwardLookingThresholdPage, true)
            .withPage(BackwardLookingDatePage, LocalDate.now())

          ForwardLookingThresholdPage.nextPage(CheckMode, answers, hasAnswerChanged = false) mustBe
            controllers.liability.routes.LiabilityCheckYourAnswersController.onPageLoad()
        }

        "the answer has changed" when {
          "the answer is yes" in {
            val answers = emptyUserAnswers
              .withPage(ForwardLookingThresholdPage, true)
              .withPage(BackwardLookingThresholdPage, true)
              .withPage(BackwardLookingDatePage, LocalDate.now())

            ForwardLookingThresholdPage.nextPage(CheckMode, answers) mustBe
              controllers.liability.routes.ExpectToExceedThresholdController.onPageLoad(CheckMode)
          }

          "the answer is no and the answer for the BackwardLookingThreshold page was yes" in {
            val answers = emptyUserAnswers
              .withPage(ForwardLookingThresholdPage, false)
              .withPage(BackwardLookingThresholdPage, true)
              .withPage(BackwardLookingDatePage, LocalDate.now())

            ForwardLookingThresholdPage.nextPage(CheckMode, answers) mustBe
              controllers.liability.routes.LiabilityCheckYourAnswersController.onPageLoad()
          }

          "the answer is no and the answer for the BackwardLookingThreshold page was no" in {
            val answers = emptyUserAnswers
              .withPage(ForwardLookingThresholdPage, false)
              .withPage(BackwardLookingThresholdPage, false)

            ForwardLookingThresholdPage.nextPage(CheckMode, answers) mustBe
              controllers.liability.routes.DoNotRegisterController.onPageLoad()
          }

          "there is no expected answer for the BackwardLookingThreshold page" in {
            val answers = emptyUserAnswers.withPage(ForwardLookingThresholdPage, true)

            BackwardLookingThresholdPage.nextPage(CheckMode, answers) mustBe routes.JourneyRecoveryController
              .onPageLoad()
          }

          "there is no expected answer for the ForwardLookingThreshold page" in {
            val answers = emptyUserAnswers.withPage(BackwardLookingThresholdPage, true)
            BackwardLookingThresholdPage.nextPage(CheckMode, answers) mustBe routes.JourneyRecoveryController
              .onPageLoad()
          }
        }
      }

      "go from the ExpectToExceedThreshold page to the next page" when {

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

      "go from the BackwardLookingThreshold page to the next page" when {

        "the answer has not changed" in {
          val answers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, true)
            .withPage(ExpectToExceedThresholdPage, LocalDate.now())
            .withPage(BackwardLookingThresholdPage, true)
            .withPage(BackwardLookingDatePage, LocalDate.now())

          BackwardLookingThresholdPage.nextPage(CheckMode, answers, hasAnswerChanged = false) mustBe
            controllers.liability.routes.LiabilityCheckYourAnswersController.onPageLoad()
        }

        "the answer has changed" when {
          "the answer is yes" in {
            val answers = emptyUserAnswers
              .withPage(ForwardLookingThresholdPage, true)
              .withPage(BackwardLookingThresholdPage, true)
              .withPage(BackwardLookingDatePage, LocalDate.now())

            BackwardLookingThresholdPage.nextPage(CheckMode, answers) mustBe
              controllers.liability.routes.BackwardLookingDateController.onPageLoad(CheckMode)
          }

          "the answer is no and the answer for the ForwardLookingThreshold page was yes" in {
            val answers = emptyUserAnswers
              .withPage(ForwardLookingThresholdPage, true)
              .withPage(ExpectToExceedThresholdPage, LocalDate.now())
              .withPage(BackwardLookingThresholdPage, false)

            BackwardLookingThresholdPage.nextPage(CheckMode, answers) mustBe
              controllers.liability.routes.LiabilityCheckYourAnswersController.onPageLoad()
          }

          "the answer is no and the answer for the ForwardLookingThreshold page was no" in {
            val answers = emptyUserAnswers
              .withPage(ForwardLookingThresholdPage, false)
              .withPage(BackwardLookingThresholdPage, false)

            BackwardLookingThresholdPage.nextPage(CheckMode, answers) mustBe
              controllers.liability.routes.DoNotRegisterController.onPageLoad()
          }

          "there is no expected answer for the ForwardLookingThreshold page" in {
            val answers = emptyUserAnswers.withPage(BackwardLookingThresholdPage, true)
            BackwardLookingThresholdPage.nextPage(CheckMode, answers) mustBe routes.JourneyRecoveryController
              .onPageLoad()
          }

          "there is no expected answer for the BackwardLookingThresholdPage page" in {
            val answers = emptyUserAnswers.withPage(ForwardLookingThresholdPage, true)
            BackwardLookingThresholdPage.nextPage(CheckMode, answers) mustBe routes.JourneyRecoveryController
              .onPageLoad()
          }
        }
      }

      "go from the BackwardLookingDate page to the next page" when {

        "the provided date is valid" in {
          val answers = emptyUserAnswers.withPage(BackwardLookingDatePage, LocalDate.now())
          BackwardLookingDatePage.nextPage(CheckMode, answers) mustBe
            controllers.liability.routes.LiabilityCheckYourAnswersController.onPageLoad()
        }

        "there is no date in the user answers" in {
          val answers = emptyUserAnswers
          BackwardLookingDatePage.nextPage(CheckMode, answers) mustBe routes.JourneyRecoveryController
            .onPageLoad()
        }
      }
    }
  }
}
