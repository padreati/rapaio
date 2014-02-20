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
class Range(
             var x1: Double = Double.NaN,
             var y1: Double = Double.NaN,
             var x2: Double = Double.NaN,
             var y2: Double = Double.NaN) {

  def union(range: Range) {
    union(range.x1, range.y1)
    union(range.x2, range.y2)
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

  def contains(x: Double, y: Double): Boolean = {
    x1 <= x && x <= x2 && y1 <= y && y <= y2
  }

  def width: Double = x2 - x1

  def height: Double = y2 - y1

  def properDecimalsX: Int = {
    var decimals: Int = 0
    var max: Double = math.abs(x2 - x1)
    while (max <= 10.0) {
      max *= 10
      decimals += 1
      if (decimals > 7) {
        decimals
      }
    }
    decimals
  }

  def properDecimalsY: Int = {
    var decimals: Int = 0
    var max: Double = math.abs(y2 - y1)
    while (max <= 10.0) {
      max *= 10
      decimals += 1
      if (decimals > 7) {
        decimals
      }
    }
    decimals
  }

}