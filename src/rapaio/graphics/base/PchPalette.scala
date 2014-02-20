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

import java.awt._

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
object PchPalette {
  private val pchs = Array[Drawer](
    new Drawer('o') {
      def draw(g2d: Graphics2D, x: Int, y: Int, size: Double) {
        g2d.drawOval((x - size).toInt, (y - size).toInt, (size * 2 + 1).toInt, (size * 2 + 1).toInt)
      }
    },
    new Drawer('p') {
      def draw(g2d: Graphics2D, x: Int, y: Int, size: Double) {
        g2d.fillOval((x - size).toInt, (y - size).toInt, (size * 2 + 1).toInt, (size * 2 + 1).toInt)
      }
    },
    new Drawer('s') {
      override def draw(g2d: Graphics2D, x: Int, y: Int, size: Double): Unit = {
        g2d.fillRect((x - size).toInt, (y - size).toInt, (size * 2 + 1).toInt, (size * 2 + 1).toInt)
      }
    },
    new Drawer('+') {
      override def draw(g2d: Graphics2D, x: Int, y: Int, size: Double): Unit = {
        val sz = size.toInt
        g2d.drawLine(x, y - sz - 1, x, y + sz + 1)
        g2d.drawLine(x - sz - 1, y, x + sz + 1, y)
      }
    },
    new Drawer('x') {
      override def draw(g2d: Graphics2D, x: Int, y: Int, size: Double): Unit = {
        val sz = size.toInt
        g2d.drawLine(x - sz, y - sz, x + sz, y + sz)
        g2d.drawLine(x + sz, y - sz, x - sz, y + sz)
      }
    }
  )

  def draw(g2d: Graphics2D, x: Int, y: Int, size: Double, pch: Int) {
    if (pch < 0) draw(g2d, x, y, size, 0)
    if (pch >= pchs.size) draw(g2d, x, y, size, pch % pchs.size)
    pchs(pch).draw(g2d, x, y, size)
  }

  def apply(ch: Char): Int = {
    var result = 0
    for (i <- 0 until pchs.length) {
      if (pchs(i).pattern.equals(ch)) result = i
    }
    result
  }
}

abstract class Drawer(charPattern: Char) {
  def draw(g2d: Graphics2D, x: Int, y: Int, size: Double)

  def pattern: Char = charPattern
}