package tech.cryptonomic.nautilus.cloud.tools

import org.scalatest.matchers.MatchResult
import org.scalatest.matchers.Matcher
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode

import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait JsonMatchers {

  /**
    * Checks if the given json objects are equivalent.
    */
  def matchJson(right: String): Matcher[String] =
    Matcher[String] { left =>
      Try(
        JSONCompare.compareJSON(right, left, JSONCompareMode.LENIENT)
      ) match {
        case Failure(_) =>
          MatchResult(
            matches = false,
            rawFailureMessage = "Could not parse json {0} did not equal {1}",
            rawNegatedFailureMessage = "Json should not have matched {0} {1}",
            args = IndexedSeq(left.trim, right.trim)
          )
        case Success(jSONCompareResult) =>
          MatchResult(
            matches = jSONCompareResult.passed(),
            rawFailureMessage = "Json did not match {0} did not match {1}\n\nJson Diff:\n{2}",
            rawNegatedFailureMessage = "Json should not have matched {0} matched {1}\n\nJson Diff:\n{2}",
            args = IndexedSeq(left.trim, right.trim, jSONCompareResult.getMessage)
          )
      }
    }
}

object JsonMatchers extends JsonMatchers
