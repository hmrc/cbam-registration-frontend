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
import connectors.UserAnswersConnector
import models.UserAnswers
import models.errors.ApiError.{InternalServerError, NotFoundError}
import models.requests.{IdentifierRequest, OptionalDataRequest}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import types.ResultT

import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase {

  class Harness(userAnswersConnector: UserAnswersConnector) extends DataRetrievalActionImpl(userAnswersConnector) {
    def actionRefine[A](request: IdentifierRequest[A]): Future[Either[Result, OptionalDataRequest[A]]] = refine(request)
  }

  "Data Retrieval Action" when {

    "user answers do not exist" must {

      "set userAnswers to 'None' in the request" in {
        val userAnswersConnector = mock[UserAnswersConnector]
        when(userAnswersConnector.get(eqTo(testCredId))(any())) thenReturn ResultT.fromError(NotFoundError)
        val action               = new Harness(userAnswersConnector)

        val result = action.actionRefine(IdentifierRequest(FakeRequest(), testCredId, false, None, None)).futureValue

        result.isRight mustBe true
        result.map { optionalDataRequest =>
          optionalDataRequest.userAnswers must not be defined
        }
      }
    }

    "user answers exist" must {

      "build a userAnswers object and add it to the request" in {
        val userAnswersConnector = mock[UserAnswersConnector]
        when(userAnswersConnector.get(eqTo(testCredId))(any())) thenReturn ResultT.fromValue(emptyUserAnswers)
        val action               = new Harness(userAnswersConnector)

        val result = action
          .actionRefine(IdentifierRequest(FakeRequest(), testCredId, true, Some(testEori), Some(testEori)))
          .futureValue

        result.isRight mustBe true
        result.map { optionalDataRequest =>
          optionalDataRequest.userAnswers mustBe Some(emptyUserAnswers)
        }
      }
    }

    "UserAnswersConnector returns an error other than NOT_FOUND" must {

      "redirect to the Journey Recovery controller" in {
        val userAnswersConnector = mock[UserAnswersConnector]
        when(userAnswersConnector.get(eqTo(testCredId))(any())) thenReturn ResultT.fromError(InternalServerError)
        val action               = new Harness(userAnswersConnector)

        val result = action.actionRefine(IdentifierRequest(FakeRequest(), testCredId, false, None, None))

        val redirectResult = result.map {
          case Left(res) => res
          case Right(_)  => fail("Expected a Left to be returned")
        }
        status(redirectResult) mustBe SEE_OTHER
        redirectLocation(redirectResult).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
