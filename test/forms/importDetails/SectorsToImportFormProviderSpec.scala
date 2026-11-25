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

package forms.importDetails

import forms.SectorsToImportFormProvider
import forms.behaviours.CheckboxFieldBehaviours

class SectorsToImportFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new SectorsToImportFormProvider()()

  ".value" must {

    val fieldName   = "value"
    val requiredKey = "sectorsToImport.error.required"

    "bind a string successfully" in {
      val result = form.bind(Map(s"$fieldName[0]" -> "example"))
      result.get mustEqual Set("example")
      result.errors mustBe empty
    }

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
