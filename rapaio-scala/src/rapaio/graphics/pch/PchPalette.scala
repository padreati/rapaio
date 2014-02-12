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
package rapaio.graphics.pch

import java.awt._
import scala.collection.mutable

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
object PchPalette {
  final val STANDARD = StandardPchPalette

  abstract trait Mapping {
    def draw(g2d: Graphics2D, x: Int, y: Int, size: Double, pch: Int)
  }
}

final class PchPalette(private val mapping: PchPalette.Mapping) {
  def draw(g2d: Graphics2D, x: Int, y: Int, size: Double, pch: Int) {
    mapping.draw(g2d, x, y, size, pch)
  }
}

object StandardPchPalette extends PchPalette.Mapping {

  private val pchs = Array[Drawer](
    new Drawer {
      def draw(g2d: Graphics2D, x: Int, y: Int, size: Double) {
        g2d.drawOval((x - size).asInstanceOf[Int], (y - size).asInstanceOf[Int], (size * 2 + 1).asInstanceOf[Int], (size * 2 + 1).asInstanceOf[Int])
      }
    },
    new Drawer {
      def draw(g2d: Graphics2D, x: Int, y: Int, size: Double) {
        g2d.fillOval((x - size).asInstanceOf[Int], (y - size).asInstanceOf[Int], (size * 2 + 1).asInstanceOf[Int], (size * 2 + 1).asInstanceOf[Int])
      }
    })

  def draw(g2d: Graphics2D, x: Int, y: Int, size: Double, pch: Int) {
    var _pch = pch
    if (_pch < 0) {
      _pch = 0
    }
    if (_pch >= pchs.size) {
      _pch %= pchs.size
    }
    pchs(_pch).draw(g2d, x, y, size)
  }

}

abstract trait Drawer {
  def draw(g2d: Graphics2D, x: Int, y: Int, size: Double)
}