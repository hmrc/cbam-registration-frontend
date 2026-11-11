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

import play.twirl.api.Html
import utils.ViewTestHelpers
import views.html.liability.DoNotRegisterView

class DoNotRegisterViewSpec extends ViewTestHelpers {

  implicit val viewTest: ViewTest = new ViewTest {
    override val page: Html          = inject[DoNotRegisterView].apply()
    override val pageTitle: String   = "You do not need to register for CBAM"
    override val pageHeading: String = "You do not need to register for CBAM"
  }

  "DoNotRegister view" should {
    basicChecks()

    checkContentOnPage("You must register for CBAM if your organisation:", ".govuk-body:nth-of-type(1)")
    checkContentOnPage(
      "expects to import CBAM goods that are valued at £50,000 or more within the next 30 days",
      ".govuk-list--bullet:nth-of-type(1) > li:nth-child(1)"
    )
    checkContentOnPage(
      "imported CBAM goods valued at £50,000 or more in the past 12 months, as assessed on the first day of each month",
      ".govuk-list--bullet:nth-of-type(1) > li:nth-child(2)"
    )

    checkContentOnPage(
      "If your circumstances change and you meet either of these conditions, you must register for CBAM.",
      ".govuk-body:nth-of-type(2)"
    )

    checkLinkOnPage(
      "Find out when you must register for CBAM (opens in new tab)",
      "https://www.gov.uk/government/collections/check-if-youll-need-to-register-for-carbon-border-adjustment-mechanism-cbam",
      ".govuk-body:nth-of-type(3) > a"
    )
  }
}
