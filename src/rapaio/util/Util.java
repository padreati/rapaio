/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
 *
 */

package rapaio.util;

import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * General utilities class.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/13/15.
 */
public class Util {

    public static void measure(Runnable task) {
        long start = System.currentTimeMillis();
        task.run();
        long stop = System.currentTimeMillis();
        System.out.println((stop - start) / 60000 + " mins, " + (((stop - start) % 60000) / 1000) + " secs");
    }

    public static <T> T measure(Supplier<T> task) {
        long start = System.currentTimeMillis();
        T t = task.get();
        long stop = System.currentTimeMillis();
        System.out.println((stop - start) / 60000 + " mins, " + (((stop - start) % 60000) / 1000) + " secs, " + ((stop - start) % 1000) + " millis");
        return t;
    }

    public static IntStream rangeStream(int n, boolean parallel) {
        if (parallel)
            return IntStream.range(0, n).parallel();
        else
            return IntStream.range(0, n);
    }
}
