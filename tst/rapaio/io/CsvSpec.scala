/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package rapaio.io

import org.scalatest.{Matchers, FlatSpec}

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
class CsvSpec extends FlatSpec with Matchers {

  var colSeparator: Char = ','
  var hasQuotas: Boolean = true
  var escapeQuotas: Char = '"'
  var trimSpaces: Boolean = true

  def parseLine(line: String): List[String] = {
    def nextEnd(line: String, start: Int, inQuotas: Boolean): Int = {
      if (start >= line.length) start
      else if (line(start) == colSeparator && !inQuotas) start
      else if (line(start) == colSeparator && inQuotas && hasQuotas) nextEnd(line, start + 1, inQuotas)
      else if (line(start) == escapeQuotas && hasQuotas) nextEnd(line, start + 1, !inQuotas)
      else nextEnd(line, start + 1, inQuotas)
    }

    var start = 0
    var list: List[String] = Nil
    while (start < line.length) {
      val end = nextEnd(line, start, false)
      list = List[String](clean(line.substring(start, end))) ::: list
      start = end + 1
    }
    list.reverse
  }

  private def clean(_tok: String): String = {
    var tok = _tok
    if (trimSpaces) {
      tok = tok.trim
    }
    if (hasQuotas && !tok.isEmpty) {
      if (tok.charAt(0) == '\"') {
        tok = tok.substring(1)
      }
      if (tok.length > 0 && tok.charAt(tok.length - 1) == '\"') {
        tok = tok.substring(0, tok.length - 1)
      }
    }
    if (hasQuotas) {
      tok = tok.replace(Array[Char](escapeQuotas, '\"'), Array[Char]('\"'))
    }
    if (trimSpaces) {
      tok = tok.trim
    }
    tok
  }

  "line parser " should " return the whole content if no col separators" in {
    colSeparator = ','
    hasQuotas = true
    escapeQuotas = '"'
    trimSpaces = true
    parseLine("a") should be(List[String]("a"))
  }

  it should " return split when is well separated, no escapes, and no trim" in {
    colSeparator = ','
    hasQuotas = false
    escapeQuotas = '"'
    trimSpaces = false
    parseLine("a,b,c,dd") should be(List[String]("a", "b", "c", "dd"))
  }

  it should " return split when is well separated, no escapes, and trim" in {
    colSeparator = ','
    hasQuotas = false
    escapeQuotas = '"'
    trimSpaces = true
    parseLine("  a, b ,c,  dd") should be(List[String]("a", "b", "c", "dd"))
  }

  it should " return empty strings when has only spaces, is well separated, no escapes, and trim" in {
    colSeparator = ','
    hasQuotas = false
    escapeQuotas = '"'
    trimSpaces = true
    parseLine("  ,  ,,  ") should be(List[String]("", "", "", ""))
  }

  it should " return empty strings when has only spaces with escapes, and trim" in {
    colSeparator = ','
    hasQuotas = true
    escapeQuotas = '"'
    trimSpaces = true
    parseLine( """  "",  "  ",, " """) should be(List[String]("", "", "", ""))
  }

  it should " return trim with escapes, and trim" in {
    colSeparator = ','
    hasQuotas = true
    escapeQuotas = '"'
    trimSpaces = true
    parseLine( """ "ana "" ",  "ana  ",ana, "ana ana """) should be(List[String]("ana \"", "ana", "ana", "ana ana"))
  }


}
