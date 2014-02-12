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
package rapaio.graphics.colors

import java.awt._
import java.io.Serializable
import scala.collection.mutable

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
object ColorPalette {
  val STANDARD = StandardColorPalette
  val GRAYSCALE = GrayColorPallete

  trait Mapping extends Serializable {
    def getColor(index: Int): Color
  }

}

final class ColorPalette(palette: ColorPalette.Mapping) {

  def getColor(index: Int): Color = {
    palette.getColor(index)
  }
}

object GrayColorPallete extends ColorPalette.Mapping {
  def getColor(index: Int): Color = {
    return new Color(modIndex(index), modIndex(index), modIndex(index))
  }

  def modIndex(index: Int): Int = {index % 256}
}

object StandardColorPalette extends ColorPalette.Mapping {
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

  def getColor(index: Int): Color = {
    var idx = index
    if (idx < 0) {
      idx *= -1
    }
    if (idx >= colors.length) {
      return colors(idx % colors.length)
    }
    return colors(idx)
  }
}