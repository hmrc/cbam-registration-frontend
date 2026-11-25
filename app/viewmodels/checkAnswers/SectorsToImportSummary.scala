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

package viewmodels.checkAnswers

import controllers.importDetails.routes
import models.{CheckMode, UserAnswers}
import pages.SectorsToImportPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object SectorsToImportSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(SectorsToImportPage).map { answers =>

      val value = ValueViewModel(
        HtmlContent(
          answers
            .map { answer =>
              HtmlFormat.escape(messages(s"sectorsToImport.$answer")).toString
            }
            .mkString(",<br>")
        )
      )

      SummaryListRowViewModel(
        key = "sectorsToImport.checkYourAnswersLabel",
        value = value,
        actions = Seq(
          ActionItemViewModel("site.change", routes.SectorsToImportController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("sectorsToImport.change.hidden"))
        )
      )
    }
}
