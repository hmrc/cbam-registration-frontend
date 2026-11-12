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

package views.checkDetails

import controllers.routes
import play.twirl.api.Html
import utils.ViewTestHelpers
import views.html.checkDetails.CheckOrganisationDetailsView

class CheckOrganisationDetailsViewSpec extends ViewTestHelpers {

  implicit val viewTest: ViewTest = new ViewTest {
    override val page: Html          = inject[CheckOrganisationDetailsView].apply()
    override val pageTitle: String   = "We need to check your organisation’s details"
    override val pageHeading: String = "We need to check your organisation’s details"
  }

  "CheckOrganisationDetails view" should {

    basicChecks()
    checkContentOnPage(
      "We’ll use the EORI number linked to your account to check the details we have for your organisation.",
      ".govuk-body"
    )
    checkLinkOnPage("Continue", "/todo", ".govuk-button")
  }
}
