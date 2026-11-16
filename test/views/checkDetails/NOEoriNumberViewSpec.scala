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
import views.html.checkDetails.NoEORINumberView

class NOEoriNumberViewSpec extends ViewTestHelpers {

  val dateString = "11 July 2024"

  implicit val viewTest: ViewTest = new ViewTest {
    override val page: Html          = inject[NoEORINumberView].apply(dateString)
    override val pageTitle: String   = "We could not find an EORI number linked to this account"
    override val pageHeading: String = "We could not find an EORI number linked to this account"
  }

  "NoEORINumber view" should {

    basicChecks()
    checkContentOnPage(
      "If your organisation has an EORI number, you might be signed in to the wrong account.",
      ".govuk-body:nth-of-type(1)"
    )
    checkLinkOnPage(
      "Sign out and sign in with the account linked to the importer you need to register for CBAM",
      controllers.auth.routes.AuthController.signOutNoSurvey().url,
      ".govuk-body:nth-of-type(2) > a"
    )
    checkContentOnPage(
      "If your organisation does not have an EORI number that starts with GB, you must get one before registering for CBAM.",
      ".govuk-body:nth-of-type(3)"
    )
    checkLinkOnPage(
      "Get an EORI number starting with GB",
      "/todo-ecc-handoff",
      ".govuk-body:nth-of-type(4) > a"
    )
    checkContentOnPage(
      "It can take up to 5 working days for your EORI number application to be approved.",
      ".govuk-body:nth-of-type(5)"
    )
    checkContentOnPage(
      "After your EORI number application is approved, return to this service to finish your CBAM registration. Your answers will be saved until 11 July 2024.",
      ".govuk-body:nth-of-type(6)"
    )
  }
}
