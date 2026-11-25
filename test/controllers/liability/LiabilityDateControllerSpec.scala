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

package controllers.liability

import base.SpecBase
import models.UserAnswers
import pages.{BackwardLookingDatePage, BackwardLookingThresholdPage, ExpectToExceedThresholdPage, ForwardLookingThresholdPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.liability.LiabilityDateView

import java.time.{LocalDate, ZoneId}

class LiabilityDateControllerSpec extends SpecBase {

  lazy val liabilityDateRoute: String = routes.LiabilityDateController.onPageLoad().url

  private val today: LocalDate            = LocalDate.now(ZoneId.of("Europe/London"))
  private val firstOfThisMonth: LocalDate = today.withDayOfMonth(1)

  private def forwardLookOnly(forwardLookDate: LocalDate): UserAnswers =
    emptyUserAnswers
      .withPage(ForwardLookingThresholdPage, true)
      .withPage(ExpectToExceedThresholdPage, forwardLookDate)
      .withPage(BackwardLookingThresholdPage, false)

  private def backwardLookOnly(backwardLookDate: LocalDate): UserAnswers =
    emptyUserAnswers
      .withPage(ForwardLookingThresholdPage, false)
      .withPage(BackwardLookingThresholdPage, true)
      .withPage(BackwardLookingDatePage, backwardLookDate)

  private def bothLooks(forwardLookDate: LocalDate, backwardLookDate: LocalDate): UserAnswers =
    emptyUserAnswers
      .withPage(ForwardLookingThresholdPage, true)
      .withPage(ExpectToExceedThresholdPage, forwardLookDate)
      .withPage(BackwardLookingThresholdPage, true)
      .withPage(BackwardLookingDatePage, backwardLookDate)

  private def checkPage(
      userAnswers: UserAnswers,
      expectedIsForwardLook: Boolean,
      expectedLiabilityStartDate: LocalDate,
      expectedBackwardLookDate: Option[LocalDate],
      expectedDeadline: LocalDate,
      expectedIsLate: Boolean
  ): Unit = {
    val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

    running(application) {
      val request = FakeRequest(GET, liabilityDateRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[LiabilityDateView]

      status(result)          mustEqual OK
      contentAsString(result) mustEqual view(
        expectedIsForwardLook,
        expectedLiabilityStartDate,
        expectedBackwardLookDate,
        expectedDeadline,
        expectedIsLate
      )(request, messages(application)).toString
    }
  }

  private def checkRedirectsToJourneyRecovery(userAnswers: Option[UserAnswers]): Unit = {
    val application = applicationBuilder(userAnswers = userAnswers).build()

    running(application) {
      val request = FakeRequest(GET, liabilityDateRoute)

      val result = route(application, request).value

      status(result)                 mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }
  }

  "LiabilityDate Controller" when {

    "onPageLoad" must {

      "return OK and the forward looking content" when {

        "only the ExpectToExceedThreshold date is present" in {
          val forwardLookDate = today.minusDays(5)

          checkPage(
            forwardLookOnly(forwardLookDate),
            expectedIsForwardLook = true,
            expectedLiabilityStartDate = forwardLookDate,
            expectedBackwardLookDate = None,
            expectedDeadline = forwardLookDate.plusMonths(1),
            expectedIsLate = false
          )
        }

        "both dates are present and the ExpectToExceedThreshold date is earlier than the BackwardLookingDate liability start date" in {
          val forwardLookDate  = firstOfThisMonth.minusMonths(3)
          val backwardLookDate = firstOfThisMonth.minusMonths(3).plusDays(10)

          checkPage(
            bothLooks(forwardLookDate, backwardLookDate),
            expectedIsForwardLook = true,
            expectedLiabilityStartDate = forwardLookDate,
            expectedBackwardLookDate = Some(backwardLookDate),
            expectedDeadline = forwardLookDate.plusMonths(1),
            expectedIsLate = true
          )
        }

        "the raw BackwardLookingDate date is earlier, but the ExpectToExceedThreshold date is earlier than the BackwardLookingDate liability start date" in {
          val forwardLookDate  = firstOfThisMonth.minusMonths(2).plusDays(19)
          val backwardLookDate = firstOfThisMonth.minusMonths(2).plusDays(14)

          checkPage(
            bothLooks(forwardLookDate, backwardLookDate),
            expectedIsForwardLook = true,
            expectedLiabilityStartDate = forwardLookDate,
            expectedBackwardLookDate = Some(backwardLookDate),
            expectedDeadline = forwardLookDate.plusMonths(1),
            expectedIsLate = true
          )
        }

        "both dates are present and the liability start dates are the same" in {
          val forwardLookDate  = firstOfThisMonth
          val backwardLookDate = firstOfThisMonth.minusMonths(1).plusDays(14)

          checkPage(
            bothLooks(forwardLookDate, backwardLookDate),
            expectedIsForwardLook = true,
            expectedLiabilityStartDate = firstOfThisMonth,
            expectedBackwardLookDate = Some(backwardLookDate),
            expectedDeadline = firstOfThisMonth.plusMonths(1),
            expectedIsLate = false
          )
        }
      }

      "return OK and the backward looking content" when {

        "only the BackwardLookingDate date is present and it is not the 1st of the month" in {
          val backwardLookDate = firstOfThisMonth.minusMonths(1).plusDays(14)

          checkPage(
            backwardLookOnly(backwardLookDate),
            expectedIsForwardLook = false,
            expectedLiabilityStartDate = firstOfThisMonth,
            expectedBackwardLookDate = Some(backwardLookDate),
            expectedDeadline = firstOfThisMonth.plusMonths(1),
            expectedIsLate = false
          )
        }

        "only the BackwardLookingDate date is present and it is the 1st of the month" in {
          checkPage(
            backwardLookOnly(firstOfThisMonth),
            expectedIsForwardLook = false,
            expectedLiabilityStartDate = firstOfThisMonth,
            expectedBackwardLookDate = Some(firstOfThisMonth),
            expectedDeadline = firstOfThisMonth.plusMonths(1),
            expectedIsLate = false
          )
        }

        "both dates are present and the BackwardLookingDate liability start date is earlier than the ExpectToExceedThreshold date" in {
          val forwardLookDate  = today.minusDays(5)
          val backwardLookDate = firstOfThisMonth.minusMonths(3).plusDays(2)

          checkPage(
            bothLooks(forwardLookDate, backwardLookDate),
            expectedIsForwardLook = false,
            expectedLiabilityStartDate = firstOfThisMonth.minusMonths(2),
            expectedBackwardLookDate = Some(backwardLookDate),
            expectedDeadline = firstOfThisMonth.minusMonths(1),
            expectedIsLate = true
          )
        }
      }

      "show the on time content when today is the registration deadline" in {
        val forwardLookDate = today.minusMonths(1)
        assume(forwardLookDate.plusMonths(1) == today, "today has no matching day in the previous month")

        checkPage(
          forwardLookOnly(forwardLookDate),
          expectedIsForwardLook = true,
          expectedLiabilityStartDate = forwardLookDate,
          expectedBackwardLookDate = None,
          expectedDeadline = today,
          expectedIsLate = false
        )
      }

      "show the late content when today is after the registration deadline" in {
        val forwardLookDate = today.minusMonths(1).minusDays(1)

        checkPage(
          forwardLookOnly(forwardLookDate),
          expectedIsForwardLook = true,
          expectedLiabilityStartDate = forwardLookDate,
          expectedBackwardLookDate = None,
          expectedDeadline = forwardLookDate.plusMonths(1),
          expectedIsLate = true
        )
      }

      "redirect to Journey Recovery" when {

        "neither the ExpectToExceedThreshold nor BackwardLookingDate date is present" in {
          checkRedirectsToJourneyRecovery(
            Some(
              emptyUserAnswers
                .withPage(ForwardLookingThresholdPage, true)
                .withPage(BackwardLookingThresholdPage, true)
            )
          )
        }

        "both the ForwardLookingThreshold and BackwardLookingThreshold answers are No" in {
          checkRedirectsToJourneyRecovery(
            Some(
              emptyUserAnswers
                .withPage(ForwardLookingThresholdPage, false)
                .withPage(BackwardLookingThresholdPage, false)
            )
          )
        }

        "the ForwardLookingThreshold and BackwardLookingThreshold answers are missing" in {
          checkRedirectsToJourneyRecovery(
            Some(
              emptyUserAnswers
                .withPage(ExpectToExceedThresholdPage, today)
                .withPage(BackwardLookingDatePage, today)
            )
          )
        }

        "no existing data is found" in {
          checkRedirectsToJourneyRecovery(None)
        }
      }
    }
  }
}
