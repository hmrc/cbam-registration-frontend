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

package views.importDetails

import controllers.importDetails.routes
import forms.SectorsToImportFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import utils.ViewTestHelpers
import viewmodels.govuk.all.CheckboxItemViewModel
import views.html.importDetails.SectorsToImportView

class SectorsToImportViewSpec extends ViewTestHelpers {

  val title                         = "Which CBAM sectors do you expect to import goods from?"
  val checkboxes: Seq[CheckboxItem] = Seq(
    CheckboxItemViewModel(
      content = Text("Item1"),
      fieldId = "value",
      index = 0,
      value = "Item1"
    ),
    CheckboxItemViewModel(
      content = Text("Item2"),
      fieldId = "value",
      index = 1,
      value = "Item2"
    )
  )

  "SectorsToImport view" when {

    "there are no errors in the form" should {

      implicit val viewTest: ViewTest = new ViewTest {
        val form: Form[Set[String]]      = SectorsToImportFormProvider()()
        override val page: Html          = inject[SectorsToImportView].apply(form, checkboxes, NormalMode)
        override val pageTitle: String   = title
        override val pageHeading: String = title
      }

      basicChecks()

      checkContentOnPage("Sectors you do not select will be recorded as having 0kg of expected imports", ".govuk-hint")

      checkCheckBoxes(
        Seq(
          ListItem("Item1", "Item1"),
          ListItem("Item2", "Item2")
        ),
        selector = ".govuk-checkboxes"
      )

      checkFormCall(routes.SectorsToImportController.onSubmit(NormalMode).url)

      checkButton("Save and continue", ".govuk-button")
      checkLinkOnPage("Save and come back later", "/todo", ".govuk-button-group > .govuk-link")
    }

    "there are errors in the form" should {

      implicit val viewTest: ViewTest = new ViewTest {
        val form: Form[Set[String]]      = SectorsToImportFormProvider()().bind(Map("value" -> ""))
        override val page: Html          = inject[SectorsToImportView].apply(form, checkboxes, NormalMode)
        override val pageTitle: String   = s"Error: $title"
        override val pageHeading: String = title
      }

      basicChecks()

      checkErrorSummary(
        "Select which CBAM sectors you expect to import goods from"
      )
      checkErrorMessageOnForm(
        "Select which CBAM sectors you expect to import goods from"
      )
    }
  }
}
