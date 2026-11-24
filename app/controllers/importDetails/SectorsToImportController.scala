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

package controllers.importDetails

import connectors.UserAnswersConnector
import controllers.actions.*
import forms.SectorsToImportFormProvider
import models.{Mode, SectorsToImport}
import navigation.Navigator
import pages.SectorsToImportPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.importDetails.SectorsToImportView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SectorsToImportController @Inject() (
    override val messagesApi: MessagesApi,
    userAnswersConnector: UserAnswersConnector,
    navigator: Navigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: SectorsToImportFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: SectorsToImportView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Set[String]] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(SectorsToImportPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    // TODO: call external API to retrieve list of sectors
    val sectorList    = Seq("Aluminium", "Cement", "Fertiliser", "Hydrogen", "Iron and steel")
    val checkboxItems = SectorsToImport.checkboxItems(sectorList)

    Ok(view(preparedForm, checkboxItems, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            // TODO: call external API to retrieve list of sectors
            val sectorList    = Seq("Aluminium", "Cement", "Fertiliser", "Hydrogen", "Iron and steel")
            val checkboxItems = SectorsToImport.checkboxItems(sectorList)

            Future.successful(BadRequest(view(formWithErrors, checkboxItems, mode)))
          ,
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(SectorsToImportPage, value))
              _              <- userAnswersConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(SectorsToImportPage, mode, updatedAnswers))
        )
  }
}
