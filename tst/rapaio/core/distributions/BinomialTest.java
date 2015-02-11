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
 */

package rapaio.core.distributions;

import org.junit.Assert;
import org.junit.Test;
import rcaller.RCaller;
import rcaller.RCode;

import java.util.function.BiConsumer;
import java.util.stream.IntStream;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class BinomialTest {

    private double[] compute(String cmd, int n, double p, double factor) {

        RCaller caller = new RCaller();
        RCode code = new RCode();
        double[] values = new double[n + 1];
        for (int i = 0; i < values.length; i++) {
            values[i] = i/factor;
        }
        caller.setRscriptExecutable("/usr/bin/Rscript");
        code.addDoubleArray("X", values);
        code.addRCode("ret <- " + cmd + String.format("(X, %d, prob=%.6f)", n, p));
        caller.setRCode(code);
        caller.runAndReturnResult("ret");
        return caller.getParser().getAsDoubleArray("ret");
    }

    @Test
    public void testPdf() throws Exception {
        BiConsumer<Integer, Double> f = (n, p) -> {
            double[] r = compute("dbinom", n, p, 1);
            double[] j = IntStream.rangeClosed(0, n).mapToDouble(new Binomial(p, n)::pdf).toArray();
            Assert.assertArrayEquals(r, j, 1e-14);
        };
        f.accept(10, 0.01);
        f.accept(10, 0.1);
        f.accept(10, 0.5);
        f.accept(10, 0.76);
        f.accept(10, 0.98);

        f.accept(100, 0.01);
        f.accept(100, 0.1);
        f.accept(100, 0.5);
        f.accept(100, 0.76);
        f.accept(100, 0.98);

//        f.accept(10000, 0.01);
        f.accept(1000, 0.1);
        f.accept(1000, 0.5);
        f.accept(1000, 0.76);
//        f.accept(10000, 0.98);
    }

    @Test
    public void testCdf() throws Exception {
        BiConsumer<Integer, Double> f = (n, p) -> {
            double[] r = compute("pbinom", n, p, 1);
            double[] j = IntStream.rangeClosed(0, n).mapToDouble(new Binomial(p, n)::cdf).toArray();
            Assert.assertArrayEquals(r, j, 1e-12);
        };
        f.accept(10, 0.01);
        f.accept(10, 0.1);
        f.accept(10, 0.5);
        f.accept(10, 0.76);
        f.accept(10, 0.98);

        f.accept(100, 0.01);
        f.accept(100, 0.1);
        f.accept(100, 0.5);
        f.accept(100, 0.76);
        f.accept(100, 0.98);

//        f.accept(10000, 0.01);
        f.accept(1000, 0.1);
        f.accept(1000, 0.5);
        f.accept(1000, 0.76);
//        f.accept(10000, 0.98);
    }

    @Test
    public void testQuantile() throws Exception {
        BiConsumer<Integer, Double> f = (n, p) -> {
            double[] r = compute("qbinom", n, p, n);
            double[] j = IntStream.rangeClosed(0, n).mapToDouble(i -> i / (1.0 * n)).map(new Binomial(p, n)::quantile).toArray();
            Assert.assertArrayEquals(r, j, 1e-14);
        };
        f.accept(10, 0.01);
        f.accept(10, 0.1);
        f.accept(10, 0.5);
        f.accept(10, 0.76);
        f.accept(10, 0.98);

        f.accept(100, 0.01);
        f.accept(100, 0.1);
        f.accept(100, 0.5);
        f.accept(100, 0.76);
        f.accept(100, 0.98);

//        f.accept(10000, 0.01);
        f.accept(1000, 0.1);
        f.accept(1000, 0.5);
        f.accept(1000, 0.76);
//        f.accept(10000, 0.98);
    }
}
