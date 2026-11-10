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
import views.html.liability.LiabilityIntroductionView

class LiabilityIntroductionViewSpec extends ViewTestHelpers {

  implicit val viewTest: ViewTest = new ViewTest {
    override val page: Html          = inject[LiabilityIntroductionView].apply()
    override val pageTitle: String   = "We need to work out the date your liability for CBAM started"
    override val pageHeading: String = "We need to work out the date your liability for CBAM started"
  }

  "LiabilityIntroduction view" should {
    basicChecks(hasBackLink = false)

    checkContentOnPage(
      "We’ll ask you a few questions to work out when you became liable to register.",
      ".govuk-body:nth-of-type(1)"
    )

    checkContentOnPage(
      "Your liability to register for CBAM starts on one of these dates:",
      ".govuk-body:nth-of-type(2)"
    )
    checkContentOnPage(
      "when you first expected to import CBAM goods worth £50,000 or more into the UK in the next 30 days",
      ".govuk-list--bullet:nth-of-type(1) > li:nth-child(1)"
    )
    checkContentOnPage(
      "when you imported CBAM goods worth £50,000 or more into the UK in the past 12 months, as assessed on the first day of each month",
      ".govuk-list--bullet:nth-of-type(1) > li:nth-child(2)"
    )

    checkContentOnPage(
      "If both scenarios apply, your liability to register for CBAM starts on the earlier date.",
      ".govuk-body:nth-of-type(3)"
    )

    checkLinkOnPage(
      "Find out more about when liability to register for CBAM starts (opens in new tab)",
      "https://www.gov.uk/guidance/work-out-the-date-youll-need-to-register-for-carbon-border-adjustment-mechanism-cbam",
      ".govuk-body:nth-of-type(4) > a"
    )

    checkLinkOnPage(
      "Continue",
      controllers.liability.routes.LiabilityIntroductionController.onPageLoad().url,
      ".govuk-button"
    )
  }
}
