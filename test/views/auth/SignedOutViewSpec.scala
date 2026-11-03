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

package views.auth

import controllers.routes
import play.twirl.api.Html
import utils.ViewTestHelpers
import views.html.auth.SignedOutView

class SignedOutViewSpec extends ViewTestHelpers {

  implicit val viewTest: ViewTest = new ViewTest {
    override val page: Html          = inject[SignedOutView].apply()
    override val pageTitle: String   = "For your security, we signed you out"
    override val pageHeading: String = "For your security, we signed you out"
  }

  "SignedOut view" should {

    checkTitle()
    checkHeading()
    checkContentOnPage("We did not save your answers.", ".govuk-body:nth-of-type(1)")
    checkLinkOnPage("Sign in", routes.IndexController.onPageLoad().url, ".govuk-button")
  }
}
