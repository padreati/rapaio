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

package rapaio.data

/**
 * Base class for a vector which enforces to read-only name given at construction time.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
abstract class AbstractFeature extends Feature {
  override def toString: String = {
    "Vector{ size='" + rowCount + "\'}"
  }

  def toValueArray: Array[Double] = {
    val list = new Array[Double](rowCount)
    var i: Int = 0
    while (i < rowCount) {
      list(i) = values(i)
      i += 1
    }
    list
  }

  def toIndexArray: Array[Int] = {
    val list = new Array[Int](rowCount)
    var i: Int = 0
    while (i < rowCount) {
      list(i) = indexes(i)
      i += 1
    }
    list
  }

  def toLabelArray: Array[String] = {
    val list = new Array[String](rowCount)
    var i: Int = 0
    while (i < rowCount) {
      list(i) = labels(i)
      i += 1
    }
    list
  }

}
