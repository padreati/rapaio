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

/**
 * @author tutuianu
 */
class Range(private var x1: Double, private var y1: Double, private var x2: Double, private var y2: Double) {

  def this() {
    this(Double.NaN, Double.NaN, Double.NaN, Double.NaN)
  }

  def union(range: Range) {
    union(range.getX1, range.getY1)
    union(range.getX2, range.getY2)
  }

  def union(x: Double, y: Double) {
    if (x == x) {
      x1 = math.min(if (x1 != x1) x else x1, x)
      x2 = math.max(if (x2 != x2) x else x2, x)
    }
    if (y == y) {
      y1 = math.min(if (y1 != y1) y else y1, y)
      y2 = math.max(if (y2 != y2) y else y2, y)
    }
  }

  def contains(x: Double, y: Double): Boolean = x1 <= x && x <= x2 && y1 <= y && y <= y2

  def getWidth: Double = x2 - x1

  def getHeight: Double = y2 - y1

  def getX1: Double = x1

  def getY1: Double = y1

  def getX2: Double = x2

  def getY2: Double = y2

  def setX1(x1: Double) {
    this.x1 = x1
  }

  def setY1(y1: Double) {
    this.y1 = y1
  }

  def setX2(x2: Double) {
    this.x2 = x2
  }

  def setY2(y2: Double) {
    this.y2 = y2
  }

  def getProperDecimalsX: Int = {
    var decimals: Int = 0
    var max: Double = math.abs(x2 - x1)
    while (max <= 10.) {
      max *= 10
      decimals += 1
      if (decimals > 7) {
        decimals
      }
    }
    decimals
  }

  def getProperDecimalsY: Int = {
    var decimals: Int = 0
    var max: Double = math.abs(y2 - y1)
    while (max <= 10.) {
      max *= 10
      decimals += 1
      if (decimals > 7) {
        decimals
      }
    }
    decimals
  }

}