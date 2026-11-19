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

import pages.*
import play.api.i18n.{Lang, Messages}
import play.api.libs.json.*
import queries.{Gettable, Settable}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import utils.DateTimeFormats

import java.time.{Instant, LocalDate}
import java.time.temporal.ChronoUnit.DAYS
import scala.util.{Failure, Success, Try}

final case class UserAnswers(
    credId: String,
    data: JsObject = Json.obj(),
    lastUpdated: Instant = Instant.now
) {

  def expiryDateString(implicit messages: Messages): String = {
    implicit val lang: Lang = messages.lang
    val dateFormatter       = DateTimeFormats.dateTimeFormat()
    dateFormatter.format(lastUpdated.plus(30, DAYS))
  }

  def liabilityDetails: Option[LiabilityDetails] = {

    def adjustBackwardLookDate(date: LocalDate): LocalDate =
      if (date.getDayOfMonth == 1) date
      else date.withDayOfMonth(1).plusMonths(1)

    (this.get(ForwardLookingThresholdPage), this.get(BackwardLookingThresholdPage)) match {
      case (Some(true), Some(false)) =>
        this.get(ExpectToExceedThresholdPage).map { date =>
          LiabilityDetails(date, isForwardLookEarliest = true)
        }
      case (Some(false), Some(true)) =>
        this.get(BackwardLookingDatePage).map { date =>
          LiabilityDetails(adjustBackwardLookDate(date), isForwardLookEarliest = false)
        }
      case (Some(true), Some(true))  =>
        (this.get(ExpectToExceedThresholdPage), this.get(BackwardLookingDatePage)) match {
          case (Some(forwardLookDate), Some(backwardLookDate)) =>
            val adjustedBackwardLookDate = adjustBackwardLookDate(backwardLookDate)
            val isForwardLookEarliest    = !forwardLookDate.isAfter(adjustedBackwardLookDate)
            val liabilityStartDate       = if (isForwardLookEarliest) forwardLookDate else adjustedBackwardLookDate
            Some(LiabilityDetails(liabilityStartDate, isForwardLookEarliest))
          case _                                               => None
        }
      case _                         => None
    }
  }

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors)       =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(Some(value), updatedAnswers)
    }
  }

  def remove[A](page: Settable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_)            =>
        Success(data)
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(None, updatedAnswers)
    }
  }
}

object UserAnswers {

  val reads: Reads[UserAnswers] = {

    import play.api.libs.functional.syntax.*

    (
      (__ \ "_id").read[String] and
        (__ \ "data").read[JsObject] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
    )(UserAnswers.apply _)
  }

  val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax.*

    (
      (__ \ "_id").write[String] and
        (__ \ "data").write[JsObject] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
    )(ua => (ua.credId, ua.data, ua.lastUpdated))
  }

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)
}
