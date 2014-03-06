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

import rapaio.data._
import java.io._
import java.text.DecimalFormat
import java.util.ArrayList

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
class CsvPersistence {

  var hasHeader: Boolean = true
  var colSeparator: Char = ','
  var hasQuotas: Boolean = true
  var escapeQuotas: Char = '"'
  var trimSpaces: Boolean = true
  var fieldHints: Map[String, String] = Map[String, String]()
  var defaultTypeHint: String = "nom"
  var startRow: Int = 0
  var endRow: Int = Int.MaxValue

  def read(fileName: String): Frame = {
    read(new FileInputStream(fileName))
  }

  def read(clazz: Class[_], resource: String): Frame = {
    read(clazz.getResourceAsStream(resource))
  }

  def read(inputStream: InputStream): Frame = {
    var rows: Int = 0
    var names = List[String]()
    val vectors = new ArrayList[Feature]
    try {
      val reader = new BufferedReader(new InputStreamReader(inputStream))
      if (hasHeader) {
        val line: String = reader.readLine
        if (line == null) {
          return null
        }
        names = parseLine(line)
      }
      var first: Boolean = true
      var break = false
      while (!break) {
        val line: String = reader.readLine
        if (line == null) {
          break = true
        } else {
          val row = parseLine(line)
          if (first) {
            first = false
            for (i <- names.size until row.size) {
              names = names ::: List("V" + (i + 1))
            }
            for (i <- 0 until names.size) {
              val colName: String = names(i)
              if (fieldHints.contains(colName)) {
                fieldHints(colName) match {
                  case "idx" => vectors.add(new Index())
                  case "val" => vectors.add(new Value())
                  case "nom" => vectors.add(new Nominal())
                  case _ => vectors.add(new Nominal())
                }
              } else {
                defaultTypeHint match {
                  case "nom" => vectors.add(new Nominal)
                  case "val" => vectors.add(new Value)
                  case "idx" => vectors.add(new Index)
                }
              }
            }
          }
          if (rows < startRow) {
            rows += 1
          } else if (rows == endRow) {
            break = true
          } else {
            rows += 1
            for (i <- 0 until names.size) {
              if (row.size <= i || ("?" == row(i)) || ("NA" == row(i))) {
                vectors.get(i).missing ++()
              } else {
                val value: String = row(i)
                val v: Feature = vectors.get(i)
                v.typeName match {
                  case "idx" =>
                    var intValue: Integer = null
                    try {
                      intValue = Integer.parseInt(value)
                      v.indexes ++ intValue
                    }
                    catch {
                      case ex: Throwable => {
                        try {
                          val fallbackNumeric = java.lang.Double.parseDouble(value)
                          val num: Value = new Value
                          for (j <- 0 until v.rowCount) {
                            num.values ++ v.indexes(j)
                          }
                          num.values ++ fallbackNumeric
                          vectors.set(i, num)
                        }
                        catch {
                          case ex2: Throwable => {
                            val nom: Nominal = new Nominal
                            for (j <- 0 until v.rowCount) {
                              nom.labels ++ String.valueOf(v.indexes(j))
                            }
                            nom.labels ++ value
                            vectors.set(i, nom)
                          }
                        }
                      }
                    }
                  case "val" =>
                    try {
                      val numValue: Double = java.lang.Double.parseDouble(value)
                      v.values ++ numValue
                    }
                    catch {
                      case ex: Throwable =>
                        val nom: Nominal = new Nominal
                        v.values.foreach(x => if (x.isNaN) nom.missing ++() else nom.labels ++ x.toString)
                        nom.labels ++ value
                        vectors.set(i, nom)
                    }
                  case "nom" =>
                    v.labels.$plus$plus(value)
                }
              }
            }
          }
        }
      }
    }
    new SolidFrame(rows - startRow, vectors.toArray(Array[Feature]()), names.toArray)
  }

  /**
   * Parses a line from csv file according with the configured setting for the
   * parse. E.g. separates columns by getCol separator, but not by the getColCount
   * separators inside quotas, if quota is configured.
   *
   * @param line
   * @return
   */
  def parseLine(line: String): List[String] = {
    def nextEnd(line: String, start: Int, inQuotas: Boolean): Int = {
      if (start >= line.length) start
      else if (line(start) == colSeparator && !inQuotas) start
      else if (line(start) == colSeparator && inQuotas && hasQuotas) nextEnd(line, start + 1, inQuotas)
      else if (line(start) == escapeQuotas && hasQuotas) nextEnd(line, start + 1, !inQuotas)
      else nextEnd(line, start + 1, inQuotas)
    }

    var start = 0
    var list = List[String]()
    while (start < line.length) {
      val end = nextEnd(line, start, false)
      list = List(clean(line.substring(start, end))) ::: list
      start = end + 1
    }
    list.reverse
  }

  /**
   * Clean the string token. - remove trailing and leading spaces, before and
   * after removing quotas - remove leading and trailing quotas - remove
   * escape quota character
   *
   * @param tok
   * @return
   */
  private def clean(tok: String): String = {

    def trim(tok: String): String = if (trimSpaces) tok.trim else tok
    def unquote(tok: String): String = {
      if (hasQuotas && !tok.isEmpty) {
        val tok1 = if (tok.charAt(0) == '\"') tok.substring(1) else tok
        if (tok1.charAt(tok1.length - 1) == '\"') {
          tok1.substring(0, tok1.length - 1)
        } else tok1
      } else tok
    }

    trim {
      if (hasQuotas) unquote(trim(tok)).replace(Array[Char](escapeQuotas, '\"'), Array[Char]('\"'))
      else unquote(trim(tok))
    }
  }

  def write(df: Frame, fileName: String) {
    try {
      val os = new FileOutputStream(fileName)
      write(df, os)
      os.flush()
      os.close()
    }
  }

  def write(df: Frame, os: OutputStream) {
    try {
      val writer = new PrintWriter(new OutputStreamWriter(os))
      if (hasHeader) {
        for (i <- 0 until df.colNames.length) {
          if (i != 0) writer.append(colSeparator)
          writer.append(df.colNames(i))
        }
        writer.append("\n")
      }
      val format: DecimalFormat = new DecimalFormat("0.###############################")
      for (i <- 0 until df.rowCount) {
        for (j <- 0 until df.colCount) {
          if (j != 0) {
            writer.append(colSeparator)
          }
          if (df.col(j).missing.apply(i)) {
            writer.append("?")
          } else if (df.col(j).isNominal) {
            writer.append(unclean(df.labels.apply(i, j)))
          } else {
            writer.append(format.format(df.values.apply(i, j)))
          }
        }
        writer.append("\n")
      }
      writer.flush()
      writer.close()
    }
  }

  private def unclean(label: String): String = {
    val line: Array[Char] = new Array[Char](label.length * 2)
    var len: Int = 0
    for (i <- 0 until label.length) {
      if (label.charAt(i) == '\"') {
        line(len) = escapeQuotas
        len += 1
      }
      line(len) = label.charAt(i)
      len += 1
    }
    if (hasQuotas) "\"" + String.valueOf(line, 0, len) + "\""
    else String.valueOf(line, 0, len)
  }
}