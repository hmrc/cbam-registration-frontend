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
import config.FrontendAppConfig
import controllers.routes
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.mvc.Results.Ok
import play.api.mvc.{BodyParsers, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.CredentialStrength.strong
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, credentialRole, credentials}
import uk.gov.hmrc.auth.core.retrieve.{~, Credentials}

import scala.concurrent.Future

class IdentifierActionSpec extends SpecBase {

  val mockAuthConnector: AuthConnector       = mock[AuthConnector]
  val mockAppConfig: FrontendAppConfig       = mock[FrontendAppConfig]
  val defaultBodyParser: BodyParsers.Default = app.injector.instanceOf[BodyParsers.Default]

  val someCredentials  = Some(Credentials(testCredId, "GovernmentGateway"))
  val cbamEnrolmentKey = "HMRC-CBAM-ORG"
  val cdsEnrolmentKey  = "HMRC-CUS-ORG"
  val identifierKey    = "EORI"
  val state            = "Activated"
  val testContent      = "Test"

  val cdsAndCbamEnrolments: Enrolments = Enrolments(
    Set(
      Enrolment(cdsEnrolmentKey, Seq(EnrolmentIdentifier(identifierKey, testEori)), state),
      Enrolment(cbamEnrolmentKey, Seq(EnrolmentIdentifier(identifierKey, testEori)), state)
    )
  )
  val cdsEnrolment: Enrolments         = Enrolments(
    Set(Enrolment(cdsEnrolmentKey, Seq(EnrolmentIdentifier(identifierKey, testEori)), state))
  )
  val otherEoriEnrolment: Enrolments   = Enrolments(
    Set(Enrolment("HMRC-ATAR-ORG", Seq(EnrolmentIdentifier(identifierKey, testEori)), state))
  )
  val adrEnrolment: Enrolments         = Enrolments(
    Set(Enrolment("HMRC-AD-ORG", Seq(EnrolmentIdentifier("APPAID", "XMADP1234567890")), state))
  )
  val emptyEnrolments: Enrolments      = Enrolments(Set.empty[Enrolment])

  val testIdentifierAction: AuthenticatedIdentifierAction =
    new AuthenticatedIdentifierAction(
      mockAuthConnector,
      mockAppConfig,
      defaultBodyParser
    )

  val testAction: Request[_] => Future[Result] = { _ =>
    Future.successful(Ok(testContent))
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector, mockAppConfig)
    when(mockAppConfig.cdsEnrolmentKey).thenReturn(cdsEnrolmentKey)
    when(mockAppConfig.cbamEnrolmentKey).thenReturn(cbamEnrolmentKey)
    when(mockAppConfig.eoriIdentifierKey).thenReturn(identifierKey)
  }

  "AuthenticatedIdentifierAction.invokeBlock" must {
    "execute the block and return OK if authorised with no enrolments" in {
      when(
        mockAuthConnector.authorise(
          ArgumentMatchers.eq(
            AuthProviders(GovernmentGateway)
              and CredentialStrength(strong)
              and ConfidenceLevel.L50
          ),
          ArgumentMatchers.eq(
            credentials and allEnrolments and affinityGroup and credentialRole
          )
        )(any(), any())
      )
        .thenReturn(Future(new ~(new ~(new ~(someCredentials, emptyEnrolments), Some(Organisation)), Some(User))))

      val result: Future[Result] = testIdentifierAction.invokeBlock(
        FakeRequest(),
        { (request: IdentifierRequest[_]) =>
          request.credId   mustBe testCredId
          request.hasEori  mustBe false
          request.cdsEori  mustBe None
          request.cbamEori mustBe None
          Future.successful(Ok(testContent))
        }
      )

      status(result)          mustBe OK
      contentAsString(result) mustBe testContent
    }

    "execute the block and return OK if authorised with an enrolment without an EORI" in {
      when(
        mockAuthConnector.authorise(
          ArgumentMatchers.eq(
            AuthProviders(GovernmentGateway)
              and CredentialStrength(strong)
              and ConfidenceLevel.L50
          ),
          ArgumentMatchers.eq(
            credentials and allEnrolments and affinityGroup and credentialRole
          )
        )(any(), any())
      )
        .thenReturn(Future(new ~(new ~(new ~(someCredentials, adrEnrolment), Some(Organisation)), Some(User))))

      val result: Future[Result] = testIdentifierAction.invokeBlock(
        FakeRequest(),
        { (request: IdentifierRequest[_]) =>
          request.credId   mustBe testCredId
          request.hasEori  mustBe false
          request.cdsEori  mustBe None
          request.cbamEori mustBe None
          Future.successful(Ok(testContent))
        }
      )

      status(result)          mustBe OK
      contentAsString(result) mustBe testContent
    }

    "execute the block and return OK if authorised with an enrolment with EORI, but not CDS or CBAM" in {
      when(
        mockAuthConnector.authorise(
          ArgumentMatchers.eq(
            AuthProviders(GovernmentGateway)
              and CredentialStrength(strong)
              and ConfidenceLevel.L50
          ),
          ArgumentMatchers.eq(
            credentials and allEnrolments and affinityGroup and credentialRole
          )
        )(any(), any())
      )
        .thenReturn(Future(new ~(new ~(new ~(someCredentials, otherEoriEnrolment), Some(Organisation)), Some(User))))

      val result: Future[Result] = testIdentifierAction.invokeBlock(
        FakeRequest(),
        { (request: IdentifierRequest[_]) =>
          request.credId   mustBe testCredId
          request.hasEori  mustBe true
          request.cdsEori  mustBe None
          request.cbamEori mustBe None
          Future.successful(Ok(testContent))
        }
      )

      status(result)          mustBe OK
      contentAsString(result) mustBe testContent
    }

    "execute the block and return OK if authorised with a CDS enrolment only" in {
      when(
        mockAuthConnector.authorise(
          ArgumentMatchers.eq(
            AuthProviders(GovernmentGateway)
              and CredentialStrength(strong)
              and ConfidenceLevel.L50
          ),
          ArgumentMatchers.eq(
            credentials and allEnrolments and affinityGroup and credentialRole
          )
        )(any(), any())
      )
        .thenReturn(Future(new ~(new ~(new ~(someCredentials, cdsEnrolment), Some(Organisation)), Some(User))))

      val result: Future[Result] = testIdentifierAction.invokeBlock(
        FakeRequest(),
        { (request: IdentifierRequest[_]) =>
          request.credId   mustBe testCredId
          request.hasEori  mustBe true
          request.cdsEori  mustBe Some(testEori)
          request.cbamEori mustBe None
          Future.successful(Ok(testContent))
        }
      )

      status(result)          mustBe OK
      contentAsString(result) mustBe testContent
    }

    "execute the block and return OK if authorised with CDS and CBAM enrolments" in {
      when(
        mockAuthConnector.authorise(
          ArgumentMatchers.eq(
            AuthProviders(GovernmentGateway)
              and CredentialStrength(strong)
              and ConfidenceLevel.L50
          ),
          ArgumentMatchers.eq(
            credentials and allEnrolments and affinityGroup and credentialRole
          )
        )(any(), any())
      )
        .thenReturn(Future(new ~(new ~(new ~(someCredentials, cdsAndCbamEnrolments), Some(Organisation)), Some(User))))

      val result: Future[Result] = testIdentifierAction.invokeBlock(
        FakeRequest(),
        { (request: IdentifierRequest[_]) =>
          request.credId   mustBe testCredId
          request.hasEori  mustBe true
          request.cdsEori  mustBe Some(testEori)
          request.cbamEori mustBe Some(testEori)
          Future.successful(Ok(testContent))
        }
      )

      status(result)          mustBe OK
      contentAsString(result) mustBe testContent
    }

    "redirect to AgentCannotRegister page when affinity group is Agent" in {
      when(
        mockAuthConnector.authorise(
          ArgumentMatchers.eq(
            AuthProviders(GovernmentGateway)
              and CredentialStrength(strong)
              and ConfidenceLevel.L50
          ),
          ArgumentMatchers.eq(
            credentials and allEnrolments and affinityGroup and credentialRole
          )
        )(any(), any())
      )
        .thenReturn(Future(new ~(new ~(new ~(someCredentials, emptyEnrolments), Some(Agent)), Some(User))))

      val result: Future[Result] = testIdentifierAction.invokeBlock(FakeRequest(), testAction)

      status(result)                 mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.AgentCannotRegisterController.onPageLoad().url
    }

    "redirect to CannotRegisterIndividual page when affinity group is Individual" in {
      when(
        mockAuthConnector.authorise(
          ArgumentMatchers.eq(
            AuthProviders(GovernmentGateway)
              and CredentialStrength(strong)
              and ConfidenceLevel.L50
          ),
          ArgumentMatchers.eq(
            credentials and allEnrolments and affinityGroup and credentialRole
          )
        )(any(), any())
      )
        .thenReturn(Future(new ~(new ~(new ~(someCredentials, emptyEnrolments), Some(Individual)), Some(User))))

      val result: Future[Result] = testIdentifierAction.invokeBlock(FakeRequest(), testAction)

      status(result)                 mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.CannotRegisterIndividualController.onPageLoad().url
    }

    "redirect to AssistantCannotRegister page when credential role is Assistant" in {
      when(
        mockAuthConnector.authorise(
          ArgumentMatchers.eq(
            AuthProviders(GovernmentGateway)
              and CredentialStrength(strong)
              and ConfidenceLevel.L50
          ),
          ArgumentMatchers.eq(
            credentials and allEnrolments and affinityGroup and credentialRole
          )
        )(any(), any())
      )
        .thenReturn(
          Future(new ~(new ~(new ~(someCredentials, emptyEnrolments), Some(Organisation)), Some(Assistant)))
        )

      val result: Future[Result] = testIdentifierAction.invokeBlock(FakeRequest(), testAction)

      status(result)                 mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.AssistantCannotRegisterController.onPageLoad().url
    }

    "redirect to Unauthorised page if credentials cannot be retrieved" in {
      when(
        mockAuthConnector.authorise(
          ArgumentMatchers.eq(
            AuthProviders(GovernmentGateway)
              and CredentialStrength(strong)
              and ConfidenceLevel.L50
          ),
          ArgumentMatchers.eq(
            credentials and allEnrolments and affinityGroup and credentialRole
          )
        )(any(), any())
      )
        .thenReturn(Future(new ~(new ~(new ~(None, emptyEnrolments), Some(Organisation)), Some(User))))

      val result: Future[Result] = testIdentifierAction.invokeBlock(FakeRequest(), testAction)

      status(result)                 mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad().url
    }

    "throw an exception when the auth connector call fails" in {
      when(
        mockAuthConnector.authorise(
          ArgumentMatchers.eq(
            AuthProviders(GovernmentGateway)
              and CredentialStrength(strong)
              and ConfidenceLevel.L50
          ),
          ArgumentMatchers.eq(
            credentials and allEnrolments and affinityGroup and credentialRole
          )
        )(any(), any())
      )
        .thenReturn(Future.failed(new RuntimeException("bang")))

      val result = intercept[RuntimeException] {
        await(testIdentifierAction.invokeBlock(FakeRequest(), testAction))
      }

      result.getMessage must include("bang")
    }

    "redirect to the login page if no longer authorised or never logged in" in {
      List(
        BearerTokenExpired(),
        MissingBearerToken(),
        InvalidBearerToken(),
        SessionRecordNotFound()
      ).foreach { exception =>
        when(mockAppConfig.loginUrl).thenReturn("/test")
        when(mockAppConfig.loginContinueUrl).thenReturn("/test2")
        when(mockAuthConnector.authorise[Unit](any(), any())(any(), any())).thenReturn(Future.failed(exception))

        val result: Future[Result] = testIdentifierAction.invokeBlock(FakeRequest(), testAction)

        status(result)                 mustBe SEE_OTHER
        redirectLocation(result).value mustBe "/test?continue=%2Ftest2"
      }
    }
  }

}
