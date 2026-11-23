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
import views.html.checkDetails.NeedToSubscribeCDSView

class NeedToSubscribeCDSViewSpec extends ViewTestHelpers {

  "NeedToSubscribeCDS view" should {
    val dateString = "17 December 2026"

    implicit val viewTest: ViewTest = new ViewTest {
      override val page: Html          = inject[NeedToSubscribeCDSView].apply(dateString)
      override val pageTitle: String   =
        "You need to subscribe to the Customs Declaration Service before registering for CBAM"
      override val pageHeading: String =
        "You need to subscribe to the Customs Declaration Service before registering for CBAM"
    }

    basicChecks()

    checkContentOnPage(
      "Subscribing to the Customs Declaration Service will allow you to do things such as:",
      ".govuk-body:nth-of-type(1)"
    )
    checkContentOnPage(
      "submit import and export declarations using software",
      ".govuk-list--bullet > li:nth-child(1)"
    )
    checkContentOnPage(
      "get your postponed import VAT statements or get your import VAT certificates so you can complete your VAT Return",
      ".govuk-list--bullet > li:nth-child(2)"
    )
    checkContentOnPage(
      "pay Customs Duty and import VAT",
      ".govuk-list--bullet > li:nth-child(3)"
    )

    checkContentOnPage(
      "It usually takes 2 hours to approve your subscription, but it can take up to 5 working days if we need to make more checks.",
      ".govuk-body:nth-of-type(2)"
    )

    checkContentOnPage(
      "After your subscription is approved, return to this service to finish your registration for CBAM. Your answers will be saved until 17 December 2026.",
      ".govuk-body:nth-of-type(3)"
    )

    checkContentOnPage(
      "If your organisation has already subscribed to the Customs Declaration Service, you might be signed in to the wrong account.",
      ".govuk-body:nth-of-type(4)"
    )

    checkLinkOnPage(
      "Sign out and sign in with the account linked to the importer you need to register for CBAM",
      viewTest.appConfig.signInToStartUrl,
      ".govuk-body:nth-of-type(5) > a"
    )

    checkLinkOnPage(
      "Subscribe to Customs Declaration Service",
      "/subscribe-to-do",
      ".govuk-button"
    )
  }
}
