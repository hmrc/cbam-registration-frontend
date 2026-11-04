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
import views.html.AgentCannotRegisterView

class AgentCannotRegisterViewSpec extends ViewTestHelpers {

  implicit val viewTest: ViewTest = new ViewTest {
    override val page: Html          = inject[AgentCannotRegisterView].apply()
    override val pageTitle: String   = "Agents cannot register for CBAM"
    override val pageHeading: String = "You cannot register for CBAM with this account"
  }

  "AgentCannotRegister view" should {
    checkTitle()
    checkHeading()

    checkContentOnPage("You signed in with an agent services account.", ".govuk-body:nth-of-type(1)")
    checkContentOnPage("Your client must register for CBAM using their own account.", ".govuk-body:nth-of-type(2)")

    checkContentOnPage("After they’ve registered, you can manage their CBAM account by:", ".govuk-body:nth-of-type(3)")
    checkLinkOnPage(
      "signing in to agent services (opens in new tab)",
      "https://www.gov.uk/guidance/sign-in-to-your-agent-services-account",
      ".govuk-list--bullet:nth-of-type(1) > li:nth-child(1) > a"
    )
    checkContentOnPage(
      "requesting authorisation to manage your clients CBAM returns",
      ".govuk-list--bullet:nth-of-type(1) > li:nth-child(2)"
    )

    checkContentOnPage("Find out more about:", ".govuk-body:nth-of-type(4)")
    checkLinkOnPage(
      "using an agent services account (opens in new tab)",
      "https://www.gov.uk/government/collections/hmrc-services-for-tax-agents",
      ".govuk-list--bullet:nth-of-type(2) > li:nth-child(1) > a"
    )
    checkLinkOnPage(
      "who must register for CBAM (opens in new tab)",
      "https://www.gov.uk/government/collections/check-if-youll-need-to-register-for-carbon-border-adjustment-mechanism-cbam",
      ".govuk-list--bullet:nth-of-type(2) > li:nth-child(2) > a"
    )

    checkElementDoesNotExist(".govuk-back-link")
  }
}
