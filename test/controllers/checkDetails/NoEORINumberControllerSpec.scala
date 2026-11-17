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

package controllers.checkDetails

import base.SpecBase
import config.FrontendAppConfig
import controllers.{checkDetails, routes}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.checkDetails.NoEORINumberView

class NoEORINumberControllerSpec extends SpecBase {

  "NoEORINumber Controller" must {

    "return OK and the correct view when the user has no EORI" in {

      val application = applicationBuilder(
        userAnswers = Some(emptyUserAnswers),
        hasEori = false
      ).build()

      val dateString = "11 July 2024"

      running(application) {
        val request = FakeRequest(GET, checkDetails.routes.NoEORINumberController.onPageLoad().url)

        val result = route(application, request).value

        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val view      = application.injector.instanceOf[NoEORINumberView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(dateString)(request, messages(application), appConfig).toString
      }
    }

    "return 303 and redirect to journey recovery when the user has a EORI" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, checkDetails.routes.NoEORINumberController.onPageLoad().url)

        val result = route(application, request).value

        status(result)        mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.JourneyRecoveryController.onPageLoad().url)
      }
    }

    "redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, checkDetails.routes.NoEORINumberController.onPageLoad().url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
