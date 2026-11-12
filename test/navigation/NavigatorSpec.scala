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

package navigation

import base.SpecBase
import controllers.routes
import models.*
import pages.*
import play.api.mvc.Call

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" when {

    "in Normal mode" must {

      "go from the ForwardLookingThreshold page to the next page" when {

        "the answer is yes" in {
          val answers = emptyUserAnswers.withPage(ForwardLookingThresholdPage, true)
          navigator.nextPage(ForwardLookingThresholdPage, NormalMode, answers) mustBe Call("GET", "/todo-yes")
        }

        "the answer is no" in {
          val answers = emptyUserAnswers.withPage(ForwardLookingThresholdPage, false)
          navigator.nextPage(ForwardLookingThresholdPage, NormalMode, answers) mustBe Call("GET", "/todo-no")
        }

        "there is no expected answer" in {
          val answers = emptyUserAnswers
          navigator.nextPage(ForwardLookingThresholdPage, NormalMode, answers) mustBe routes.JourneyRecoveryController
            .onPageLoad()
        }
      }

      "go from a page that doesn't exist in the route map to ServiceEntryController" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers) mustBe routes.ServiceEntryController.onPageLoad()
      }
    }

    "in Check mode" must {

      "go from the ForwardLookingThreshold page to CheckYourAnswers" in {

        val answers = emptyUserAnswers.withPage(ForwardLookingThresholdPage, true)
        navigator.nextPage(ForwardLookingThresholdPage, CheckMode, answers) mustBe routes.CheckYourAnswersController
          .onPageLoad()
      }

      "go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, emptyUserAnswers) mustBe routes.CheckYourAnswersController
          .onPageLoad()
      }
    }
  }
}
