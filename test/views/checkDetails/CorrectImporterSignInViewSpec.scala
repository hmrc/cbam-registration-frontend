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

import play.twirl.api.Html
import utils.ViewTestHelpers
import views.html.checkDetails.CorrectImporterSignInView

class CorrectImporterSignInViewSpec extends ViewTestHelpers {

  implicit val viewTest: ViewTest = new ViewTest {
    override val page: Html          = inject[CorrectImporterSignInView].apply()
    override val pageTitle: String   = "Sign in to the account for the importer that must register for CBAM"
    override val pageHeading: String = "Sign in to the account for the importer that must register for CBAM"
  }

  "CorrectImporterSignInView" should {
    basicChecks()

    checkContentOnPage(
      "If you need to register a different importer, sign out and sign in with the account linked to that importer.",
      ".govuk-body:nth-of-type(1)"
    )

    checkLinkOnPage(
      "Find out more about which importers must register for CBAM (opens in new tab)",
      "https://www.gov.uk/government/collections/carbon-border-adjustment-mechanism#check-if-you'll-need-to-register",
      ".govuk-body:nth-of-type(2) > a"
    )

    checkLinkOnPage(
      "Sign out",
      viewTest.appConfig.signInToStartUrl,
      ".govuk-button"
    )
  }
}
