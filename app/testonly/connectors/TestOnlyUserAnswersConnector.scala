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

package testonly.connectors

import config.FrontendAppConfig
import play.api.Logging
import play.api.http.Status.OK
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestOnlyUserAnswersConnector @Inject() (
    appConfig: FrontendAppConfig,
    httpClient: HttpClientV2
)(implicit val ec: ExecutionContext)
    extends Logging {

  def clearAll()(implicit hc: HeaderCarrier): Future[Boolean] =
    httpClient
      .delete(url"${appConfig.userAnswersClearAllUrl}")
      .execute[HttpResponse]
      .flatMap { httpResponse =>
        httpResponse.status match {
          case OK          => Future.successful(true)
          case otherStatus =>
            logger.error(s"[TestOnlyUserAnswersConnector][clearAll] Unexpected response with status $otherStatus")
            Future.failed(
              UpstreamErrorResponse("Unexpected response from TestOnlyUserAnswersConnector.clearAll", otherStatus)
            )
        }
      }

}
