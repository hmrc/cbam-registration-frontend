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

package views

import play.twirl.api.Html
import utils.ViewTestHelpers
import views.html.CannotRegisterIndividualView

class CannotRegisterIndividualViewSpec extends ViewTestHelpers {

  implicit lazy val viewTest: ViewTest = new ViewTest {
    override val page: Html          = inject[CannotRegisterIndividualView].apply()
    override val pageTitle: String   = "You cannot register for CBAM with an individual account"
    override val pageHeading: String = "You cannot register for CBAM with an individual account"
  }

  "CannotRegisterIndividualView" should {
    basicChecks(hasBackLink = false)

    checkContentOnPage(
      "You’ve signed in with an individual account.",
      "#main-content > div > div > p:nth-child(2)"
    )
    checkContentOnPage(
      "To register your organisation for CBAM, sign in using your organisation’s account details.",
      "#main-content > div > div > p:nth-child(3)"
    )
    checkLinkOnPage(
      "Find out who must register for CBAM (opens in new tab)",
      "https://www.gov.uk/government/collections/check-if-youll-need-to-register-for-carbon-border-adjustment-mechanism-cbam",
      "#main-content > div > div > p:nth-child(4) > a"
    )
  }

}
