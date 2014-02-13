package rapaio.graphics.base

import rapaio.data.Index
import java.awt.Color
import rapaio.graphics.colors.ColorPalette

/**
 *
 * @author Aurelian Tutuianu <padreati@yahoo.com>
 */
class GraphicOptions(parentOpt: GraphicOptions = null) {

  var col: ColorOption = GraphicOptions.DEFAULT_COLOR

  var lwd: LwdOption = GraphicOptions.DEFAULT_LWD
}

object GraphicOptions {
  private val DEFAULT_COLOR: ColorOption = Color.BLACK
  private val DEFAULT_LWD: LwdOption = 1
}

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
    new ColorOption(Array[Color](ColorPalette.STANDARD.getColor(i)))
  }

  implicit def fromIndex(index: Index): ColorOption = {
    val values = new Array[Color](index.getRowCount)
    for (i <- 0 until index.getRowCount) {
      values(i) = ColorPalette.STANDARD.getColor(i)
    }
    new ColorOption(values)
  }
}

class LwdOption(val values: Array[Float]) {

}

object LwdOption {

  implicit def fromOneInt(value: Int): LwdOption = {
    new LwdOption(Array[Float](value.toFloat))
  }

  implicit def fromOneDouble(value: Double): LwdOption = {
    new LwdOption(Array[Float](value.toFloat))
  }
}