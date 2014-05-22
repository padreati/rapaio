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
import rapaio.printer.LocalPrinter;
import rapaio.workspace.W;

import java.util.Arrays;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class SortSandbox {

    public static void main(String[] args) {

        W.setPrinter(new LocalPrinter());
        int N = (int) Math.pow(10, 7);
        final Numeric num = new Numeric(N);
        for (int i = 0; i < N; i++) {
//            num.setValue(i, (i % 2 == 0) ? Double.NaN : i);
            num.setValue(i, RandomSource.nextDouble());
        }

        long start = System.currentTimeMillis();

        Double[] sort = new Double[N];
        for (int i = 0; i < N; i++) {
            sort[i] = RandomSource.nextDouble();
        }
        System.out.println("millis " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        Arrays.sort(sort);
//        , new Comparator<Double>() {
//            @Override
//            public int compare(Double o1, Double o2) {
//                if (num.missing(o1)) return num.missing(o2) ? 0 : -1;
//                else return Double.compare(num.value(o1), num.value(o2));
//            }
//        });
        System.out.println("millis " + (System.currentTimeMillis() - start));


    }
}
