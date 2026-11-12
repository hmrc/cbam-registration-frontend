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
import models.requests.{IdentifierRequest, IdentifierRequestWithCbamEori}
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION

import scala.concurrent.Future

class CbamEoriRequiredActionSpec extends SpecBase {

  class Harness extends CbamEoriRequiredActionImpl {
    def actionRefine[A](request: IdentifierRequest[A]): Future[Either[Result, IdentifierRequestWithCbamEori[A]]] =
      refine(request)
  }

  "CbamEoriRequiredAction" must {

    "redirect to ServiceEntryController when cbamEori is None" in {

      val harness = new Harness

      val identifierRequest: IdentifierRequest[_] =
        IdentifierRequest(FakeRequest(), testCredId, hasEori = true, cdsEori = Some(testEori), cbamEori = None)

      val result = harness
        .actionRefine(identifierRequest)
        .futureValue
        .left
        .getOrElse(fail("Expected a Left to be returned"))
        .header

      result.status                mustBe SEE_OTHER
      result.headers.get(LOCATION) mustBe Some(routes.ServiceEntryController.onPageLoad().url)
    }

    "return an IdentifierRequestWithCbamEori with the correct values when cbamEori exists" in {

      val harness = new Harness

      val fakeRequest = FakeRequest()

      val identifierRequest: IdentifierRequest[_] =
        IdentifierRequest(
          fakeRequest,
          testCredId,
          hasEori = true,
          cdsEori = Some(testEori),
          cbamEori = Some(testEori)
        )

      val result = harness
        .actionRefine(identifierRequest)
        .futureValue

      result.isRight mustBe true
      result.map { request =>
        request.request  mustBe fakeRequest
        request.credId   mustBe testCredId
        request.cbamEori mustBe testEori
      }
    }
  }

}
