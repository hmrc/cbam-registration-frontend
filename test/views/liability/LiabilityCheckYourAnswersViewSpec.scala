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

import common.TestData
import play.twirl.api.Html
import utils.ViewTestHelpers
import views.html.liability.LiabilityCheckYourAnswersView

class LiabilityCheckYourAnswersViewSpec extends ViewTestHelpers with TestData {

  implicit val viewTest: ViewTest = new ViewTest {
    override val page: Html          = inject[LiabilityCheckYourAnswersView].apply(testSummaryList)
    override val pageTitle: String   = "Confirm liability start date"
    override val pageHeading: String = "Confirm liability start date"
  }

  "LiabilityCheckYourAnswersView" should {
    basicChecks()

    checkContentOnPage("Question", ".govuk-summary-list__key")
    checkContentOnPage("Answer", ".govuk-summary-list__value")
    checkContentOnPage("Change", ".govuk-summary-list__actions")

    checkLinkOnPage("Confirm", controllers.liability.routes.LiabilityDateController.onPageLoad().url, ".govuk-button")
    checkLinkOnPage("Save and come back later", "/todo-answers-saved", ".govuk-button-group > .govuk-link")
  }
}
