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

package utils.liability

import models.UserAnswers
import pages.{BackwardLookingThresholdPage, ForwardLookingThresholdPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.checkAnswers.*
import viewmodels.govuk.summarylist.*

class LiabilityCheckYourAnswersHelper {

  def getSummaryList(
      userAnswers: UserAnswers
  )(implicit messages: Messages): Option[SummaryList] =
    for {
      forwardLookMet           <- userAnswers.get(ForwardLookingThresholdPage)
      backwardLookMet          <- userAnswers.get(BackwardLookingThresholdPage)
      if forwardLookMet || backwardLookMet
      forwardLookThresholdRow  <- ForwardLookingThresholdSummary.row(userAnswers)
      backwardLookThresholdRow <- BackwardLookingThresholdSummary.row(userAnswers)
      maybeForwardLookDateRow  <-
        if (forwardLookMet) ExpectToExceedThresholdSummary.row(userAnswers).map(Some(_)) else Some(None)
      maybeBackwardLookDateRow <-
        if (backwardLookMet) BackwardLookingDateSummary.row(userAnswers).map(Some(_)) else Some(None)
    } yield {
      val rows: Seq[SummaryListRow] = Seq(
        Some(forwardLookThresholdRow),
        maybeForwardLookDateRow,
        Some(backwardLookThresholdRow),
        maybeBackwardLookDateRow
      ).flatten
      SummaryListViewModel(rows = rows)
    }

}
