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
import forms.liability.BackwardLookingThresholdFormProvider
import models.NormalMode
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import pages.{BackwardLookingDatePage, BackwardLookingThresholdPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.liability.BackwardLookingThresholdView

import java.time.LocalDate
import scala.concurrent.Future

class BackwardLookingThresholdControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider        = new BackwardLookingThresholdFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val backwardLookingThresholdRoute: String = routes.BackwardLookingThresholdController.onPageLoad(NormalMode).url

  "BackwardLookingThreshold Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, backwardLookingThresholdRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BackwardLookingThresholdView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.withPage(BackwardLookingThresholdPage, true)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, backwardLookingThresholdRoute)

        val view = application.injector.instanceOf[BackwardLookingThresholdView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, backwardLookingThresholdRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, backwardLookingThresholdRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[BackwardLookingThresholdView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "redirect to the next page with hasAnswerChanged = false when the answer has not changed" in {
      val userAnswers = emptyUserAnswers.withPage(BackwardLookingThresholdPage, false)

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute, expectedHasAnswerChanged = false))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, backwardLookingThresholdRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(eqTo(userAnswers))(any())
      }
    }

    "redirect to the next page with hasAnswerChanged = true and clear the backward looking date when the answer changes from Yes to No" in {
      val userAnswers = emptyUserAnswers
        .withPage(BackwardLookingThresholdPage, true)
        .withPage(BackwardLookingDatePage, LocalDate.now(clock))

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, backwardLookingThresholdRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(
          eqTo(emptyUserAnswers.withPage(BackwardLookingThresholdPage, false))
        )(any())
      }
    }

    "redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, backwardLookingThresholdRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, backwardLookingThresholdRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
