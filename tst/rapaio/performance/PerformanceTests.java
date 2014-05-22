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

package rapaio.performance;

import org.junit.Test;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Numeric;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Lines;
import rapaio.printer.LocalPrinter;

import static rapaio.workspace.W.draw;
import static rapaio.workspace.W.setPrinter;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class PerformanceTests {

    @Test
    public void testNumericVector() {

        setPrinter(new LocalPrinter());

        final int TESTS = 500;
        final int LEN = 100_000;

        Numeric index = new Numeric();
        Numeric time1 = new Numeric();
        Numeric time2 = new Numeric();
        Numeric delta = new Numeric();

        for (int i = 0; i < TESTS; i++) {

            long start = System.currentTimeMillis();
            double[] values = new double[LEN];
            for (int j = 0; j < LEN; j++) {
                values[j] += j * Math.sin(j);
            }
            time1.addValue(System.currentTimeMillis() - start);

            start = System.currentTimeMillis();
            Numeric numeric = new Numeric(LEN);
            for (int j = 0; j < LEN; j++) {
                numeric.addValue(j * Math.sin(j));
            }
            time2.addValue(System.currentTimeMillis() - start);
            index.addIndex(i);
            delta.addValue(time1.getValue(i) - time2.getValue(i));
        }

//        draw(new Plot()
//                .add(new Lines(getIndex, time1).setColorIndex(1))
//                .add(new Lines(getIndex, time2).setColorIndex(2))
//                .add(new Lines(getIndex, delta).setColorIndex(3))
//        );

        draw(new Plot()
                        .add(new Lines(index, time1).setCol(1))
                        .add(new Lines(index, time2).setCol(2))
                        .setXLab("array")
                        .setYLab("NumVector")
        );

//        draw(new Plot()
//                .add(new Histogram(delta).setBins(30))
//        );

        new Mean(delta).summary();
        new Variance(delta).summary();
    }
}
