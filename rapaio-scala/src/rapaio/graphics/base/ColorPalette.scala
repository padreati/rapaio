package rapaio.graphics.base

import java.awt.Color
import scala.collection.mutable
import scala.annotation.tailrec

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
    if (i >= colors.length) color(i % colors.length)
    colors(i)
  }
}