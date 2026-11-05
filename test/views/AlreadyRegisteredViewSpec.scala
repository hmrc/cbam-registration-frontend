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

package views

import config.FrontendAppConfig
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.twirl.api.Html
import utils.ViewTestHelpers
import views.html.AlreadyRegisteredView

class AlreadyRegisteredViewSpec extends ViewTestHelpers {

  "AlreadyRegistered view" when {

    "returnsServiceAvailable = true" should {
      val appWithConfig: Application = new GuiceApplicationBuilder()
        .configure("features.returns-available" -> true)
        .build()

      implicit val viewTest: ViewTest = new ViewTest {
        override implicit lazy val appConfig: FrontendAppConfig = appWithConfig.injector.instanceOf[FrontendAppConfig]

        override val page: Html          = appWithConfig.injector.instanceOf[AlreadyRegisteredView].apply()
        override val pageTitle: String   = "Your organisation is already registered for CBAM"
        override val pageHeading: String = "Your organisation is already registered for CBAM"
      }

      basicChecks(hasBackLink = false)

      checkContentOnPage("You can manage your CBAM obligations in your CBAM account.", ".govuk-body:nth-of-type(1)")
      checkLinkOnPage(
        "Go to CBAM account",
        controllers.routes.AlreadyRegisteredController.onPageLoad().url,
        ".govuk-button"
      )
    }

    "returnsServiceAvailable = false" should {
      val appWithConfig: Application = new GuiceApplicationBuilder()
        .configure("features.returns-available" -> false)
        .build()

      implicit val viewTest: ViewTest = new ViewTest {
        override implicit lazy val appConfig: FrontendAppConfig = appWithConfig.injector.instanceOf[FrontendAppConfig]

        override val page: Html          = appWithConfig.injector.instanceOf[AlreadyRegisteredView].apply()
        override val pageTitle: String   = "Your organisation is already registered for CBAM"
        override val pageHeading: String = "Your organisation is already registered for CBAM"
      }

      basicChecks(hasBackLink = false)

      checkContentOnPage("You can manage your CBAM obligations in your CBAM account.", ".govuk-body:nth-of-type(1)")
      checkElementDoesNotExist(".govuk-button")
    }
  }
}
