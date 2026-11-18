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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.*
import itutil.ApplicationWithWiremock
import models.errors.ApiError.{InternalServerError, JsonValidationError, NotFoundError}
import org.scalactic.Prettifier.default
import play.api.test.Helpers.*
import play.api.libs.json.Json
import uk.gov.hmrc.http.UpstreamErrorResponse

class UserAnswersConnectorISpec extends ApplicationWithWiremock {

  val connector: UserAnswersConnector = app.injector.instanceOf[UserAnswersConnector]

  ".get" must {

    val baseUrlPattern = "/cbam-registration-backend/user-answers/.*"

    val jsonResponse = Json.toJson(emptyUserAnswers).toString

    "successfully retrieve user answers" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(jsonResponse)
          )
      )

      val result = connector.get(testCredId).value.futureValue
      result mustBe Right(emptyUserAnswers)
    }

    "return JsonValidationError when response JSON is invalid" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("""{"incorrect": "structure"}""")
          )
      )

      val result = connector.get(testCredId).value.futureValue
      result mustBe Left(JsonValidationError)
    }

    "return NotFoundError when the backend returns 404" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )

      val result = connector.get(testCredId).value.futureValue
      result mustBe Left(NotFoundError)
    }

    "return InternalServerError when the backend returns another error" in {
      stubFor(
        get(urlPathMatching(baseUrlPattern))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      val result = connector.get(testCredId).value.futureValue
      result mustBe Left(InternalServerError)
    }
  }

  ".set" must {

    val baseUrlPattern = "/cbam-registration-backend/user-answers"

    "return true when user answers are updated successfully" in {
      stubFor(
        put(urlMatching(baseUrlPattern))
          .withRequestBody(equalToJson(Json.stringify(Json.toJson(emptyUserAnswers))))
          .willReturn(aResponse().withStatus(OK))
      )

      val result = connector.set(emptyUserAnswers).futureValue
      result mustBe true
    }

    "fail the future when the backend returns an error" in {
      stubFor(
        put(urlMatching(baseUrlPattern))
          .withRequestBody(equalToJson(Json.stringify(Json.toJson(emptyUserAnswers))))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      val ex = intercept[UpstreamErrorResponse] {
        await(connector.set(emptyUserAnswers))
      }
      ex.statusCode mustBe NOT_FOUND
    }
  }

  ".create" must {

    val baseUrlPattern = "/cbam-registration-backend/user-answers/create/.*"

    "return CREATED when user answers are created successfully" in {
      stubFor(
        post(urlPathMatching(baseUrlPattern))
          .willReturn(aResponse().withStatus(CREATED))
      )

      val result = connector.create(testCredId).futureValue
      result mustBe true
    }

    "fail the future when the backend returns an error" in {
      stubFor(
        post(urlPathMatching(baseUrlPattern))
          .willReturn(aResponse().withStatus(SERVICE_UNAVAILABLE))
      )

      val ex = intercept[UpstreamErrorResponse] {
        await(connector.create(testCredId))
      }
      ex.statusCode mustBe SERVICE_UNAVAILABLE
    }
  }

  ".delete" must {

    val baseUrlPattern = "/cbam-registration-backend/user-answers/delete/.*"

    "return OK when user answers are deleted successfully" in {
      stubFor(
        delete(urlPathMatching(baseUrlPattern))
          .willReturn(aResponse().withStatus(OK))
      )

      val result = connector.delete(testCredId).futureValue
      result mustBe true
    }

    "fail the future when the backend returns an error" in {
      stubFor(
        delete(urlPathMatching(baseUrlPattern))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
      )

      val ex = intercept[UpstreamErrorResponse] {
        await(connector.delete(testCredId))
      }
      ex.statusCode mustBe INTERNAL_SERVER_ERROR
    }
  }

  ".keepAlive" must {

    val baseUrlPattern = "/cbam-registration-backend/user-answers/keep-alive/.*"

    "return OK when user answers are updated successfully" in {
      stubFor(
        put(urlPathMatching(baseUrlPattern))
          .willReturn(aResponse().withStatus(OK))
      )

      val result = connector.keepAlive(testCredId).futureValue
      result mustBe true
    }

    "fail the future when the backend returns an error" in {
      stubFor(
        put(urlPathMatching(baseUrlPattern))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
      )

      val ex = intercept[UpstreamErrorResponse] {
        await(connector.keepAlive(testCredId))
      }
      ex.statusCode mustBe INTERNAL_SERVER_ERROR
    }
  }
}
