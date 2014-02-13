package rapaio.graphics.base

import rapaio.data.Index
import java.awt.Color
import rapaio.graphics.colors.ColorPalette
import rapaio.graphics.base

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
class GraphicOptions(parentOpt: GraphicOptions = null) {

  var color: ColorOption = GraphicOptions.DEFAULT_COLOR
}

object GraphicOptions {
  private val DEFAULT_COLOR: ColorOption = Color.BLACK
}

class ColorOption(val colorArray: Array[Color]) {

  def apply(i: Int): Color = {
    require(i >= 0, "color index must be greater than 0")
    if (i >= colorArray.length) colorArray(i % colorArray.length)
    else colorArray(i)
  }
}

object ColorOption {

  implicit def fromOneColor(c: Color): ColorOption = {
    new ColorOption(Array[Color](c))
  }

  implicit def fromOneInt(i: Int): ColorOption = {
    new ColorOption(Array[Color](ColorPalette.STANDARD.getColor(i)))
  }

  implicit def fromIndex(index: Index): ColorOption = {
    val colors = new Array[Color](index.getRowCount)
    for (i <- 0 until index.getRowCount) {
      colors(i) = ColorPalette.STANDARD.getColor(i)
    }
    new ColorOption(colors)
  }
}
