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

package rapaio.core.sample

import rapaio.data.Frame
import rapaio.data.mapping.{MappedFrame, Mapping}


/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
object Sampling {

  def randomSample(frame: Frame, splits: Int): List[Frame] = {
    val rowCounts = new Array[Int](splits - 1)
    for (i <- 0 until (splits - 1)) {
      rowCounts(i) = frame.rowCount / splits
    }
    randomSample(frame, rowCounts)
  }

  def randomSample(frame: Frame, rowCounts: Array[Int]): List[Frame] = {
    val total: Int = rowCounts.sum
    require(total <= frame.rowCount, "total counts greater than available number of getRowCount")

    var result: List[Frame] = Nil
    val shuffle = DiscreteSampling.sampleWOR(frame.rowCount, frame.rowCount)
    shuffle.transform(x => frame.rowId(x))
    var last = 0
    for (i <- 0 until rowCounts.length) {
      val map = Mapping(shuffle.slice(last, last + rowCounts(i)).toList)
      result = result ::: List(new MappedFrame(frame.sourceFrame, map))
      last += shuffle(i)
    }
    if (last < frame.rowCount) {
      val map = Mapping(shuffle.slice(last, frame.rowCount).toList)
      result = result ::: List(new MappedFrame(frame.sourceFrame, map))
    }
    result.reverse
  }

  def randomBootstrap(frame: Frame): Frame = randomBootstrap(frame, frame.rowCount)

  def randomBootstrap(frame: Frame, size: Int): Frame = {
    val mapping = DiscreteSampling.sampleWR(size, size)
    mapping.transform(row => frame.rowId(row))
    new MappedFrame(frame.sourceFrame, Mapping(mapping.toList))
  }
}