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

import forms.behaviours.DateBehaviours
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.Helpers.stubMessages

import java.time.{LocalDate, ZoneOffset}

class ExpectToExceedThresholdFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()
  private val form = new ExpectToExceedThresholdFormProvider()()

  ".value" should {
    
    "parse correctly" when {
      
      "a valid date is presented" in {
        val todaysDate = LocalDate.now(ZoneOffset.UTC)
        
        val boundForm = form.bind(
          Map(
            "value.day" -> todaysDate.getDayOfMonth.toString,
            "value.month" -> todaysDate.getMonthValue.toString,
            "value.year" -> todaysDate.getYear.toString
          )
        )
        
        boundForm.errors.length mustBe 0
        boundForm.value mustBe Some(todaysDate)
      }
    }
    
    "return an error" when {
      
      "the date is pre-2027" in {
        form.bind(
          Map(
            "value.day" -> 1.toString,
            "value.month" -> 1.toString,
            "value.year" -> 2025.toString
          )
        ).error("value").map(_.message) mustBe Some("expectToExceedThreshold.error.beforeJan27")
      }
      
      "the date is after the current date" in {
        val dayAfter: LocalDate = LocalDate.now(ZoneOffset.UTC).plusDays(1)
        
        form.bind(
          Map(
            "value.day" -> dayAfter.getDayOfMonth.toString,
            "value.month" -> dayAfter.getMonthValue.toString,
            "value.year" -> dayAfter.getYear.toString
          )
        ).error("value").map(_.message) mustBe Some("expectToExceedThreshold.error.notAfterToday")
      }
      
    }
  }
}
