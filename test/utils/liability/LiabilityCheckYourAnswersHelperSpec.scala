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

package utils.liability

import base.SpecBase
import models.CheckMode
import pages.{BackwardLookingDatePage, BackwardLookingThresholdPage, ExpectToExceedThresholdPage, ForwardLookingThresholdPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, SummaryList}

import java.time.LocalDate

class LiabilityCheckYourAnswersHelperSpec extends SpecBase {

  val helper = new LiabilityCheckYourAnswersHelper

  implicit val messages: Messages = messages(app)

  ".getSummaryList" must {

    "return None when user answers are missing the required data" in {
      val userAnswers1 = emptyUserAnswers
        .withPage(ForwardLookingThresholdPage, true)

      val userAnswers2 = emptyUserAnswers
        .withPage(ForwardLookingThresholdPage, true)
        .withPage(ExpectToExceedThresholdPage, LocalDate.now(clock))

      val userAnswers3 = emptyUserAnswers
        .withPage(ForwardLookingThresholdPage, true)
        .withPage(ExpectToExceedThresholdPage, LocalDate.now(clock))
        .withPage(BackwardLookingThresholdPage, true)

      val userAnswers4 = emptyUserAnswers
        .withPage(ForwardLookingThresholdPage, false)
        .withPage(BackwardLookingThresholdPage, false)

      val userAnswers5 = emptyUserAnswers
        .withPage(ForwardLookingThresholdPage, false)
        .withPage(BackwardLookingThresholdPage, true)

      Seq(emptyUserAnswers, userAnswers1, userAnswers2, userAnswers3, userAnswers4, userAnswers5).foreach {
        userAnswers =>
          helper.getSummaryList(userAnswers) mustBe None
      }
    }

    "return a summary list" when {
      "only the forward looking threshold is met" in {
        val userAnswers = emptyUserAnswers
          .withPage(ForwardLookingThresholdPage, true)
          .withPage(ExpectToExceedThresholdPage, LocalDate.now(clock))
          .withPage(BackwardLookingThresholdPage, false)

        val expectedKeys = List(
          "Do you expect to import CBAM goods that are valued at £50,000 or more within the next 30 days?",
          "When did you first expect to import CBAM goods with a total value of £50,000 or more?",
          "Have you imported CBAM goods that are valued at £50,000 or more in the past 12 months?"
        )

        val expectedValues = List("Yes", "11 June 2024", "No")

        val expectedActions = Seq(
          ActionItem(
            content = Text("Change"),
            href = controllers.liability.routes.ForwardLookingThresholdController.onPageLoad(CheckMode).url,
            visuallyHiddenText =
              Some("whether you expect to import CBAM goods that are valued at £50,000 or more within the next 30 days")
          ),
          ActionItem(
            content = Text("Change"),
            href = controllers.liability.routes.ExpectToExceedThresholdController.onPageLoad(CheckMode).url,
            visuallyHiddenText =
              Some("when you first expected to import CBAM goods with a total value of £50,000 or more")
          ),
          ActionItem(
            content = Text("Change"),
            href = controllers.liability.routes.BackwardLookingThresholdController.onPageLoad(CheckMode).url,
            visuallyHiddenText =
              Some("whether you imported CBAM goods that are valued at £50,000 or more in the past 12 months")
          )
        )

        val summaryList: SummaryList =
          helper.getSummaryList(userAnswers).getOrElse(fail("Expected a summary list to be returned"))

        summaryList.rows.map(_.key.content.asHtml.toString)   mustBe expectedKeys
        summaryList.rows.map(_.value.content.asHtml.toString) mustBe expectedValues
        summaryList.rows.map(_.actions.get.items.head)        mustBe expectedActions
      }

      "only the backward looking threshold is met" in {
        val userAnswers = emptyUserAnswers
          .withPage(ForwardLookingThresholdPage, false)
          .withPage(BackwardLookingThresholdPage, true)
          .withPage(BackwardLookingDatePage, LocalDate.now(clock))

        val expectedKeys = List(
          "Do you expect to import CBAM goods that are valued at £50,000 or more within the next 30 days?",
          "Have you imported CBAM goods that are valued at £50,000 or more in the past 12 months?",
          "When did your imports of CBAM goods reach £50,000 or more within the past 12 months?"
        )

        val expectedValues = List("No", "Yes", "11 June 2024")

        val expectedActions = Seq(
          ActionItem(
            content = Text("Change"),
            href = controllers.liability.routes.ForwardLookingThresholdController.onPageLoad(CheckMode).url,
            visuallyHiddenText =
              Some("whether you expect to import CBAM goods that are valued at £50,000 or more within the next 30 days")
          ),
          ActionItem(
            content = Text("Change"),
            href = controllers.liability.routes.BackwardLookingThresholdController.onPageLoad(CheckMode).url,
            visuallyHiddenText =
              Some("whether you imported CBAM goods that are valued at £50,000 or more in the past 12 months")
          ),
          ActionItem(
            content = Text("Change"),
            href = controllers.liability.routes.BackwardLookingDateController.onPageLoad(CheckMode).url,
            visuallyHiddenText =
              Some("when your imports of CBAM goods reached £50,000 or more within the past 12 months")
          )
        )

        val summaryList: SummaryList =
          helper.getSummaryList(userAnswers).getOrElse(fail("Expected a summary list to be returned"))

        summaryList.rows.map(_.key.content.asHtml.toString)   mustBe expectedKeys
        summaryList.rows.map(_.value.content.asHtml.toString) mustBe expectedValues
        summaryList.rows.map(_.actions.get.items.head)        mustBe expectedActions
      }

      "the forward and backward looking thresholds are met" in {
        val userAnswers = emptyUserAnswers
          .withPage(ForwardLookingThresholdPage, true)
          .withPage(ExpectToExceedThresholdPage, LocalDate.now(clock))
          .withPage(BackwardLookingThresholdPage, true)
          .withPage(BackwardLookingDatePage, LocalDate.now(clock).plusDays(1))

        val expectedKeys = List(
          "Do you expect to import CBAM goods that are valued at £50,000 or more within the next 30 days?",
          "When did you first expect to import CBAM goods with a total value of £50,000 or more?",
          "Have you imported CBAM goods that are valued at £50,000 or more in the past 12 months?",
          "When did your imports of CBAM goods reach £50,000 or more within the past 12 months?"
        )

        val expectedValues = List("Yes", "11 June 2024", "Yes", "12 June 2024")

        val expectedActions = Seq(
          ActionItem(
            content = Text("Change"),
            href = controllers.liability.routes.ForwardLookingThresholdController.onPageLoad(CheckMode).url,
            visuallyHiddenText =
              Some("whether you expect to import CBAM goods that are valued at £50,000 or more within the next 30 days")
          ),
          ActionItem(
            content = Text("Change"),
            href = controllers.liability.routes.ExpectToExceedThresholdController.onPageLoad(CheckMode).url,
            visuallyHiddenText =
              Some("when you first expected to import CBAM goods with a total value of £50,000 or more")
          ),
          ActionItem(
            content = Text("Change"),
            href = controllers.liability.routes.BackwardLookingThresholdController.onPageLoad(CheckMode).url,
            visuallyHiddenText =
              Some("whether you imported CBAM goods that are valued at £50,000 or more in the past 12 months")
          ),
          ActionItem(
            content = Text("Change"),
            href = controllers.liability.routes.BackwardLookingDateController.onPageLoad(CheckMode).url,
            visuallyHiddenText =
              Some("when your imports of CBAM goods reached £50,000 or more within the past 12 months")
          )
        )

        val summaryList: SummaryList =
          helper.getSummaryList(userAnswers).getOrElse(fail("Expected a summary list to be returned"))

        summaryList.rows.map(_.key.content.asHtml.toString)   mustBe expectedKeys
        summaryList.rows.map(_.value.content.asHtml.toString) mustBe expectedValues
        summaryList.rows.map(_.actions.get.items.head)        mustBe expectedActions
      }
    }
  }
}
