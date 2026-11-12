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
import models.requests.{DataRequestWithCbamEori, IdentifierRequestWithCbamEori, OptionalDataRequestWithCbamEori}
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION

import scala.concurrent.Future

class DataRequiredWithCbamEoriActionSpec extends SpecBase {

  class Harness extends DataRequiredWithCbamEoriActionImpl {
    def actionRefine[A](
        request: OptionalDataRequestWithCbamEori[A]
    ): Future[Either[Result, DataRequestWithCbamEori[A]]] = refine(request)
  }

  val identifierRequest: IdentifierRequestWithCbamEori[_] =
    IdentifierRequestWithCbamEori(FakeRequest(), testCredId, testEori)

  "DataRequiredWithCbamEoriAction" must {

    "redirect to Journey Recovery when UserAnswers is None" in {

      val harness = new Harness

      val optionalDataRequest =
        OptionalDataRequestWithCbamEori(
          identifierRequest,
          identifierRequest.credId,
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

    "return a DataRequestWithCbamEori with the correct values when User Answers exists" in {

      val harness = new Harness

      val optionalDataRequest =
        OptionalDataRequestWithCbamEori(
          identifierRequest,
          identifierRequest.credId,
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
        dataRequest.cbamEori    mustBe testEori
      }
    }
  }

}
