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

package pages

import base.SpecBase
import controllers.routes
import models.*
import play.api.mvc.Call

class SectorsToImportPageSpec extends SpecBase {

  "SectorsToImportPage" when {

    "in Normal mode" must {

      "go to the next page" when {

        "sectors have been selected" in {
          val answers = emptyUserAnswers.withPage(SectorsToImportPage, Set("Aluminium", "Cement"))
          SectorsToImportPage.nextPage(NormalMode, answers) mustBe Call("GET", "/todo-25")
        }

        "there is no answer in the user answers" in {
          val answers = emptyUserAnswers
          SectorsToImportPage.nextPage(NormalMode, answers) mustBe routes.JourneyRecoveryController.onPageLoad()
        }
      }
    }

    "in Check mode" must {

      "go to Journey Recovery" in {
        val answers = emptyUserAnswers.withPage(SectorsToImportPage, Set("Aluminium"))
        SectorsToImportPage.nextPage(CheckMode, answers) mustBe routes.JourneyRecoveryController.onPageLoad()
      }
    }
  }
}
