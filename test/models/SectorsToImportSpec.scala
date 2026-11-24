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

package models

import base.SpecBase
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.all.CheckboxItemViewModel

class SectorsToImportSpec extends SpecBase {

  "SectorsToImport.checkboxItems" must {

    "generate a list of checkbox items from a list of Strings" in {

      val listItems             = Seq("Aluminium", "Cement", "Fertiliser")
      val expectedCheckboxItems = Seq(
        CheckboxItemViewModel(
          content = Text("Aluminium"),
          fieldId = "value",
          index = 0,
          value = "Aluminium"
        ),
        CheckboxItemViewModel(
          content = Text("Cement"),
          fieldId = "value",
          index = 1,
          value = "Cement"
        ),
        CheckboxItemViewModel(
          content = Text("Fertiliser"),
          fieldId = "value",
          index = 2,
          value = "Fertiliser"
        )
      )
      val checkboxItems         = SectorsToImport.checkboxItems(listItems)(messages(app))

      checkboxItems mustBe expectedCheckboxItems
    }
  }
}
