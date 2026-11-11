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
import forms.ForwardLookingThresholdFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.Html
import utils.ViewTestHelpers
import views.html.liability.ForwardLookingThresholdView

class ForwardLookingThresholdViewSpec extends ViewTestHelpers {

  val title = "Do you expect to import CBAM goods that are valued at £50,000 or more within the next 30 days?"

  "ForwardLookingThreshold view" when {

    "there are no errors in the form" should {

      implicit val viewTest: ViewTest = new ViewTest {
        val form: Form[Boolean]          = ForwardLookingThresholdFormProvider()()
        override val page: Html          = inject[ForwardLookingThresholdView].apply(form, NormalMode)
        override val pageTitle: String   = title
        override val pageHeading: String = title
      }

      basicChecks()

      checkContentOnPage("Do not include goods imported before 1 January 2027", ".govuk-hint")

      checkRadioButton(
        content = "Yes",
        selected = false,
        selector = ".govuk-radios__item:nth-of-type(1)"
      )
      checkRadioButton(
        content = "No",
        selected = false,
        selector = ".govuk-radios__item:nth-of-type(2)"
      )

      checkFormCall(routes.ForwardLookingThresholdController.onSubmit(NormalMode).url)

      checkButton("Save and continue", ".govuk-button")
      checkLinkOnPage("Save and come back later", "/todo", ".govuk-button-group > .govuk-link")
    }

    "there are errors in the form" should {

      implicit val viewTest: ViewTest = new ViewTest {
        val form: Form[Boolean]          = ForwardLookingThresholdFormProvider()().bind(Map("value" -> ""))
        override val page: Html          = inject[ForwardLookingThresholdView].apply(form, NormalMode)
        override val pageTitle: String   = s"Error: $title"
        override val pageHeading: String = title
      }

      basicChecks()

      checkErrorSummary(
        "Select yes if you expect to import CBAM goods valued at £50,000 or more within the next 30 days"
      )
      checkErrorMessageOnForm(
        "Select yes if you expect to import CBAM goods valued at £50,000 or more within the next 30 days"
      )
    }
  }
}
