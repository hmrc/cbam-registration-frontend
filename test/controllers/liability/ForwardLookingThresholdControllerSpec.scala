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
import forms.liability.ForwardLookingThresholdFormProvider
import models.{CheckMode, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import pages.{ExpectToExceedThresholdPage, ForwardLookingThresholdPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.liability.ForwardLookingThresholdView

import java.time.LocalDate
import scala.concurrent.Future

class ForwardLookingThresholdControllerSpec extends SpecBase {

  val form = new ForwardLookingThresholdFormProvider()()

  lazy val forwardLookingThresholdRoute: String      = routes.ForwardLookingThresholdController.onPageLoad(NormalMode).url
  lazy val forwardLookingThresholdCheckRoute: String =
    routes.ForwardLookingThresholdController.onPageLoad(CheckMode).url

  "ForwardLookingThreshold Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, forwardLookingThresholdRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForwardLookingThresholdView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.withPage(ForwardLookingThresholdPage, true)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, forwardLookingThresholdRoute)

        val view = application.injector.instanceOf[ForwardLookingThresholdView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, forwardLookingThresholdRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ExpectToExceedThresholdController.onPageLoad(NormalMode).url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, forwardLookingThresholdRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ForwardLookingThresholdView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "redirect to check your answers in CheckMode when the answer has not changed" in {
      val userAnswers = emptyUserAnswers.withPage(ForwardLookingThresholdPage, false)

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, forwardLookingThresholdCheckRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.LiabilityCheckYourAnswersController.onPageLoad().url

        verify(mockUserAnswersConnector, times(1)).set(eqTo(userAnswers))(any())
      }
    }

    "redirect to the next page and clear the forward looking date when the answer changes from Yes to No" in {
      val userAnswers = emptyUserAnswers
        .withPage(ForwardLookingThresholdPage, true)
        .withPage(ExpectToExceedThresholdPage, LocalDate.now(clock))

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, forwardLookingThresholdRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.BackwardLookingThresholdController.onPageLoad(NormalMode).url

        verify(mockUserAnswersConnector, times(1)).set(
          eqTo(emptyUserAnswers.withPage(ForwardLookingThresholdPage, false))
        )(any())
      }
    }

    "redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, forwardLookingThresholdRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, forwardLookingThresholdRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
