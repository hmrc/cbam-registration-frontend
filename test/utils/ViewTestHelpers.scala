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

package utils

import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.{BeMatcher, MatchResult}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.Html

import java.util.UUID

trait ViewTestHelpers extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with Injecting {

  val requestId: String = UUID.randomUUID().toString

  private def normaliseAssetHref(href: String): String =
    if (href.contains("/assets/")) {
      href.replaceFirst("/[0-9a-f]{32}-([^/?#]+)(?=([?#].*)?$)", "/$1")
    } else href

  case class LinkCheck(expectedText: String, expectedHref: String) extends BeMatcher[(String, String)] {
    override def apply(left: (String, String)): MatchResult = {
      val (_, actualHref)            = left
      val normalisedActualHref       = normaliseAssetHref(actualHref)
      val normalisedExpectedHref     = normaliseAssetHref(expectedHref)
      val hrefsMatchAfterNormalising = normalisedActualHref == normalisedExpectedHref

      val errorMessage = left match {
        case (`expectedText`, _) if hrefsMatchAfterNormalising          => None
        case (`expectedText`, otherHref) if !hrefsMatchAfterNormalising =>
          Some(s"The href of '$otherHref' was not the same as the expected href of '$expectedHref'.")
        case (otherText, `expectedHref`) if otherText != expectedText   =>
          Some(s"The text of '$otherText' was not the same as the expected text of '$expectedText'.")
        case (otherText, otherHref)                                     =>
          Some(s"""Expected text: $expectedText
                  |Actual text:   $otherText
                  |============================
                  |Expected href: $expectedHref
                  |Actual href:   $otherHref""".stripMargin)
      }

      MatchResult(errorMessage.isEmpty, errorMessage.getOrElse(""), errorMessage.getOrElse(""))
    }
  }

  case class ListItem(text: String, value: String)

  trait ViewTest {
    implicit lazy val baseFakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    implicit lazy val mcc: Messages                                        = inject[MessagesApi].preferred(baseFakeRequest)
    implicit lazy val appConfig: FrontendAppConfig                         = inject[FrontendAppConfig]

    val page: Html
    val pageTitle: String
    val pageHeading: String

    lazy val doc: Document = Jsoup.parse(page.body)

    def select(selector: String): Elements = doc.select(selector)
  }

  def checkTitle()(implicit viewTest: ViewTest): Unit =
    s"have the title of '${viewTest.pageTitle}'" in {
      viewTest.doc.title() shouldBe s"${viewTest.pageTitle} - Register for CBAM - GOV.UK"
    }

  def checkHeading(selector: String = "h1")(implicit viewTest: ViewTest): Unit = {
    lazy val pageHeading: String = viewTest.select(selector).first().text()

    s"have the page heading of '${viewTest.pageHeading}' using the selector of '$selector'" in {
      pageHeading shouldBe viewTest.pageHeading
    }
  }

  def checkAttribute(attributeKey: String, expectedValue: String, selector: String)(implicit
      viewTest: ViewTest
  ): Unit = {
    lazy val attribute = viewTest.select(selector).attr(attributeKey)

    s"contain the attribute '$attributeKey' with value '$expectedValue' with the selector '$selector'" in {
      attribute shouldBe expectedValue
    }
  }

  def checkContentOnPage(expectedContent: String, selector: String)(implicit viewTest: ViewTest): Unit = {
    val content = viewTest.select(selector)

    val testNameLength = 40
    val testNameText   =
      if (expectedContent.length > testNameLength) expectedContent.take(testNameLength) + "..."
      else expectedContent

    s"contain the content '$testNameText' with the selector '$selector'" in {
      if (content.isEmpty) fail(s"Could not find element with selector '$selector'.")
      else {
        content.text() shouldBe expectedContent
      }
    }
  }

  def checkErrorSummary(error: String, summarySelector: String = ".govuk-error-summary", expectedErrorCount: Int = 1)(
      implicit viewTest: ViewTest
  ): Unit = {
    lazy val errorSummary = viewTest.select(summarySelector)

    "the error summary" should {
      "exist" in {
        errorSummary.size() shouldBe 1
      }

      "contain the title of 'There is a problem'" in {
        errorSummary.select("h2").first().text() shouldBe "There is a problem"
      }

      s"contains a link with the text '$error'" in {
        errorSummary.select("a").text() shouldBe Seq.fill(expectedErrorCount)(error).mkString(" ")
      }
    }
  }

  def checkErrorMessageOnForm(
      error: String,
      errorSelector: String = ".govuk-error-message",
      expectedErrorCount: Int = 1
  )(implicit viewTest: ViewTest): Unit = {
    lazy val errorMessage = viewTest.select(errorSelector)

    "the field error" should {
      "exist" in {
        errorMessage.size() shouldBe expectedErrorCount
      }

      s"contains a link with the text 'Error: $error'" in {
        errorMessage.text() shouldBe Seq.fill(expectedErrorCount)("Error: " + error).mkString(" ")
      }
    }
  }

  def checkLinkOnPage(expectedLinkText: String, expectedLinkHref: String, selector: String)(implicit
      viewTest: ViewTest
  ): Unit = {
    lazy val linkElement = viewTest.select(selector)
    lazy val linkText    = linkElement.text()
    lazy val linkHref    = linkElement.attr("href")

    s"contain a link with the text '$expectedLinkText' and href of '$expectedLinkHref' with the selector '$selector'" which {

      "is the correct link" in {
        (linkText, linkHref) shouldBe LinkCheck(expectedLinkText, expectedLinkHref)
      }
    }
  }

  def checkInputField(selector: String, expectedValue: Option[String] = None, placeholderText: Option[String] = None)(
      implicit viewTest: ViewTest
  ): Unit = {
    lazy val inputElement = viewTest.select(selector)

    s"contain an input field with selector '$selector'" which {

      "exists" in {
        inputElement.size() should not be 0
      }

      expectedValue.foreach { prePopValue =>
        s"has the value '$prePopValue' pre populated" in {
          inputElement.`val`() shouldBe prePopValue
        }
      }

      placeholderText.foreach { phText =>
        s"has the placeholder text of '$phText'" in {
          inputElement.attr("placeholder") shouldBe phText
        }
      }
    }
  }

  def checkDropdownField(
      valueAndText: Seq[ListItem],
      selector: String,
      selectedValue: Option[String] = None,
      emptyOption: Option[String] = None
  )(implicit viewTest: ViewTest): Unit = {
    lazy val dropdownField     = viewTest.select(selector)
    lazy val options           = dropdownField.select("option")
    lazy val totalOptions: Int = valueAndText.size + emptyOption.fold(0)(_ => 1)

    s"contain a dropdown with the selector '$selector'" which {

      "exists" in {
        dropdownField.size() shouldBe 1
      }

      s"there should be $totalOptions options" in {
        options.size() shouldBe totalOptions
      }

      emptyOption.foreach { option =>
        s"should have the first option of '$option'" in {
          options.first().text() shouldBe option
        }
      }

      selectedValue.foreach { sV =>
        s"the option '$sV' should be selected" in {
          options.select("[selected]").text() shouldBe sV
        }
      }

      valueAndText.zipWithIndex.foreach { case (item, index) =>
        val offset = emptyOption.fold(0)(_ => 1)

        s"the option in position $index" should {

          s"have the display text of ${item.text}" in {
            options.get(index + offset).text() shouldBe item.text
          }

          s"have the value of ${item.value}" in {
            options.get(index + offset).`val`() shouldBe item.value
          }
        }
      }
    }
  }

  def checkCheckBoxes(
      items: Seq[ListItem],
      selector: String,
      allValuesSelected: Option[Boolean] = None,
      allValuesDisabled: Option[Boolean] = None
  )(implicit viewTest: ViewTest): Unit = {
    lazy val checkListOptions = viewTest.select(selector).select(".govuk-checkboxes__item")

    s"have a check list form with the selector '$selector'" which {

      items.zipWithIndex.foreach { case (item, index) =>
        s"has the item '${item.text}' with value '${item.value}'" in {
          val option = checkListOptions.get(index)
          option.select(".govuk-checkboxes__label").text() shouldBe item.text
          option.select("input").`val`()                   shouldBe item.value
        }
      }

      allValuesSelected.foreach { selected =>
        if (selected) {
          "all the boxes are checked" in {
            checkListOptions.select("[checked]").size() shouldBe items.size
          }
        } else {
          "all the boxes are unchecked" in {
            checkListOptions.select("[checked]").size() shouldBe 0
          }
        }
      }

      allValuesDisabled.foreach { disabled =>
        if (disabled) {
          "all the boxes are disabled" in {
            checkListOptions.select("[disabled]").size() shouldBe items.size
          }
        } else {
          "all the boxes are enabled" in {
            checkListOptions.select("[disabled]").size() shouldBe 0
          }
        }
      }
    }
  }

  def checkButton(buttonText: String, selector: String)(implicit viewTest: ViewTest): Unit = {
    val button = viewTest.select(selector)

    s"contain a button with the text '$buttonText'" which {
      "exists" in {
        button should not be null
      }

      s"has the text of '$buttonText'" in {
        button.text() shouldBe buttonText
      }
    }
  }

  def checkElementExists(selector: String)(implicit viewTest: ViewTest): Unit = {
    val element = viewTest.select(selector)

    s"have an element with the selector '$selector'" in {
      element.size() should not be 0
    }
  }

  def checkElementDoesNotExist(selector: String)(implicit viewTest: ViewTest): Unit = {
    val element = viewTest.select(selector)

    s"not have an element with the selector '$selector'" in {
      element.size() shouldBe 0
    }
  }

  def checkElementSize(amount: Int, selector: String)(implicit viewTest: ViewTest): Unit = {
    val elements = viewTest.select(selector).first.children()

    s"have $amount elements with the selector '$selector'" in {
      elements.size() shouldBe amount
    }
  }

  def checkRadioButton(content: String, selected: Boolean, selector: String, checkForAttribute: String = "selected")(
      implicit viewTest: ViewTest
  ): Unit = {
    val elements    = viewTest.select(selector)
    val radioButton = elements.select("input[type=\"radio\"]")
    val label       = elements.select("label")

    s"have the radio buttons with selector $selector" which {

      s"have the label of '$content'" in {
        label.text() shouldBe content
      }

      "have a radio button attached" which {

        "exists" in {
          radioButton.size() should not be 0
        }

        s"is ${if (selected) "" else "not"} selected" in {
          radioButton.hasAttr(checkForAttribute) shouldBe selected
        }
      }

    }
  }

  def checkDateForm(day: String, month: String, year: String, rootSelector: String = "date-input")(implicit
      viewTest: ViewTest
  ): Unit = {
    val dayForm    = viewTest.select(s"#$rootSelector-day")
    val dayLabel   = viewTest.select(s"[for=$rootSelector-day]")
    val monthForm  = viewTest.select(s"#$rootSelector-month")
    val monthLabel = viewTest.select(s"[for=$rootSelector-month]")
    val yearForm   = viewTest.select(s"#$rootSelector-year")
    val yearLabel  = viewTest.select(s"[for=$rootSelector-year]")

    s"have a date form with the root selector of '$rootSelector'" which {
      case class PeriodTest(period: String, expectedValue: String, formField: Elements, label: Elements)

      Seq(
        PeriodTest("Day", day, dayForm, dayLabel),
        PeriodTest("Month", month, monthForm, monthLabel),
        PeriodTest("Year", year, yearForm, yearLabel)
      ).foreach { periodData =>
        s"has a ${periodData.period} field" which {

          s"has the label of ${periodData.period}" in {
            periodData.label.text() shouldBe periodData.period
          }

          s"has the form with the value of '${periodData.expectedValue}' in it" in {
            periodData.formField.`val`() shouldBe periodData.expectedValue
          }
        }
      }
    }
  }

  def checkFormCall(href: String, selector: String = "form")(implicit viewTest: ViewTest): Unit = {
    val form = viewTest.select(selector)

    s"have a form on the page with the action url of '$href'" in {
      form.attr("action") shouldBe href
    }
  }

  def checkListContents(listElements: Seq[String], selector: String)(implicit viewTest: ViewTest): Unit = {
    lazy val rootElement = viewTest.select(selector)
    lazy val rootList    = rootElement.first().children()

    s"have a list with selector '$selector'" which {

      s"has a size of ${listElements.size}" in {
        rootList.size() shouldBe listElements.size
      }

      listElements.zipWithIndex.foreach { case (element, index) =>
        s"contains a row with the text '$element' in position ${index + 1}" in {
          if (rootElement.isEmpty) {
            fail(s"Cannot find the element with the selector '$selector'.")
          } else if (rootList.isEmpty) {
            fail(s"Cannot find text '$element'. Element at index $index does not exist.")
          } else {
            rootList.get(index).text shouldBe element
          }
        }
      }
    }
  }

  case class TableRowTest(text: String*)

  def checkTable(headings: Seq[String], rows: Seq[TableRowTest], selector: String)(implicit
      viewTest: ViewTest
  ): Unit = {
    lazy val tableElement = viewTest.select(selector).first()
    lazy val tableHeaders = tableElement.select("thead > tr").first().children()
    lazy val tableRows    = tableElement.select("tbody").first().children()

    s"have a table with selector '$selector'" which {

      s"has ${headings.size} columns" in {
        tableHeaders.size() shouldBe headings.size
      }

      "has the following headers" which {
        headings.zipWithIndex.foreach { case (heading, index) =>
          s"'$heading' in position ${index + 1}" in {
            tableHeaders.get(index).text() shouldBe heading
          }
        }
      }

      s"has ${rows.size} rows" in {
        tableRows.size() shouldBe rows.size
      }

      "has rows with the correct content" which {
        lazy val allRowsIndexed = rows.zipWithIndex

        allRowsIndexed.foreach { case (row, index) =>
          s"row ${index + 1}" should {

            s"have ${row.text.size} columns" in {
              tableRows.get(index).children().size() shouldBe row.text.size
            }

            "have the following content in each column" which {
              lazy val rowColumnsIndexed = row.text.zipWithIndex

              rowColumnsIndexed.foreach { case (columnText, columnIndex) =>
                s"has '$columnText' in position ${columnIndex + 1}" in {
                  val htmlRow    = tableRows.get(index).children()
                  val htmlColumn = htmlRow.get(columnIndex)

                  htmlColumn.text() shouldBe columnText
                }
              }
            }
          }
        }
      }
    }
  }
}
