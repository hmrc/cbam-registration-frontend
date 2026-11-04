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
import views.html.IndexView

class IndexViewSpec extends ViewTestHelpers {

  implicit val viewTest: ViewTest = new ViewTest {
    override val page: Html          = inject[IndexView].apply()
    override val pageTitle: String   = "cbam-registration-frontend"
    override val pageHeading: String = "cbam-registration-frontend"
  }

  "Index view" should {

    checkTitle()
    checkHeading()
    checkContentOnPage(
      "Welcome to your new frontend. Please see the README file for a guide to getting started.",
      ".govuk-body"
    )
  }
}
