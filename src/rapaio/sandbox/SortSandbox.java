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

import rapaio.data.Numeric;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Histogram;
import rapaio.printer.LocalPrinter;
import rapaio.workspace.Workspace;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class SortSandbox {

    public static void main(String[] args) {

        Workspace.setPrinter(new LocalPrinter());
        int N = 1000000;
        final Numeric num = new Numeric(N);
        for (int i = 0; i < N; i++) {
            num.setValue(i, (i % 2 == 0) ? Double.NaN : i);
        }
        long start = System.currentTimeMillis();
        Integer[] sort = new Integer[N];
        for (int i = 0; i < N; i++) {
            sort[i] = i;
        }
        Arrays.sort(sort, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                if (num.isMissing(o1)) return num.isMissing(o2) ? 0 : -1;
                else return Double.compare(num.getValue(o1), num.getValue(o2));
            }
        });

        long stop = System.currentTimeMillis();

        Workspace.draw(new Plot().add(new Histogram(num)));


        System.out.println("millis " + (stop - start));
    }
}
