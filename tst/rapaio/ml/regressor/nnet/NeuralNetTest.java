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

package rapaio.ml.regressor.nnet;

import org.junit.Assert;
import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.ml.regressor.Regressor;
import rapaio.ml.regressor.RegressorFit;
import rapaio.ws.Summary;


/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class NeuralNetTest {

    @Test
    public void testAnd() {

        Var a = Numeric.newEmpty().withName("a");
        Var b = Numeric.newEmpty().withName("b");
        Var and = Numeric.newEmpty().withName("and");

        a.addValue(0);
        b.addValue(0);
        and.addValue(0);

        a.addValue(1.);
        b.addValue(0.);
        and.addValue(0.);

        a.addValue(0.);
        b.addValue(1.);
        and.addValue(0.);

        a.addValue(1.);
        b.addValue(1.);
        and.addValue(1.);

        Frame df = SolidFrame.newWrapOf(a, b, and);

        Regressor nn = new MultiLayerPerceptronRegressor(2, 1).withLearningRate(0.1).withRuns(100);

        for (int i = 0; i < 1000; i++) {
            nn.learn(df, "and");
        }
        RegressorFit pred = nn.predict(df);

        Summary.lines(pred.fitFrame());

        Assert.assertTrue(pred.firstFit().value(0) < .5);
        Assert.assertTrue(pred.firstFit().value(1) < .5);
        Assert.assertTrue(pred.firstFit().value(2) < .5);
        Assert.assertTrue(pred.firstFit().value(3) > .5);
    }

//    @Test
//    public void testXor() {
//
//        Var a = Numeric.newWrapOf(0, 1, 0, 1).withName("a");
//        Var b = Numeric.newWrapOf(0, 0, 1, 1).withName("b");
//        Var xor = Numeric.newWrapOf(1, 0, 0, 1).withName("xor");
//
//        Frame df = SolidFrame.newWrapOf(a, b, xor);
//
//        Regressor nn = new MultiLayerPerceptronRegressor(2, 2, 1).withLearningRate(0.1).withRuns(100);
//
//        for (int i = 0; i < 2000; i++) {
//            nn.learn(df, "xor");
//        }
//        RegressorFit rp = nn.predict(df);
//
//        Summary.printLines(rp.fitFrame());
//
//        Assert.assertTrue(rp.firstFit().value(0) > .5);
//        Assert.assertTrue(rp.firstFit().value(1) < .5);
//        Assert.assertTrue(rp.firstFit().value(2) < .5);
//        Assert.assertTrue(rp.firstFit().value(3) > .5);
//    }

    @Test
    public void testXorTwoOutputs() {

        Var a = Numeric.newEmpty().withName("a");
        Var b = Numeric.newEmpty().withName("b");
        Var xorA = Numeric.newEmpty().withName("xorA");
        Var xorB = Numeric.newEmpty().withName("xorB");

        a.addValue(0);
        b.addValue(0);
        xorA.addValue(0);
        xorB.addValue(1);

        a.addValue(1.);
        b.addValue(0.);
        xorA.addValue(1);
        xorB.addValue(0);

        a.addValue(0.);
        b.addValue(1.);
        xorA.addValue(1);
        xorB.addValue(0);

        a.addValue(1.);
        b.addValue(1.);
        xorA.addValue(0);
        xorB.addValue(1);

        Frame df = SolidFrame.newWrapOf(xorA.rowCount(), a, b, xorA, xorB);

        Regressor nn = new MultiLayerPerceptronRegressor(2, 4, 2).withLearningRate(0.1).withRuns(100);

        for (int i = 0; i < 10_000; i++) {
            nn.learn(df, "xorA,xorB");
        }
        RegressorFit rp = nn.predict(df);

        Assert.assertTrue(rp.fitFrame().var("xorA").value(0) < .5);
        Assert.assertTrue(rp.fitFrame().var("xorA").value(1) > .5);
        Assert.assertTrue(rp.fitFrame().var("xorA").value(2) > .5);
        Assert.assertTrue(rp.fitFrame().var("xorA").value(3) < .5);

        Assert.assertTrue(rp.fitFrame().var("xorB").value(0) > .5);
        Assert.assertTrue(rp.fitFrame().var("xorB").value(1) < .5);
        Assert.assertTrue(rp.fitFrame().var("xorB").value(2) < .5);
        Assert.assertTrue(rp.fitFrame().var("xorB").value(3) > .5);

        Summary.lines(rp.fitFrame());
    }

    @Test
    public void testGarciaChallenge() {

        Var a = Numeric.newWrapOf(0, 1, 0, 1).withName("a");
        Var b = Numeric.newWrapOf(0, 0, 1, 1).withName("b");
        Var xor = Numeric.newWrapOf(1, 0, 0, 1).withName("xor");

        Frame df = SolidFrame.newWrapOf(a, b, xor);

        Regressor nn = new MultiLayerPerceptronRegressor(2, 2, 1).withLearningRate(0.1).withRuns(100);

        Frame stat = SolidFrame.newMatrix(100, "time", "xor1err", "xor2err", "xor3err", "xor4err");
        for (int i = 0; i < 1; i++) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 4 * 2_000; j++) {
                nn.learn(df, "xor");
            }
            RegressorFit rp = nn.predict(df);
            long stop = System.currentTimeMillis();

            Assert.assertTrue(rp.firstFit().value(0) > .95);
            Assert.assertTrue(rp.firstFit().value(1) < .05);
            Assert.assertTrue(rp.firstFit().value(2) < .05);
            Assert.assertTrue(rp.firstFit().value(3) > .95);

            stat.setValue(i, "time", (stop - start) / 1000.);
            stat.setValue(i, "xor1err", 1. - rp.firstFit().value(0));
            stat.setValue(i, "xor2err", rp.firstFit().value(1));
            stat.setValue(i, "xor3err", rp.firstFit().value(2));
            stat.setValue(i, "xor4err", 1. - rp.firstFit().value(3));
        }

        Summary.printSummary(stat);

    }
}
