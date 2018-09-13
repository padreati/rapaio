/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/29/18.
 */
public class Time {

    public static void showRun(String message, Runnable task) {
        long start = System.currentTimeMillis();
        task.run();
        System.out.println(message != null ? message + ": " : "" + Duration.of(System.currentTimeMillis() - start, ChronoUnit.MILLIS).toString());
    }

    public static void showRun(Runnable task) {
        showRun(null, task);
    }

    public static <T> T showRun(String message, Supplier<T> task) {
        long start = System.currentTimeMillis();
        T t = task.get();
        long stop = System.currentTimeMillis();
        System.out.println(message != null ? message + ": " : "" + Duration.of(System.currentTimeMillis() - start, ChronoUnit.MILLIS).toString());
        return t;
    }

    public static <T> T showRun(Supplier<T> task) {
        return showRun(null, task);
    }

    public static <T> long measure(String message, Supplier<T> task) {
        long start = System.currentTimeMillis();
        T t = task.get();
        return System.currentTimeMillis()-start;
    }

    public static <T> long measure(Supplier<T> task) {
        return measure(null, task);
    }
}
