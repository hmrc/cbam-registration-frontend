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
import models.UserAnswers
import models.requests.{IdentifierRequestWithCbamEori, OptionalDataRequestWithCbamEori}
import org.mockito.Mockito.*
import play.api.test.FakeRequest
import repositories.SessionRepository

import scala.concurrent.Future

class DataRetrievalWithCbamEoriActionSpec extends SpecBase {

  class Harness(sessionRepository: SessionRepository) extends DataRetrievalWithCbamEoriActionImpl(sessionRepository) {
    def callTransform[A](request: IdentifierRequestWithCbamEori[A]): Future[OptionalDataRequestWithCbamEori[A]] =
      transform(request)
  }

  "DataRetrievalWithCbamEoriAction" when {

    "there is no data in the cache" must {

      "set userAnswers to 'None' in the request" in {

        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.get(testCredId)) thenReturn Future.successful(None)
        val action            = new Harness(sessionRepository)

        val result = action
          .callTransform(IdentifierRequestWithCbamEori(FakeRequest(), testCredId, testEori))
          .futureValue

        result.userAnswers must not be defined
      }
    }

    "there is data in the cache" must {

      "add the userAnswers to the request" in {

        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.get(testCredId)) thenReturn Future.successful(Some(emptyUserAnswers))
        val action            = new Harness(sessionRepository)

        val result = action
          .callTransform(IdentifierRequestWithCbamEori(FakeRequest(), testCredId, testEori))
          .futureValue

        result.userAnswers mustBe Some(emptyUserAnswers)
      }
    }
  }
}
