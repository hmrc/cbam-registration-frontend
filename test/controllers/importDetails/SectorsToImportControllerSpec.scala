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

package controllers.importDetails

import base.SpecBase
import forms.SectorsToImportFormProvider
import models.{NormalMode, SectorsToImport}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.SectorsToImportPage
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import views.html.importDetails.SectorsToImportView

import scala.concurrent.Future

class SectorsToImportControllerSpec extends SpecBase with MockitoSugar {

  lazy val sectorsToImportRoute: String = routes.SectorsToImportController.onPageLoad(NormalMode).url

  val formProvider                     = new SectorsToImportFormProvider()
  val form: Form[Set[String]]          = formProvider()
  val sectorList: Seq[String]          = Seq("Aluminium", "Cement", "Fertiliser", "Hydrogen", "Iron and steel")
  val checkboxItems: Seq[CheckboxItem] = SectorsToImport.checkboxItems(sectorList)(messages(app))

  "SectorsToImport Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, sectorsToImportRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SectorsToImportView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, checkboxItems, NormalMode)(request, messages(application)).toString
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.withPage(SectorsToImportPage, Set("Item1", "Item2"))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, sectorsToImportRoute)

        val view = application.injector.instanceOf[SectorsToImportView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form.fill(Set("Item1", "Item2")), checkboxItems, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, sectorsToImportRoute)
            .withFormUrlEncodedBody(("value[0]", "Item1"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual Call("GET", "/todo-25").url
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, sectorsToImportRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[SectorsToImportView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, checkboxItems, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, sectorsToImportRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, sectorsToImportRoute)
            .withFormUrlEncodedBody(("value[0]", "Item1"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
