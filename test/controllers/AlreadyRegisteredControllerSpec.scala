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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.AlreadyRegisteredView

class AlreadyRegisteredControllerSpec extends SpecBase {

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  "AlreadyRegistered Controller" - {

    "must return OK and the correct view for a GET" - {
      "when returns-available is true in config" in {
        when(mockAppConfig.returnsServiceAvailable).thenReturn(true)
        when(mockAppConfig.feedbackUrl(any())).thenReturn("feedbackUrl")

        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.AlreadyRegisteredController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AlreadyRegisteredView]

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(
            returnsServiceAvailable = true
          )(request, messages(application)).toString
        }
      }

      "when returns-available is false in config" in {
        when(mockAppConfig.returnsServiceAvailable).thenReturn(false)
        when(mockAppConfig.feedbackUrl(any())).thenReturn("feedbackUrl")

        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.AlreadyRegisteredController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AlreadyRegisteredView]

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(
            returnsServiceAvailable = false
          )(request, messages(application)).toString
        }
      }
    }
  }
}
