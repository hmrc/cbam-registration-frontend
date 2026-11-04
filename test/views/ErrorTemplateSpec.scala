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
import views.html.ErrorTemplate

class ErrorTemplateSpec extends ViewTestHelpers {

  val exampleTitle   = "Sorry there was a problem"
  val exampleHeading = "Sorry again"
  val exampleMessage = "Sorry for the last time"

  implicit val viewTest: ViewTest = new ViewTest {
    override val page: Html          = inject[ErrorTemplate].apply(exampleTitle, exampleHeading, exampleMessage)
    override val pageTitle: String   = exampleTitle
    override val pageHeading: String = exampleHeading
  }

  "Error Template" should {

    checkTitle()
    checkHeading()
    checkContentOnPage(exampleMessage, ".govuk-body")
  }
}
