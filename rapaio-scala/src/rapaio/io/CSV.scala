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

import java.io._
import java.util.NoSuchElementException
import scala.util.parsing.input.PagedSeqReader
import scala.collection.immutable.PagedSeq
import scala.util.parsing.combinator.RegexParsers
import rapaio.data._
import scala.collection.mutable
import scala.Some

/**
 * @author <a href="email:padreati@yahoo.com>Aurelian Tutuianu</a>
 */

object CSV {

  def read(file: File, hasHeader: Boolean = true, defaultType: String = "val"): Frame = {
    val opt = new CSVFormat {}
    val csv = CSVReader.open(new FileReader(file))(opt)

    val names = new mutable.MutableList[String]
    val vectors = new mutable.MutableList[Feature]
    val it = csv.iterator

    var rows = 0
    if (hasHeader) {
      val next = it.next()
      for (i <- 0 until next.size) {
        names += next(i)
      }
    }

    while (it.hasNext) {
      val next = it.next()
      for (i <- 0 until next.size) {
        assureName(names, i)
        pushValue(vectors, i, rows, next(i))
      }
      rows += 1
    }

    new SolidFrame(rows, vectors.toArray[Feature], names.toArray[String])
  }

  private def assureName(names: mutable.MutableList[String], index: Int): String = {
    if (names.length <= index)
      for (i <- names.length to index) names ++ ("V" + i).toString
    names(index)
  }

  private def pushValue(vectors: mutable.MutableList[Feature], index: Int, row: Int, value: String) {
    if (vectors.length <= index)
      for (i <- vectors.length to index) vectors += new Nominal()
    if (vectors(index).rowCount <= row - 1)
      for (i <- vectors(index).rowCount to row - 1) {
        vectors(index).missing ++
      }
    vectors(index).labels ++ value
  }

}


/*
* Copyright 2013 Toshiyuki Takahashi
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
protected class CSVReader protected(private val reader: Reader)(implicit format: CSVFormat) {

  val delimiter: Char = format.delimiter
  val quoteChar: Char = format.quoteChar

  private val parser = new CSVParser(format)

  private var pagedReader: parser.Input = new PagedSeqReader(PagedSeq.fromReader(reader))

  private def handleParseError[A, B]: PartialFunction[parser.ParseResult[A], B] = {
    case parser.Failure(msg, _) => throw new MalformedCSVException(msg)
    case parser.Error(msg, _) => throw new CSVParserException(msg)
  }

  def readNext(): Option[List[String]] = {

    def handleParseResult = handleParseSuccess.orElse(handleParseError[List[String], (List[String], parser.Input)])

    def handleParseSuccess: PartialFunction[parser.ParseResult[List[String]], (List[String], parser.Input)] = {
      case parser.Success(result, input) => (result, input)
    }

    if (pagedReader.atEnd) {
      None
    } else {
      val parseResult = parser.parseLine(pagedReader)
      val (result, input) = handleParseResult(parseResult)
      pagedReader = input
      Some(result)
    }
  }

  def foreach(f: Seq[String] => Unit): Unit = iterator.foreach(f)

  def iterator: Iterator[Seq[String]] = new Iterator[Seq[String]] {

    private var _next: Option[Seq[String]] = None

    def hasNext: Boolean = {
      _next match {
        case Some(row) => true
        case None => {
          _next = readNext
          _next.isDefined
        }
      }
    }

    def next(): Seq[String] = {
      _next match {
        case Some(row) => {
          val _row = row
          _next = None
          _row
        }
        case None => readNext.getOrElse(throw new NoSuchElementException("next on empty iterator"))
      }
    }
  }

  def toStream(): Stream[List[String]] =
    Stream.continually(readNext).takeWhile(_.isDefined).map(_.get)

  def all(): List[List[String]] = {
    toStream().toList
  }


  def allWithHeaders(): List[Map[String, String]] = {
    readNext() map {
      headers =>
        val lines = all()
        lines.map(l => headers.zip(l).toMap)
    } getOrElse List()
  }

  def close(): Unit = reader.close()
}

protected object CSVReader {

  val DEFAULT_ENCODING = "UTF-8"

  def open(reader: Reader)(implicit format: CSVFormat): CSVReader =
    new CSVReader(reader)(format)

  def open(file: File)(implicit format: CSVFormat): CSVReader = {
    open(file, this.DEFAULT_ENCODING)(format)
  }

  def open(file: File, encoding: String)(implicit format: CSVFormat): CSVReader = {
    val fin = new FileInputStream(file)
    try {
      val reader = new InputStreamReader(fin, encoding)
      open(reader)(format)
    } catch {
      case e: UnsupportedEncodingException => fin.close(); throw e
    }
  }

  def open(filename: String)(implicit format: CSVFormat): CSVReader =
    open(new File(filename), this.DEFAULT_ENCODING)(format)

  def open(filename: String, encoding: String)(implicit format: CSVFormat): CSVReader =
    open(new File(filename), encoding)(format)

}

/*
* Copyright 2013 Toshiyuki Takahashi
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

protected class CSVWriter(protected val writer: Writer)(implicit val format: CSVFormat) {

  private val printWriter: PrintWriter = new PrintWriter(writer)

  def close(): Unit = printWriter.close()

  def flush(): Unit = printWriter.flush()

  private def writeNext(fields: Seq[Any]): Unit = {

    def shouldQuote(field: String, quoting: Quoting): Boolean =
      quoting match {
        case QUOTE_ALL => true
        case QUOTE_MINIMAL => {
          List("\r", "\n", format.quoteChar.toString, format.delimiter.toString).exists(field.contains)
        }
        case QUOTE_NONE => false
        case QUOTE_NONNUMERIC => {
          if (field.forall(_.isDigit)) {
            false
          } else {
            val firstCharIsDigit = field.headOption.map(_.isDigit).getOrElse(false)
            if (firstCharIsDigit && (field.filterNot(_.isDigit) == ".")) {
              false
            } else true
          }
        }
      }

    def quote(field: String): String =
      if (shouldQuote(field, format.quoting)) field.mkString(format.quoteChar.toString, "", format.quoteChar.toString)
      else field

    def repeatQuoteChar(field: String): String =
      field.replace(format.quoteChar.toString, format.quoteChar.toString * 2)

    def escapeDelimiterChar(field: String): String =
      field.replace(format.delimiter.toString, format.escapeChar.toString + format.delimiter.toString)

    def show(s: Any): String = s.toString

    val renderField = {
      val escape = format.quoting match {
        case QUOTE_NONE => escapeDelimiterChar _
        case _ => repeatQuoteChar _
      }
      quote _ compose escape compose show
    }

    printWriter.print(fields.map(renderField).mkString(format.delimiter.toString))
    printWriter.print(format.lineTerminator)
  }

  def writeAll(allLines: Seq[Seq[Any]]): Unit = {
    allLines.foreach(line => writeNext(line))
    if (printWriter.checkError) {
      throw new java.io.IOException("Failed to write")
    }
  }

  def writeRow(fields: Seq[Any]): Unit = {
    writeNext(fields)
    if (printWriter.checkError) {
      throw new java.io.IOException("Failed to write")
    }
  }
}

protected object CSVWriter {

  def open(writer: Writer)(implicit format: CSVFormat): CSVWriter = {
    new CSVWriter(writer)(format)
  }

  def open(file: File)(implicit format: CSVFormat): CSVWriter = open(file, false, "UTF-8")(format)

  def open(file: File, encoding: String)(implicit format: CSVFormat): CSVWriter = open(file, false, encoding)(format)

  def open(file: File, append: Boolean)(implicit format: CSVFormat): CSVWriter = open(file, append, "UTF-8")(format)

  def open(file: File, append: Boolean, encoding: String)(implicit format: CSVFormat): CSVWriter = {
    val fos = new FileOutputStream(file, append)
    try {
      val writer = new OutputStreamWriter(fos, encoding)
      open(writer)(format)
    } catch {
      case e: UnsupportedEncodingException => fos.close(); throw e
    }
  }

  def open(file: String)(implicit format: CSVFormat): CSVWriter = open(file, false, "UTF-8")(format)

  def open(file: String, encoding: String)(implicit format: CSVFormat): CSVWriter = open(file, false, encoding)(format)

  def open(file: String, append: Boolean)(implicit format: CSVFormat): CSVWriter = open(file, append, "UTF-8")(format)

  def open(file: String, append: Boolean, encoding: String)(implicit format: CSVFormat): CSVWriter =
    open(new File(file), append, encoding)(format)
}

protected trait Between extends RegexParsers {

  def between[A, B, C](start: Parser[A], p: Parser[B], end: Parser[C]): Parser[B] = start ~> p <~ end

  def between[A, B](startAndEnd: Parser[A], p: Parser[B]): Parser[B] = between(startAndEnd, p, startAndEnd)
}

protected class CSVParserException(msg: String) extends Exception(msg)

protected class CSVParser(format: CSVFormat)
  extends RegexParsers
  with Between {

  override def skipWhitespace = false

  def cr: Parser[String] = "\r"

  def lf: Parser[String] = "\n"

  def crlf: Parser[String] = "\r\n"

  def eof: Parser[String] = """\z""".r

  def newLine: Parser[String] = crlf | cr | lf

  def escape: Parser[String] = format.escapeChar.toString

  def quote: Parser[String] = format.quoteChar.toString

  def delimiter: Parser[String] = format.delimiter.toString

  def emptyLine: Parser[List[String]] = (newLine | eof) ^^ {
    _ => Nil
  }

  def nonEmptyLine: Parser[List[String]] = rep1sep(field, delimiter) <~ (newLine | eof)

  def record: Parser[List[String]] = if (format.treatEmptyLineAsNil) {
    emptyLine | nonEmptyLine
  } else nonEmptyLine


  def field: Parser[String] = format.quoting match {
    case QUOTE_NONE => {
      def textData: Parser[String] = escape ~> (""".""".r | newLine) | not(delimiter) ~> """.""".r
      rep(textData) ^^ {
        _.mkString
      }
    }
    case QUOTE_ALL | QUOTE_MINIMAL | QUOTE_NONNUMERIC => {
      def textData: Parser[String] = not(delimiter | quote | newLine) ~> """.""".r
      def escapedQuote: Parser[String] = repN(2, quote) ^^ {
        _ => format.quoteChar.toString
      }
      def escaped = between(quote, rep(textData | delimiter | newLine | escapedQuote)) ^^ {
        _.mkString
      }
      def nonEscaped = rep(textData) ^^ {
        _.mkString
      }
      escaped | nonEscaped
    }
  }

  def parseLine(in: Input): ParseResult[List[String]] = {
    parse(record, in)
  }

}

protected trait CSVFormat {

  var delimiter: Char = ','
  var quoteChar: Char = '"'
  var escapeChar: Char = '\\'
  var lineTerminator: String = "\r\n"
  var quoting: Quoting = QUOTE_MINIMAL
  var treatEmptyLineAsNil: Boolean = false
}

protected sealed abstract trait Quoting

protected case object QUOTE_ALL extends Quoting

protected case object QUOTE_MINIMAL extends Quoting

protected case object QUOTE_NONE extends Quoting

protected case object QUOTE_NONNUMERIC extends Quoting

protected class MalformedCSVException(message: String) extends Exception(message)
