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

package rapaio.sandbox;

import rapaio.core.RandomSource;
import rapaio.data.Numeric;
import rapaio.data.collect.VInstance;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class Functional {

    public static void main(String[] args) {
        new Functional().run();
    }

    public void run() {

        Numeric num = new Numeric();
        for (int i = 0; i < 10_000_000; i++) {
            num.addValue(RandomSource.nextDouble());
        }

        time(() -> {
            double cnt = num.stream().parallel().filter((VInstance vi) -> !vi.isMissing()).count();
            System.out.println(cnt);
            return "parallel";
        });

        time(() -> {
            double cnt = num.stream().filter((VInstance vi) -> !vi.isMissing()).count();
            System.out.println(cnt);
            return "non-parallel";
        });
    }

    public static void time(Supplier<String> f) {
        long start = System.currentTimeMillis();
        String obj = f.get();
        long stop = System.currentTimeMillis();
        System.out.println("time for " + obj + " took " + (stop - start) + " millis");
    }
}
