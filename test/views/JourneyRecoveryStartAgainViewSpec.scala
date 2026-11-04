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

import controllers.routes
import play.twirl.api.Html
import utils.ViewTestHelpers
import views.html.JourneyRecoveryStartAgainView

class JourneyRecoveryStartAgainViewSpec extends ViewTestHelpers {

  implicit val viewTest: ViewTest = new ViewTest {
    override val page: Html          = inject[JourneyRecoveryStartAgainView].apply()
    override val pageTitle: String   = "Sorry, there is a problem with the service"
    override val pageHeading: String = "Sorry, there is a problem with the service"
  }

  "JourneyRecoveryStartAgain view" should {

    checkTitle()
    checkHeading()
    checkContentOnPage("[Add content to explain why the user needs to start again.]", ".govuk-body:nth-of-type(1)")
    checkLinkOnPage("Start again", routes.IndexController.onPageLoad().url, ".govuk-button")
  }
}
