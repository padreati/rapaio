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

package rapaio.sandbox

import rapaio.data.Value
import java.util.Arrays
import java.util.function.ToLongFunction

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
object SortingSandbox extends App {

  val N = 1000000
  val num = new Value(N)
  for (i <- 0 until N) {
    num.values(i) = if (i % 2 == 0) Double.NaN else i
  }


  val start = System.currentTimeMillis()
  val sort = (0 until N).toArray.sortWith((i, j) => {
    if (num.missing(i)) !num.missing(j)
    else num.values(i) < num.values(j)
  })


  val stop = System.currentTimeMillis()

  println("millis " + (stop - start))


}
