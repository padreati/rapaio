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

package rapaio.ml.refactor.nnet;

import junit.framework.Assert;
import org.junit.Test;
import rapaio.data.*;
import rapaio.ml.regressor.Regressor;
import rapaio.ws.Summary;


/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class NeuralNetTest {

    @Test
    public void testAnd() {

        Var a = Numeric.newEmpty();
        Var b = Numeric.newEmpty();
        Var and = Numeric.newEmpty();

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

        Frame df = new SolidFrame(and.rowCount(), new Var[]{and}, new String[]{"and"});
        df = Frames.addCol(df, b, "b", 0);
        df = Frames.addCol(df, a, "a", 0);

        Regressor nn = new MultiLayerPerceptronRegressor(new int[]{2, 1}, 0.1).setRounds(100);

        for (int i = 0; i < 1000; i++) {
            nn.learn(df, "and");
        }
        nn.predict(df);

        Summary.lines(nn.getAllFitValues());

        Assert.assertTrue(nn.getFitValues().value(0) < .5);
        Assert.assertTrue(nn.getFitValues().value(1) < .5);
        Assert.assertTrue(nn.getFitValues().value(2) < .5);
        Assert.assertTrue(nn.getFitValues().value(3) > .5);
    }

    @Test
    public void testXor() {

        Var a = Numeric.newEmpty();
        Var b = Numeric.newEmpty();
        Var xor = Numeric.newEmpty();

        a.addValue(0);
        b.addValue(0);
        xor.addValue(1);

        a.addValue(1.);
        b.addValue(0.);
        xor.addValue(0.);

        a.addValue(0.);
        b.addValue(1.);
        xor.addValue(0.);

        a.addValue(1.);
        b.addValue(1.);
        xor.addValue(1.);

        Frame df = new SolidFrame(xor.rowCount(), new Var[]{xor}, new String[]{"xor"});
        df = Frames.addCol(df, b, "b", 0);
        df = Frames.addCol(df, a, "a", 0);

        Regressor nn = new MultiLayerPerceptronRegressor(new int[]{2, 2, 1}, 0.1).setRounds(100);

        for (int i = 0; i < 1000; i++) {
            nn.learn(df, "xor");
        }
        nn.predict(df);

        Summary.lines(nn.getAllFitValues());

        Assert.assertTrue(nn.getFitValues().value(0) > .5);
        Assert.assertTrue(nn.getFitValues().value(1) < .5);
        Assert.assertTrue(nn.getFitValues().value(2) < .5);
        Assert.assertTrue(nn.getFitValues().value(3) > .5);
    }

    @Test
    public void testXorTwoOutputs() {

        Var a = Numeric.newEmpty();
        Var b = Numeric.newEmpty();
        Var xorA = Numeric.newEmpty();
        Var xorB = Numeric.newEmpty();

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

        Frame df = new SolidFrame(xorA.rowCount(),
                new Var[]{a, b, xorA, xorB},
                new String[]{"a", "b", "xorA", "xorB"});

        Regressor nn = new MultiLayerPerceptronRegressor(new int[]{2, 4, 2}, 0.1).setRounds(100);

        for (int i = 0; i < 10_000; i++) {
            nn.learn(df, "xorA,xorB");
        }
        nn.predict(df);

        Assert.assertTrue(nn.getAllFitValues().var("xorA").value(0) < .5);
        Assert.assertTrue(nn.getAllFitValues().var("xorA").value(1) > .5);
        Assert.assertTrue(nn.getAllFitValues().var("xorA").value(2) > .5);
        Assert.assertTrue(nn.getAllFitValues().var("xorA").value(3) < .5);

        Assert.assertTrue(nn.getAllFitValues().var("xorB").value(0) > .5);
        Assert.assertTrue(nn.getAllFitValues().var("xorB").value(1) < .5);
        Assert.assertTrue(nn.getAllFitValues().var("xorB").value(2) < .5);
        Assert.assertTrue(nn.getAllFitValues().var("xorB").value(3) > .5);

        Summary.lines(nn.getAllFitValues());
    }

    //    @Test
    public void testGarciaChallenge() {

        Var a = Numeric.newEmpty();
        Var b = Numeric.newEmpty();
        Var xor = Numeric.newEmpty();

        a.addValue(0);
        b.addValue(0);
        xor.addValue(1);

        a.addValue(1.);
        b.addValue(0.);
        xor.addValue(0.);

        a.addValue(0.);
        b.addValue(1.);
        xor.addValue(0.);

        a.addValue(1.);
        b.addValue(1.);
        xor.addValue(1.);

        Frame df = new SolidFrame(xor.rowCount(), new Var[]{xor}, new String[]{"xor"});
        df = Frames.addCol(df, b, "b", 0);
        df = Frames.addCol(df, a, "a", 0);

        Regressor nn = new MultiLayerPerceptronRegressor(new int[]{2, 2, 1}, 0.1).setRounds(100);

        Frame stat = Frames.newMatrix(100, new String[]{"time", "xor1err", "xor2err", "xor3err", "xor4err"});
        for (int i = 0; i < 100; i++) {
            long start = System.currentTimeMillis();
            for (int j = 0; j < 4 * 2_000; j++) {
                nn.learn(df, "xor");
            }
            nn.predict(df);
            long stop = System.currentTimeMillis();

            Assert.assertTrue(nn.getFitValues().value(0) > .95);
            Assert.assertTrue(nn.getFitValues().value(1) < .05);
            Assert.assertTrue(nn.getFitValues().value(2) < .05);
            Assert.assertTrue(nn.getFitValues().value(3) > .95);

            stat.setValue(i, "time", (stop - start) / 1000.);
            stat.setValue(i, "xor1err", 1. - nn.getFitValues().value(0));
            stat.setValue(i, "xor2err", nn.getFitValues().value(1));
            stat.setValue(i, "xor3err", nn.getFitValues().value(2));
            stat.setValue(i, "xor4err", 1. - nn.getFitValues().value(3));
        }

        Summary.summary(stat);

    }
}
