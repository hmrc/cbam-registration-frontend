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

  "LiabilityDate Controller" when {

    "onPageLoad" must {

      "return OK and the forward looking content" when {

        "only the ExpectToExceedThreshold date is present" in {
          val forwardLookDate = today.minusDays(5)

          val userAnswers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, true)
            .withPage(ExpectToExceedThresholdPage, forwardLookDate)
            .withPage(BackwardLookingThresholdPage, false)

          checkPage(
            userAnswers,
            expectedIsForwardLook = true,
            expectedLiabilityStartDate = forwardLookDate,
            expectedBackwardLookDate = None,
            expectedDeadline = forwardLookDate.plusMonths(1),
            expectedIsLate = false
          )
        }

        "both dates are present and the ExpectToExceedThreshold date is earlier than the BackwardLookingDate liability start date" in {
          val forwardLookDate  = firstOfThisMonth.minusMonths(3)
          val backwardLookDate = firstOfThisMonth.minusMonths(2).plusDays(10)

          val userAnswers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, true)
            .withPage(ExpectToExceedThresholdPage, forwardLookDate)
            .withPage(BackwardLookingThresholdPage, true)
            .withPage(BackwardLookingDatePage, backwardLookDate)

          checkPage(
            userAnswers,
            expectedIsForwardLook = true,
            expectedLiabilityStartDate = forwardLookDate,
            expectedBackwardLookDate = Some(backwardLookDate),
            expectedDeadline = forwardLookDate.plusMonths(1),
            expectedIsLate = true
          )
        }
      }

      "return OK and the backward looking content" when {

        "only the BackwardLookingDate date is present" in {
          val userAnswers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, false)
            .withPage(BackwardLookingThresholdPage, true)
            .withPage(BackwardLookingDatePage, today)

          checkPage(
            userAnswers,
            expectedIsForwardLook = false,
            expectedLiabilityStartDate = firstOfThisMonth,
            expectedBackwardLookDate = Some(today),
            expectedDeadline = firstOfThisMonth.plusMonths(1),
            expectedIsLate = false
          )
        }

        "both dates are present and the BackwardLookingDate liability start date is earlier than the ExpectToExceedThreshold date" in {
          val forwardLookDate  = today.minusDays(5)
          val backwardLookDate = firstOfThisMonth.minusMonths(2).plusDays(2)

          val userAnswers = emptyUserAnswers
            .withPage(ForwardLookingThresholdPage, true)
            .withPage(ExpectToExceedThresholdPage, forwardLookDate)
            .withPage(BackwardLookingThresholdPage, true)
            .withPage(BackwardLookingDatePage, backwardLookDate)

          checkPage(
            userAnswers,
            expectedIsForwardLook = false,
            expectedLiabilityStartDate = firstOfThisMonth.minusMonths(2),
            expectedBackwardLookDate = Some(backwardLookDate),
            expectedDeadline = firstOfThisMonth.minusMonths(1),
            expectedIsLate = true
          )
        }

        "the raw ExpectToExceedThreshold date is earlier, but the BackwardLookingDate liability start date is earlier" in {
          assume(today.getDayOfMonth > 2, "needs an ExpectToExceedThreshold date in this month after the 1st")

          val forwardLookDate  = firstOfThisMonth.plusDays(1)
          val backwardLookDate = today

          val userAnswers = emptyUserAnswers
            .withPage(ExpectToExceedThresholdPage, forwardLookDate)
            .withPage(BackwardLookingDatePage, backwardLookDate)

          checkPage(
            userAnswers,
            expectedIsForwardLook = false,
            expectedLiabilityStartDate = firstOfThisMonth,
            expectedBackwardLookDate = Some(backwardLookDate),
            expectedDeadline = firstOfThisMonth.plusMonths(1),
            expectedIsLate = false
          )
        }

        "both dates are present and the liability start dates are the same" in {
          val userAnswers = emptyUserAnswers
            .withPage(ExpectToExceedThresholdPage, firstOfThisMonth)
            .withPage(BackwardLookingDatePage, today)

          checkPage(
            userAnswers,
            expectedIsForwardLook = false,
            expectedLiabilityStartDate = firstOfThisMonth,
            expectedBackwardLookDate = Some(today),
            expectedDeadline = firstOfThisMonth.plusMonths(1),
            expectedIsLate = false
          )
        }
      }

      "only check the dates are present, ignoring the Yes/No answers" in {
        val userAnswers = emptyUserAnswers.withPage(BackwardLookingDatePage, today)

        checkPage(
          userAnswers,
          expectedIsForwardLook = false,
          expectedLiabilityStartDate = firstOfThisMonth,
          expectedBackwardLookDate = Some(today),
          expectedDeadline = firstOfThisMonth.plusMonths(1),
          expectedIsLate = false
        )
      }

      "show the on time content when today is the registration deadline" in {
        val forwardLookDate = today.minusMonths(1)
        assume(forwardLookDate.plusMonths(1) == today, "today has no matching day in the previous month")

        val userAnswers = emptyUserAnswers.withPage(ExpectToExceedThresholdPage, forwardLookDate)

        checkPage(
          userAnswers,
          expectedIsForwardLook = true,
          expectedLiabilityStartDate = forwardLookDate,
          expectedBackwardLookDate = None,
          expectedDeadline = today,
          expectedIsLate = false
        )
      }

      "show the late content when today is after the registration deadline" in {
        val forwardLookDate = today.minusMonths(1).minusDays(1)

        val userAnswers = emptyUserAnswers.withPage(ExpectToExceedThresholdPage, forwardLookDate)

        checkPage(
          userAnswers,
          expectedIsForwardLook = true,
          expectedLiabilityStartDate = forwardLookDate,
          expectedBackwardLookDate = None,
          expectedDeadline = forwardLookDate.plusMonths(1),
          expectedIsLate = true
        )
      }

      "redirect to Journey Recovery when neither the ExpectToExceedThreshold nor BackwardLookingDate date is present" in {
        val userAnswers = emptyUserAnswers
          .withPage(ForwardLookingThresholdPage, true)
          .withPage(BackwardLookingThresholdPage, true)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, liabilityDateRoute)

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "redirect to Journey Recovery when no existing data is found" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, liabilityDateRoute)

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
