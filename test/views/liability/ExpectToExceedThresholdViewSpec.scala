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

package views.liability

import forms.liability.ExpectToExceedThresholdFormProvider
import models.NormalMode
import play.api.data.{Form, FormError}
import play.twirl.api.Html
import utils.ViewTestHelpers
import views.html.liability.ExpectToExceedThresholdView

import java.time.LocalDate

class ExpectToExceedThresholdViewSpec extends ViewTestHelpers {

  val title = "When did you first expect to import CBAM goods with a total value of £50,000 or more?"

  def createViewTest(formError: Option[FormError] = None): ViewTest =
    new ViewTest {
      val form: Form[LocalDate]        = formError.fold(
        ExpectToExceedThresholdFormProvider()()
      )(error => ExpectToExceedThresholdFormProvider()().withError(error))
      override val page: Html          = inject[ExpectToExceedThresholdView].apply(form, NormalMode)
      override val pageTitle: String   = formError.fold("")(_ => "Error: ") + title
      override val pageHeading: String = title
    }

  "ExpectToExceedThreshold" should {

    "render correctly" when {

      "there are no form errors" which {
        implicit val viewTest: ViewTest = createViewTest()

        basicChecks()
        checkDateForm("", "", "")
        checkButton(
          "Continue",
          "#main-content > div > div > form > button"
        )
      }

      "there is a form error" which {
        implicit val viewTest: ViewTest = createViewTest(
          Some(FormError("value.day", "Summet gon wrong"))
        )

        basicChecks()
        checkErrorSummary("Summet gon wrong")
        checkDateForm("", "", "")
        checkButton(
          "Continue",
          "#main-content > div > div > form > button"
        )
      }
    }

  }

}
