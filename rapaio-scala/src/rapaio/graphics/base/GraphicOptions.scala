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

package rapaio.graphics.base

import rapaio.data.Index
import java.awt.Color

/**
 *
 * @author Aurelian Tutuianu <padreati@yahoo.com>
 */
class GraphicOptions {
  var col: ColorOption = GraphicOptions.DEFAULT_COLOR
  var lwd: LwdOption = GraphicOptions.DEFAULT_LWD
  var pch: PchOption = GraphicOptions.DEFAULT_PCH
  var sz: SizeOption = GraphicOptions.DEFAULT_SIZE
}

/**
 * Provides default values for graphical options.
 */
object GraphicOptions {
  private val DEFAULT_COLOR: ColorOption = Color.BLACK
  private val DEFAULT_LWD: LwdOption = 1.2
  private val DEFAULT_PCH: PchOption = 'o'
  private val DEFAULT_SIZE: SizeOption = 2.5
}

/**
 * Graphical option for colors.
 *
 * @param values an array of [[Color]] values
 */
class ColorOption(val values: Array[Color]) {

  def apply(i: Int): Color = {
    require(i >= 0, "color index must be greater than 0")
    if (i >= values.length) values(i % values.length)
    else values(i)
  }
}

object ColorOption {

  implicit def fromOneColor(c: Color): ColorOption = {
    new ColorOption(Array[Color](c))
  }

  implicit def fromOneInt(i: Int): ColorOption = {
    new ColorOption(Array[Color](StandardColorPalette.color(i)))
  }

  implicit def fromIndex(index: Index): ColorOption = {
    val values = new Array[Color](index.getRowCount)
    for (i <- 0 until index.getRowCount) {
      values(i) = StandardColorPalette.color(i)
    }
    new ColorOption(values)
  }
}

class LwdOption(val values: Array[Float]) {

  def apply(i: Int): Float = if (i >= values.length) values(i % values.length) else values(i)
}

/**
 * Provides implicit conversions for line width graphical option.
 */
object SizeOption {

  implicit def fromOneInt(value: Int): SizeOption = new SizeOption(Array[Double](value.toDouble))

  implicit def fromOneDouble(value: Double): SizeOption = new SizeOption(Array[Double](value.toDouble))
}

class SizeOption(val values: Array[Double]) {

  def apply(i: Int): Double = if (i >= values.length) values(i % values.length) else values(i)
}

/**
 * Provides implicit conversions for line width graphical option.
 */
object LwdOption {

  implicit def fromOneInt(value: Int): LwdOption = new LwdOption(Array[Float](value.toFloat))

  implicit def fromOneDouble(value: Double): LwdOption = new LwdOption(Array[Float](value.toFloat))
}

class PchOption(val index: Index = Index(0)) {

  def apply(i: Int): Int = {
    if (i >= index.getRowCount) apply(i % index.getRowCount)
    else index.getIndex(i)
  }
}

object PchOption {

  implicit def fromOneString(xs: Char): PchOption = {
    new PchOption(Index(PchPalette(xs)))
  }

  implicit def fromOneInt(xs: Int): PchOption = new PchOption(Index(xs))
}
