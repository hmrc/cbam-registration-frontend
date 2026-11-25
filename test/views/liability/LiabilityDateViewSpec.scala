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

package views.liability

import play.twirl.api.Html
import utils.ViewTestHelpers
import views.html.liability.LiabilityDateView

import java.time.LocalDate

class LiabilityDateViewSpec extends ViewTestHelpers {

  private val title: String = "Liability start date for CBAM"

  private val guidanceUrl: String =
    "https://www.gov.uk/guidance/work-out-the-date-youll-need-to-register-for-carbon-border-adjustment-mechanism-cbam"

  private val checkOrganisationDetailsUrl: String =
    controllers.checkDetails.routes.CheckOrganisationDetailsController.onPageLoad().url

  private def viewTestFor(
      isForwardLook: Boolean,
      liabilityStartDate: LocalDate,
      backwardLookDate: Option[LocalDate],
      registrationDeadline: LocalDate,
      isLate: Boolean
  ): ViewTest = new ViewTest {
    override val page: Html          =
      inject[LiabilityDateView].apply(
        isForwardLook,
        liabilityStartDate,
        backwardLookDate,
        registrationDeadline,
        isLate
      )
    override val pageTitle: String   = title
    override val pageHeading: String = title
  }

  "LiabilityDate view" when {

    "the liability start date is determined by the forward looking threshold" when {

      "the registration is on or before the deadline" should {
        implicit val viewTest: ViewTest =
          viewTestFor(
            isForwardLook = true,
            liabilityStartDate = LocalDate.of(2026, 4, 1),
            backwardLookDate = None,
            registrationDeadline = LocalDate.of(2026, 5, 1),
            isLate = false
          )

        basicChecks()

        checkContentOnPage(
          "Your organisation’s liability for CBAM started on 1 April 2026.",
          ".govuk-body:nth-of-type(1)"
        )
        checkContentOnPage("1 April 2026", ".govuk-body:nth-of-type(1) > strong")

        checkContentOnPage(
          "This is the date you first expected to import £50,000 or more worth of CBAM goods into the UK within the next 30 days.",
          ".govuk-body:nth-of-type(2)"
        )

        checkContentOnPage(
          "You must submit your registration by 1 May 2026. If your submission is late, you may be charged a penalty.",
          ".govuk-body:nth-of-type(3)"
        )
        checkContentOnPage("1 May 2026", ".govuk-body:nth-of-type(3) > strong")

        checkElementDoesNotExist("#threshold-reached")
        checkElementDoesNotExist("#guidance")

        checkLinkOnPage("Save and continue", checkOrganisationDetailsUrl, ".govuk-button")
      }

      "the registration is after the deadline" should {
        implicit val viewTest: ViewTest =
          viewTestFor(
            isForwardLook = true,
            liabilityStartDate = LocalDate.of(2026, 4, 1),
            backwardLookDate = None,
            registrationDeadline = LocalDate.of(2026, 5, 1),
            isLate = true
          )

        checkContentOnPage(
          "Your deadline to submit your CBAM registration was 1 May 2026. Submit your registration as soon as possible. Late submissions may be charged a penalty.",
          ".govuk-body:nth-of-type(3)"
        )
        checkContentOnPage("1 May 2026", ".govuk-body:nth-of-type(3) > strong")
      }

      "a BackwardLookingDate date is also present" should {
        implicit val viewTest: ViewTest =
          viewTestFor(
            isForwardLook = true,
            liabilityStartDate = LocalDate.of(2027, 3, 10),
            backwardLookDate = Some(LocalDate.of(2027, 4, 15)),
            registrationDeadline = LocalDate.of(2027, 4, 10),
            isLate = false
          )

        checkContentOnPage(
          "Your organisation’s liability for CBAM started on 10 March 2027.",
          ".govuk-body:nth-of-type(1)"
        )

        checkElementDoesNotExist("#threshold-reached")
      }
    }

    "the liability start date is determined by the backward looking threshold" when {

      "the registration is on or before the deadline" should {
        implicit val viewTest: ViewTest =
          viewTestFor(
            isForwardLook = false,
            liabilityStartDate = LocalDate.of(2027, 2, 1),
            backwardLookDate = Some(LocalDate.of(2027, 2, 15)),
            registrationDeadline = LocalDate.of(2027, 3, 1),
            isLate = false
          )

        basicChecks()

        checkContentOnPage(
          "Your imports of CBAM goods reached £50,000 or more on 15 February 2027.",
          ".govuk-body:nth-of-type(1)"
        )
        checkContentOnPage("15 February 2027", ".govuk-body:nth-of-type(1) > strong")

        checkContentOnPage(
          "This means your liability for CBAM started on 1 February 2027 because CBAM is tested on the first day of the month.",
          ".govuk-body:nth-of-type(2)"
        )
        checkContentOnPage("1 February 2027", ".govuk-body:nth-of-type(2) > strong")

        checkContentOnPage(
          "You must submit your registration by 1 March 2027. If your submission is late, you may be charged a penalty.",
          ".govuk-body:nth-of-type(4)"
        )
        checkContentOnPage("1 March 2027", ".govuk-body:nth-of-type(4) > strong")

        checkLinkOnPage(
          "Find out more information about when you are liable to register for CBAM (opens in new tab)",
          guidanceUrl,
          ".govuk-body:nth-of-type(3) > a"
        )
        checkAttribute("target", "_blank", ".govuk-body:nth-of-type(3) > a")

        checkElementDoesNotExist("#forward-look-explanation")

        checkLinkOnPage("Save and continue", checkOrganisationDetailsUrl, ".govuk-button")
      }

      "the registration is after the deadline" should {
        implicit val viewTest: ViewTest =
          viewTestFor(
            isForwardLook = false,
            liabilityStartDate = LocalDate.of(2027, 2, 1),
            backwardLookDate = Some(LocalDate.of(2027, 2, 15)),
            registrationDeadline = LocalDate.of(2027, 3, 1),
            isLate = true
          )

        checkContentOnPage(
          "Your deadline to submit your CBAM registration was 1 March 2027. Submit your registration as soon as possible. Late submissions may be charged a penalty.",
          ".govuk-body:nth-of-type(4)"
        )
        checkContentOnPage("1 March 2027", ".govuk-body:nth-of-type(4) > strong")
      }
    }

    "only one deadline message is shown" should {
      implicit val viewTest: ViewTest =
        viewTestFor(
          isForwardLook = true,
          liabilityStartDate = LocalDate.of(2026, 4, 1),
          backwardLookDate = None,
          registrationDeadline = LocalDate.of(2026, 5, 1),
          isLate = true
        )

      "have exactly one deadline paragraph" in {
        viewTest.select("#deadline").size() shouldBe 1
      }
    }
  }
}
