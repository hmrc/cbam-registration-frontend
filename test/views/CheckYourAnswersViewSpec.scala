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
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import utils.ViewTestHelpers
import views.html.CheckYourAnswersView

class CheckYourAnswersViewSpec extends ViewTestHelpers {

  val answers = SummaryList(Seq(
    SummaryListRow(
      Key(Text("Question?")),
      Value(Text("Answer")),
      actions = Some(Actions(items = Seq(ActionItem(content = Text("Change"))))))
  ))

  implicit val viewTest: ViewTest = new ViewTest {
    override val page: Html          = inject[CheckYourAnswersView].apply(answers)
    override val pageTitle: String   = "Check your answers"
    override val pageHeading: String = "Check your answers"
  }

  "CheckYourAnswers view" should {

    checkTitle()
    checkHeading()
    checkContentOnPage("Question?", ".govuk-summary-list__key")
    checkContentOnPage("Answer", ".govuk-summary-list__value")
    checkContentOnPage("Change", ".govuk-summary-list__actions")
  }
}
