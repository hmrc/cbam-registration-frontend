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

package controllers

import controllers.actions.*
import models.UserAnswers
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ServiceEntryController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    sessionRepository: SessionRepository,
    val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  // TODO: Shift user answers to backend
  // TODO: Check if user is already registered via call to ETMP before creating user answers and redirect to /already-registered if so

  def onPageLoad(): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    request.userAnswers match {
      case None    =>
        for {
          _ <- sessionRepository.set(UserAnswers(credId = request.credId))
        } yield Redirect(controllers.liability.routes.LiabilityIntroductionController.onPageLoad().url)
      case Some(_) =>
        // TODO: Handle routing when user answers exist (e.g. if user is returning to service)
        Future.successful(Redirect(controllers.liability.routes.LiabilityIntroductionController.onPageLoad().url))
    }
  }

}
