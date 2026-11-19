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

package models

import base.SpecBase
import pages.*
import play.api.libs.json.Json

import java.time.LocalDate

class UserAnswersSpec extends SpecBase {

  "UserAnswers" must {
    val userAnswers: UserAnswers = emptyUserAnswers.copy(
      data = Json.obj("foo" -> "bar", "bool" -> true)
    )

    val json =
      s"""{"_id":"$testCredId","data":{"foo":"bar","bool":true},"lastUpdated":{"$$date":{"$$numberLong":"1718118467838"}}}"""

    "serialise to json" in {
      Json.toJson(userAnswers).toString mustBe json
    }

    "deserialise from json" in {
      Json.parse(json).as[UserAnswers] mustBe userAnswers
    }
  }

  ".liabilityDetails" when {
    "user answers are missing the required data" must {
      "return None" in {
        val userAnswers1 = emptyUserAnswers
          .withPage(ForwardLookingThresholdPage, true)

        val userAnswers2 = emptyUserAnswers
          .withPage(ForwardLookingThresholdPage, true)
          .withPage(ExpectToExceedThresholdPage, LocalDate.now(clock))

        val userAnswers3 = emptyUserAnswers
          .withPage(ForwardLookingThresholdPage, true)
          .withPage(ExpectToExceedThresholdPage, LocalDate.now(clock))
          .withPage(BackwardLookingThresholdPage, true)

        val userAnswers4 = emptyUserAnswers
          .withPage(ForwardLookingThresholdPage, false)
          .withPage(BackwardLookingThresholdPage, false)

        val userAnswers5 = emptyUserAnswers
          .withPage(ForwardLookingThresholdPage, false)
          .withPage(BackwardLookingThresholdPage, true)

        Seq(emptyUserAnswers, userAnswers1, userAnswers2, userAnswers3, userAnswers4, userAnswers5).foreach {
          _.liabilityDetails mustBe None
        }
      }
    }

    "only the forward looking threshold is met" must {
      "return the forward looking threshold date" in {
        val userAnswers = emptyUserAnswers
          .withPage(ForwardLookingThresholdPage, true)
          .withPage(ExpectToExceedThresholdPage, LocalDate.of(2027, 3, 2))
          .withPage(BackwardLookingThresholdPage, false)

        userAnswers.liabilityDetails mustBe Some(
          LiabilityDetails(LocalDate.of(2027, 3, 2), isForwardLookEarliest = true)
        )
      }
    }

    "only the backward looking threshold is met" must {
      "return the backward looking threshold date when it is the 1st of month" in {
        val userAnswers = emptyUserAnswers
          .withPage(ForwardLookingThresholdPage, false)
          .withPage(BackwardLookingThresholdPage, true)
          .withPage(BackwardLookingDatePage, LocalDate.of(2027, 3, 1))

        userAnswers.liabilityDetails mustBe Some(
          LiabilityDetails(LocalDate.of(2027, 3, 1), isForwardLookEarliest = false)
        )
      }

      "return the 1st of the next month when the backward looking threshold date is not the 1st of month" in {
        val userAnswers = emptyUserAnswers
          .withPage(ForwardLookingThresholdPage, false)
          .withPage(BackwardLookingThresholdPage, true)
          .withPage(BackwardLookingDatePage, LocalDate.of(2027, 3, 15))

        userAnswers.liabilityDetails mustBe Some(
          LiabilityDetails(LocalDate.of(2027, 4, 1), isForwardLookEarliest = false)
        )
      }
    }

    "the forward and backward looking thresholds are met" must {
      "compare the dates and return the earlier liability start date" when {
        "the forward looking date is earlier" in {
          val userAnswers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, true)
            .withPage(ExpectToExceedThresholdPage, LocalDate.of(2027, 3, 2))
            .withPage(BackwardLookingThresholdPage, true)
            .withPage(BackwardLookingDatePage, LocalDate.of(2027, 3, 15))

          userAnswers.liabilityDetails mustBe Some(
            LiabilityDetails(LocalDate.of(2027, 3, 2), isForwardLookEarliest = true)
          )
        }

        "the forward and backward looking dates are equal (1st of month)" in {
          val userAnswers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, true)
            .withPage(ExpectToExceedThresholdPage, LocalDate.of(2027, 3, 1))
            .withPage(BackwardLookingThresholdPage, true)
            .withPage(BackwardLookingDatePage, LocalDate.of(2027, 3, 1))

          userAnswers.liabilityDetails mustBe Some(
            LiabilityDetails(LocalDate.of(2027, 3, 1), isForwardLookEarliest = true)
          )
        }

        "the forward and backward looking dates are equal (not 1st of month)" in {
          val userAnswers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, true)
            .withPage(ExpectToExceedThresholdPage, LocalDate.of(2027, 3, 2))
            .withPage(BackwardLookingThresholdPage, true)
            .withPage(BackwardLookingDatePage, LocalDate.of(2027, 3, 2))

          userAnswers.liabilityDetails mustBe Some(
            LiabilityDetails(LocalDate.of(2027, 3, 2), isForwardLookEarliest = true)
          )
        }

        "the backward looking date is earlier even after adjustment to the next 1st of month" in {
          val userAnswers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, true)
            .withPage(ExpectToExceedThresholdPage, LocalDate.of(2027, 3, 2))
            .withPage(BackwardLookingThresholdPage, true)
            .withPage(BackwardLookingDatePage, LocalDate.of(2027, 2, 15))

          userAnswers.liabilityDetails mustBe Some(
            LiabilityDetails(LocalDate.of(2027, 3, 1), isForwardLookEarliest = false)
          )
        }

        "the backward looking date is earlier, but equals the forward looking date after adjustment to the next 1st of month" in {
          val userAnswers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, true)
            .withPage(ExpectToExceedThresholdPage, LocalDate.of(2027, 3, 1))
            .withPage(BackwardLookingThresholdPage, true)
            .withPage(BackwardLookingDatePage, LocalDate.of(2027, 2, 15))

          userAnswers.liabilityDetails mustBe Some(
            LiabilityDetails(LocalDate.of(2027, 3, 1), isForwardLookEarliest = true)
          )
        }

        "the backward looking date is earlier, but becomes later after adjustment to the next 1st of month" in {
          val userAnswers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, true)
            .withPage(ExpectToExceedThresholdPage, LocalDate.of(2027, 3, 20))
            .withPage(BackwardLookingThresholdPage, true)
            .withPage(BackwardLookingDatePage, LocalDate.of(2027, 3, 15))

          userAnswers.liabilityDetails mustBe Some(
            LiabilityDetails(LocalDate.of(2027, 3, 20), isForwardLookEarliest = true)
          )
        }
      }
    }
  }
}
