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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.*
import play.api.mvc.Results.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.CredentialStrength.strong
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, credentialRole, credentials}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction
    extends ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
    override val authConnector: AuthConnector,
    config: FrontendAppConfig,
    val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with Logging {

  private def predicate: Predicate =
    AuthProviders(GovernmentGateway) and
      CredentialStrength(strong) and
      ConfidenceLevel.L50

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(predicate).retrieve(credentials and allEnrolments and affinityGroup and credentialRole) {
      case _ ~ _ ~ Some(Agent) ~ _                =>
        Future.successful(Redirect(routes.AgentCannotRegisterController.onPageLoad()))
      case _ ~ _ ~ Some(Individual) ~ _           =>
        Future.successful(Redirect(routes.CannotRegisterIndividualController.onPageLoad()))
      case _ ~ _ ~ _ ~ Some(Assistant)            =>
        Future.successful(Redirect(routes.AssistantCannotRegisterController.onPageLoad()))
      case Some(credentials) ~ enrolments ~ _ ~ _ =>
        val hasEori: Boolean            = enrolments.enrolments.exists(_.identifiers.exists(_.key == config.eoriIdentifierKey))
        val optCdsEori: Option[String]  =
          getOptIdentifierValue(enrolments, config.cdsEnrolmentKey, config.eoriIdentifierKey)
        val optCbamEori: Option[String] =
          getOptIdentifierValue(enrolments, config.cbamEnrolmentKey, config.eoriIdentifierKey)
        block(IdentifierRequest(request, credentials.providerId, hasEori, optCdsEori, optCbamEori))
      case _                                      =>
        logger.warn("[AuthenticatedIdentifierAction][invokeBlock] Unable to retrieve credId")
        throw new UnauthorizedException("Failed to retrieve valid auth data")
    } recover {
      case e: AuthorisationException =>
        logger.info(s"[AuthenticatedIdentifierAction][invokeBlock] AuthorisationException: $e")
        handleAuthException(e)
      case e: UnauthorizedException  =>
        logger.info(s"[AuthenticatedIdentifierAction][invokeBlock] UnauthorizedException: $e")
        Redirect(routes.UnauthorisedController.onPageLoad())
    }
  }

  private def handleAuthException: PartialFunction[Throwable, Result] = {
    case _: InsufficientEnrolments      => Redirect(routes.UnauthorisedController.onPageLoad())
    case _: InsufficientConfidenceLevel => Redirect(routes.UnauthorisedController.onPageLoad())
    case _: UnsupportedAuthProvider     => Redirect(routes.UnauthorisedController.onPageLoad())
    case _: UnsupportedAffinityGroup    => Redirect(routes.UnauthorisedController.onPageLoad())
    case _: UnsupportedCredentialRole   => Redirect(routes.UnauthorisedController.onPageLoad())
    case _: IncorrectCredentialStrength => Redirect(routes.UnauthorisedController.onPageLoad())
    case _                              =>
      Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
  }

  private def getOptIdentifierValue(
      enrolments: Enrolments,
      enrolmentKey: String,
      identifierKey: String
  ): Option[String] =
    enrolments.getEnrolment(enrolmentKey).flatMap(_.getIdentifier(identifierKey)).map(_.value)
}
