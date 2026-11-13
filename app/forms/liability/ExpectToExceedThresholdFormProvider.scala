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

package forms.liability

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.validation.Constraint
import play.api.i18n.Messages

import java.time.{LocalDate, ZoneId, ZoneOffset}
import javax.inject.Inject

class ExpectToExceedThresholdFormProvider @Inject() extends Mappings {

  private val notBeforeJan2027: LocalDate => Boolean = { date =>
    val yearToUse: Int = if(LocalDate.now(ZoneOffset.UTC).getYear == 2026) 2026 else 2027 //TODO Remove in the new year - This is only to allow the service to work pre-2027 for testing purposes
    val startOf2027 = LocalDate.of(yearToUse, 1, 1)
    date.isAfter(startOf2027)
  }
  
  private val notAfterCurrent: LocalDate => Boolean = { date =>
    val currentDate = LocalDate.now(ZoneOffset.UTC).plusDays(1)
    date.isBefore(currentDate)
  }
  
  def apply()(implicit messages: Messages): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey     = "expectToExceedThreshold.error.invalid",
        allRequiredKey = "expectToExceedThreshold.error.required.all",
        twoRequiredKey = "expectToExceedThreshold.error.required.two",
        requiredKey    = "expectToExceedThreshold.error.required"
      )
        .verifying("expectToExceedThreshold.error.beforeJan27", notBeforeJan2027)
        .verifying("expectToExceedThreshold.error.notAfterToday", notAfterCurrent)
    )
}
