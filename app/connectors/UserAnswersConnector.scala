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

import config.FrontendAppConfig
import models.UserAnswers
import models.errors.ApiError.{InternalServerError, JsonValidationError, NotFoundError}
import play.api.Logging
import play.api.http.Status.{CREATED, NOT_FOUND, OK}
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import types.ResultT
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReadsInstances, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class UserAnswersConnector @Inject() (
    config: FrontendAppConfig,
    implicit val httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances
    with Logging {

  def get(credId: String)(implicit hc: HeaderCarrier): ResultT[UserAnswers] =
    ResultT.fromFuture {
      httpClient
        .get(url"${config.userAnswersGetUrl(credId)}")
        .execute[HttpResponse]
        .map { httpResponse =>
          httpResponse.status match {
            case OK          =>
              Try(httpResponse.json.as[UserAnswers]) match {
                case Success(userAnswers) => Right(userAnswers)
                case Failure(exception)   =>
                  logger.error(s"[UserAnswersConnector][get] Error parsing response as UserAnswers for credId $credId")
                  Left(JsonValidationError)
              }
            case NOT_FOUND   =>
              Left(NotFoundError)
            case otherStatus =>
              logger.error(s"[UserAnswersConnector][get] Unexpected response for $credId with status $otherStatus")
              Left(InternalServerError)
          }
        }
    }

  def set(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Boolean] =
    httpClient
      .put(url"${config.userAnswersSetUrl}")
      .withBody(Json.toJson(userAnswers))
      .execute[HttpResponse]
      .flatMap { httpResponse =>
        httpResponse.status match {
          case OK          => Future.successful(true)
          case otherStatus =>
            logger.error(
              s"[UserAnswersConnector][set] Unexpected response for ${userAnswers.credId} with status $otherStatus"
            )
            Future.failed(UpstreamErrorResponse("Unexpected response from UserAnswersConnector.set", otherStatus))
        }
      }

  def create(credId: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    httpClient
      .post(url"${config.userAnswersCreateUrl(credId)}")
      .execute[HttpResponse]
      .flatMap { httpResponse =>
        httpResponse.status match {
          case CREATED     => Future.successful(true)
          case otherStatus =>
            logger.error(s"[UserAnswersConnector][create] Unexpected response for $credId with status $otherStatus")
            Future.failed(UpstreamErrorResponse("Unexpected response from UserAnswersConnector.create", otherStatus))
        }
      }

  def delete(credId: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    httpClient
      .delete(url"${config.userAnswersDeleteUrl(credId)}")
      .execute[HttpResponse]
      .flatMap { httpResponse =>
        httpResponse.status match {
          case OK          => Future.successful(true)
          case otherStatus =>
            logger.error(s"[UserAnswersConnector][delete] Unexpected response for $credId with status $otherStatus")
            Future.failed(UpstreamErrorResponse("Unexpected response from UserAnswersConnector.delete", otherStatus))
        }
      }

  def keepAlive(credId: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    httpClient
      .put(url"${config.userAnswersKeepAliveUrl(credId)}")
      .execute[HttpResponse]
      .flatMap { httpResponse =>
        httpResponse.status match {
          case OK          => Future.successful(true)
          case otherStatus =>
            logger.error(s"[UserAnswersConnector][keepAlive] Unexpected response for $credId with status $otherStatus")
            Future.failed(UpstreamErrorResponse("Unexpected response from UserAnswersConnector.keepAlive", otherStatus))
        }
      }
}
