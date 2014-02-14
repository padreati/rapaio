package rapaio.graphics.base

import org.scalatest.{Matchers, FlatSpec}
import java.awt.Color
import java.awt

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
class GraphicOptionsSpec extends FlatSpec with Matchers {

  "A color option " should " receive it's value from a color" in {
    val opt = new GraphicOptions
    opt.col = Color.GREEN

    for (i <- 0 until 100) {
      opt.col(i) should be(Color.GREEN)
    }
  }

  "The default value for color option " should " be black for all index values" in {
    val opt = new GraphicOptions
    for(i<-0 until 100) opt.col(i) should be(Color.BLACK)
  }
}
