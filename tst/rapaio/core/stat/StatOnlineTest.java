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

package rapaio.core.stat;

import org.junit.Test;
import rapaio.WS;
import rapaio.core.distributions.Normal;
import rapaio.data.Index;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Points;
import rapaio.printer.IdeaPrinter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static rapaio.WS.draw;
import static rapaio.WS.setPrinter;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class StatOnlineTest {

    @Test
    public void testVariance() {

//        RandomSource.setSeed(1223);
        setPrinter(new IdeaPrinter());

        int LEN = 1_000;
        Var v = new Normal(0, 1).sample(LEN);

        StatOnline statOnline = new StatOnline();

        Var index = Index.newSeq(LEN);
        Var varLeft = Numeric.newFill(LEN);
        Var varRight = Numeric.newFill(LEN);
        Var varSum = Numeric.newFill(LEN);

        for (int i = 0; i < LEN; i++) {
            statOnline.update(v.value(i));
            if (i > 0) {
                varLeft.setValue(i, statOnline.variance());
            }
        }
        statOnline.clean();
        for (int i = LEN - 1; i >= 0; i--) {
            statOnline.update(v.value(i));
            if (i < LEN - 1) {
                varRight.setValue(i, statOnline.variance());
            }
        }
        for (int i = 0; i < LEN; i++) {
            varSum.setValue(i, (varLeft.value(i) + varRight.value(i)) / 2);
        }

        draw(new Plot()
                        .add(new Points(index, varLeft).color(1))
                        .add(new Points(index, varRight).color(2))
                        .add(new Points(index, varSum).color(3))
                        .yLim(0.5, 1.5)
                        .sz(0.4)
        );
    }

    @Test
    public void testParallelStat() {
        Var a = Numeric.newWrapOf(1, 2, 3, 13, 17, 30);
        Var b = Numeric.newWrapOf(44, 5, 234, 12, 33, 1);
        Var ab = a.bindRows(b);
        StatOnline soA = new StatOnline();
        StatOnline soB = new StatOnline();
        a.stream().forEach(s -> soA.update(s.value()));
        b.stream().forEach(s -> soB.update(s.value()));

        soA.apply(soB);

        WS.p(String.format("%12f", soA.variance()));
        WS.p(String.format("%12f", new Variance(ab).value()));
    }

    @Test
    public void testWeightedStat() {

        Var a = Numeric.newWrapOf(1, 1, 2, 2, 2, 3, 3, 3, 3, 4);
        StatOnline so1 = new StatOnline();

        a.stream().forEach(s -> so1.update(s.value(), 1.0));

        assertEquals(2.4, so1.mean(), 10e-12);
        assertEquals(0.9333333333333331, so1.variance(), 10e-12);
        assertEquals(-0.897959183673469, so1.kurtosis(), 10e-12);
        assertEquals(-0.0935219529582825, so1.skewness(), 10e-12);

        StatOnline so2 = new StatOnline();

        so2.update(1, 2);
        so2.update(2, 3);
        so2.update(3, 4);
        so2.update(4, 1);

        assertEquals("mean", 2.4, so2.mean(), 10e-12);
        assertEquals("variance", 0.9333333333333331, so2.variance(), 10e-12);
        assertTrue(Double.isNaN(so2.kurtosis()));
        assertTrue(Double.isNaN(so2.skewness()));

        StatOnline so3 = new StatOnline();

        so3.update(1, 1);
        so3.update(2, 1.5);
        so3.update(3, 2);
        so3.update(4, 0.5);

        assertEquals("mean", 2.4, so3.mean(), 10e-12);
//        assertEquals("variance", 0.9333333333333331, so3.variance(), 10e-12);
        assertTrue(Double.isNaN(so3.kurtosis()));
        assertTrue(Double.isNaN(so3.skewness()));

    }
}
