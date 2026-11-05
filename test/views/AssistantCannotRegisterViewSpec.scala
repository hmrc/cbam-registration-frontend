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
import views.html.AssistantCannotRegisterView

class AssistantCannotRegisterViewSpec extends ViewTestHelpers {

  implicit val viewTest: ViewTest = new ViewTest {
    override val page: Html          = inject[AssistantCannotRegisterView].apply()
    override val pageTitle: String   = "You must be an administrator to register for CBAM"
    override val pageHeading: String = "You must be an administrator to register for CBAM"
  }

  "AssistantCannotRegister view" should {

    basicChecks(hasBackLink = false)
    checkContentOnPage(
      "You’re signed in with an organisation account that does not have administrator access. " +
        "Only an administrator can register your organisation for CBAM.",
      ".govuk-body:nth-of-type(1)"
    )
    checkContentOnPage(
      "Ask your organisation’s administrator to register for CBAM or give you administrator access so you can register.",
      ".govuk-body:nth-of-type(2)"
    )
  }
}
