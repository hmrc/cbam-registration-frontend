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
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import pages.{BackwardLookingDatePage, BackwardLookingThresholdPage, ExpectToExceedThresholdPage, ForwardLookingThresholdPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.liability.LiabilityCheckYourAnswersHelper
import views.html.liability.LiabilityCheckYourAnswersView

import java.time.LocalDate

class LiabilityCheckYourAnswersControllerSpec extends SpecBase {

  val mockHelper: LiabilityCheckYourAnswersHelper = mock[LiabilityCheckYourAnswersHelper]

  lazy val liabilityCheckYourAnswersRoute: String = routes.LiabilityCheckYourAnswersController.onPageLoad().url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHelper)
  }

  "LiabilityCheckYourAnswers Controller" must {

    "return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers
        .withPage(ForwardLookingThresholdPage, true)
        .withPage(ExpectToExceedThresholdPage, LocalDate.now(clock))
        .withPage(BackwardLookingThresholdPage, true)
        .withPage(BackwardLookingDatePage, LocalDate.now(clock))

      when(mockHelper.getSummaryList(any())(any())).thenReturn(Some(testSummaryList))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[LiabilityCheckYourAnswersHelper].toInstance(mockHelper)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, liabilityCheckYourAnswersRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[LiabilityCheckYourAnswersView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(testSummaryList)(request, messages(application)).toString

        verify(mockHelper, times(1)).getSummaryList(eqTo(userAnswers))(any())
      }
    }

    "redirect to Journey Recovery for a GET if the summary list cannot be created due to missing answers" in {
      when(mockHelper.getSummaryList(any())(any())).thenReturn(None)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[LiabilityCheckYourAnswersHelper].toInstance(mockHelper)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, liabilityCheckYourAnswersRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockHelper, times(1)).getSummaryList(eqTo(emptyUserAnswers))(any())
      }
    }

    "redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, liabilityCheckYourAnswersRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
