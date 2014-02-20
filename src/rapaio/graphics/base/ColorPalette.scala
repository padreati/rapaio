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

import java.awt.Color
import scala.collection.mutable

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
abstract class ColorPalette {
  def color(index: Int): Color
}

object GrayColorPalette extends ColorPalette {
  def color(i: Int): Color = {
    val c = mod(i)
    new Color(c, c, c)
  }

  private def mod(i: Int): Int = i % 256
}

object StandardColorPalette extends ColorPalette {
  private val colors = new mutable.MutableList[Color]
  for (i <- 0 until 256) {
    var index: Int = i
    var r: Int = 0
    var g: Int = 0
    var b: Int = 0
    r = 2 * r + (index & 1)
    index >>= 1
    g = 2 * g + (index & 1)
    index >>= 1
    b = 2 * b + (index & 1)
    index >>= 1
    r = 2 * r + (index & 1)
    index >>= 1
    g = 2 * g + (index & 1)
    index >>= 1
    b = 2 * b + (index & 1)
    index >>= 1
    r = 2 * r + (index & 1)
    index >>= 1
    g = 2 * g + (index & 1)
    index >>= 1
    colors += new Color((r + 1) * 32 - 1, (g + 1) * 32 - 1, (b + 1) * 64 - 1)
  }
  colors(0) = Color.BLACK
  colors(1) = Color.RED
  colors(2) = Color.BLUE
  colors(3) = Color.GREEN
  colors(4) = Color.ORANGE

  def color(i: Int): Color = {
    if (i < 0) color(-i)
    else if (i >= colors.length) color(i % colors.length)
    else colors(i)
  }
}