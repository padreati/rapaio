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


/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
object StatSampling {
  //  def randomSample(frame: Frame, splits: Int): List[Frame] = {
  //    val rowCounts: Array[Int] = new Array[Int](splits - 1)
  //    {
  //      var i: Int = 0
  //      while (i < splits - 1) {
  //        {
  //          rowCounts(i) = frame.getRowCount / splits
  //        }
  //        ({
  //          i += 1; i - 1
  //        })
  //      }
  //    }
  //    return randomSample(frame, rowCounts)
  //  }
  //
  //  def randomSample(frame: Frame, rowCounts: Array[Int]): List[Frame] = {
  //    var total: Int = 0
  //    {
  //      var i: Int = 0
  //      while (i < rowCounts.length) {
  //        {
  //          total += rowCounts(i)
  //        }
  //        ({
  //          i += 1; i - 1
  //        })
  //      }
  //    }
  //    if (total > frame.getRowCount) {
  //      throw new IllegalArgumentException("total counts greater than available number of getRowCount")
  //    }
  //    val result: List[Frame] = new ArrayList[Frame]
  //    val shuffle: Frame = shuffle(frame)
  //    val it: Nothing = shuffle.getIterator
  //    {
  //      var i: Int = 0
  //      while (i < rowCounts.length) {
  //        {
  //          {
  //            var j: Int = 0
  //            while (j < rowCounts(i)) {
  //              {
  //                it.next
  //                it.appendToMapping(i)
  //              }
  //              ({
  //                j += 1; j - 1
  //              })
  //            }
  //          }
  //          result.add(it.getMappedFrame(i))
  //        }
  //        ({
  //          i += 1; i - 1
  //        })
  //      }
  //    }
  //    while (it.next) {
  //      it.appendToMapping(rowCounts.length)
  //    }
  //    if (it.getMappingsKeys.contains(String.valueOf(rowCounts.length))) {
  //      result.add(it.getMappedFrame(rowCounts.length))
  //    }
  //    return result
  //  }
  //
  //  def randomBootstrap(frame: Frame): Frame = {
  //    return randomBootstrap(frame, frame.getRowCount)
  //  }
  //
  //  def randomBootstrap(frame: Frame, size: Int): Frame = {
  //    val mapping: List[Integer] = new ArrayList[Integer]
  //    {
  //      var i: Int = 0
  //      while (i < size) {
  //        {
  //          val next: Int = RandomSource.nextInt(frame.getRowCount)
  //          mapping.add(frame.getRowId(next))
  //        }
  //        ({
  //          i += 1; i - 1
  //        })
  //      }
  //    }
  //    return new MappedFrame(frame.getSourceFrame, new Mapping(mapping))
  //  }
}