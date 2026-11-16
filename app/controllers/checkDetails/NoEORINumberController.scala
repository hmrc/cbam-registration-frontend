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

package controllers.checkDetails

import controllers.actions.*
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats
import views.html.checkDetails.NoEORINumberView

import java.time.temporal.ChronoUnit.DAYS
import javax.inject.Inject

class NoEORINumberController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    view: NoEORINumberView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    if (!request.hasEori) {
      implicit val lang: Lang = messagesApi.preferred(request).lang
      val dateFormatter       = DateTimeFormats.dateTimeFormat()
      val answersExpiryDate   = dateFormatter.format(request.userAnswers.lastUpdated.plus(30, DAYS))
      Ok(view(answersExpiryDate))
    } else {
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
