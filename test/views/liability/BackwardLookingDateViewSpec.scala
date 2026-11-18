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

import controllers.liability.routes
import forms.liability.BackwardLookingDateFormProvider
import models.NormalMode
import play.api.data.{Form, FormError}
import play.twirl.api.Html
import utils.ViewTestHelpers
import views.html.liability.BackwardLookingDateView

import java.time.LocalDate

class BackwardLookingDateViewSpec extends ViewTestHelpers {

  val title = "When did your imports of CBAM goods reach £50,000 or more within the past 12 months?"

  def createViewTest(formError: Option[FormError] = None): ViewTest =
    new ViewTest {
      val form: Form[LocalDate]        = formError.fold(
        BackwardLookingDateFormProvider()()
      )(error => BackwardLookingDateFormProvider()().withError(error))
      override val page: Html          = inject[BackwardLookingDateView].apply(form, NormalMode)
      override val pageTitle: String   = formError.fold("")(_ => "Error: ") + title
      override val pageHeading: String = title
    }

  "BackwardLookingDateView" should {

    "render correctly" when {

      "there are no form errors" which {
        implicit val viewTest: ViewTest = createViewTest()

        basicChecks()
        checkDateForm("", "", "", hintText = Some("For example, 27 3 2027"))
        checkFormCall(routes.BackwardLookingDateController.onSubmit(NormalMode).url)

        checkButton("Save and continue", ".govuk-button")
        checkLinkOnPage(
          "Save and come back later",
          "/todo-answers-saved",
          ".govuk-button-group > a"
        )
      }

      "there is a form error" which {
        implicit val viewTest: ViewTest = createViewTest(
          Some(FormError("value.day", "Summet gon wrong"))
        )

        basicChecks()
        checkErrorSummary("Summet gon wrong")
        checkDateForm("", "", "", hintText = Some("For example, 27 3 2027"))
        checkFormCall(routes.BackwardLookingDateController.onSubmit(NormalMode).url)

        checkButton("Save and continue", ".govuk-button")
        checkLinkOnPage(
          "Save and come back later",
          "/todo-answers-saved",
          ".govuk-button-group > a"
        )
      }
    }

  }

}
