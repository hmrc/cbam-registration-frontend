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

package controllers.actions

import base.SpecBase
import controllers.routes
import models.requests.{DataRequest, IdentifierRequest, OptionalDataRequest}
import play.api.http.Status.SEE_OTHER
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION

import scala.concurrent.Future

class DataRequiredActionSpec extends SpecBase {

  class Harness extends DataRequiredActionImpl {
    def actionRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  val identifierRequest: IdentifierRequest[AnyContentAsEmpty.type] =
    IdentifierRequest(FakeRequest(), testCredId, hasEori = true, cdsEori = Some(testEori), cbamEori = Some(testEori))

  "Data Required Action" must {

    "redirect to Journey Recovery when UserAnswers is None" in {

      val harness = new Harness

      val optionalDataRequest =
        OptionalDataRequest(
          identifierRequest,
          testCredId,
          identifierRequest.hasEori,
          identifierRequest.cdsEori,
          identifierRequest.cbamEori,
          userAnswers = None
        )

      val result = harness
        .actionRefine(optionalDataRequest)
        .futureValue
        .left
        .getOrElse(fail("Expected a Left to be returned"))
        .header

      result.status                mustBe SEE_OTHER
      result.headers.get(LOCATION) mustBe Some(routes.JourneyRecoveryController.onPageLoad().url)
    }

    "return a DataRequest with the correct values when User Answers exists" in {

      val harness = new Harness

      val optionalDataRequest =
        OptionalDataRequest(
          identifierRequest,
          testCredId,
          identifierRequest.hasEori,
          identifierRequest.cdsEori,
          identifierRequest.cbamEori,
          userAnswers = Some(emptyUserAnswers)
        )

      val result = harness
        .actionRefine(optionalDataRequest)
        .futureValue

      result.isRight mustBe true
      result.map { dataRequest =>
        dataRequest.request     mustBe identifierRequest
        dataRequest.userAnswers mustBe emptyUserAnswers
        dataRequest.credId      mustBe testCredId
        dataRequest.hasEori     mustBe true
        dataRequest.cdsEori     mustBe Some(testEori)
        dataRequest.cbamEori    mustBe Some(testEori)
      }
    }
  }

}
