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

package common

import models.UserAnswers
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

import java.time.{Clock, Instant, ZoneId}

trait TestData {

  val clock: Clock = Clock.fixed(Instant.ofEpochMilli(1718118467838L), ZoneId.of("UTC")) // 11 June 2024

  val testCredId: String = "cred0"
  val testEori: String   = "GB12345612345678"

  def emptyUserAnswers: UserAnswers = UserAnswers(testCredId, lastUpdated = Instant.now(clock))

  val testSummaryList: SummaryList = SummaryList(
    Seq(
      SummaryListRow(
        Key(Text("Question")),
        Value(Text("Answer")),
        actions = Some(Actions(items = Seq(ActionItem(content = Text("Change")))))
      )
    )
  )

}
