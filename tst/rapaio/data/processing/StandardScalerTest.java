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

package rapaio.data.processing;

import org.junit.Assert;
import org.junit.Test;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;

import java.io.IOException;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/21/18.
 */
public class StandardScalerTest {

    private static final double TOL = 1e-10;

    @Test
    public void allInTest() throws IOException {

        Frame df = Datasets.loadISLAdvertising().removeVars(VRange.of("ID"));

        Frame copy = df.solidCopy();

        StandardScaler standardScaler = StandardScaler.from(copy, true, true);
        standardScaler.transform(copy);

        Frame secondCopy = copy.solidCopy();

        standardScaler.reverse(copy);

        // tests

        Assert.assertFalse(df.deepEquals(secondCopy));

        for (int i = 0; i < df.rowCount(); i++) {
            for (int j = 0; j < df.varCount(); j++) {
                Assert.assertEquals(df.getDouble(i, j), copy.getDouble(i, j), TOL);
            }
        }
    }

    @Test
    public void selectiveTest() throws IOException {

        Frame df = Datasets.loadISLAdvertising().removeVars(VRange.of("ID"));

        Frame copy = df.solidCopy();

        StandardScaler standardScaler = StandardScaler.from(copy.removeVars(VRange.of("Sales")), true, true);
        standardScaler.transform(copy);

        Frame secondCopy = copy.solidCopy();

        standardScaler.reverse(copy);

        // tests

        Assert.assertFalse(df.deepEquals(secondCopy));
        Assert.assertTrue(df.mapVars("Sales").deepEquals(secondCopy.mapVars("Sales")));
        Assert.assertFalse(df.mapVars("TV").deepEquals(secondCopy.mapVars("TV")));

        for (int i = 0; i < df.rowCount(); i++) {
            for (int j = 0; j < df.varCount(); j++) {
                Assert.assertEquals(df.getDouble(i, j), copy.getDouble(i, j), TOL);
            }
        }
    }

    @Test
    public void centeringTest() throws IOException {
        Frame df = Datasets.loadISLAdvertising().removeVars(VRange.of("ID"));

        Frame copy = df.solidCopy();

        StandardScaler standardScaler = StandardScaler.from(copy, true, false);
        standardScaler.transform(copy);

        for(String varName : df.varNames()) {
            double mean = Mean.from(copy.rvar(varName)).value();
            double sd = Variance.from(copy.rvar(varName)).sdValue();

            Assert.assertEquals(0.0, mean, TOL);
            Assert.assertNotEquals(1.0, sd, TOL);
        }
    }

    @Test
    public void scalingTest() throws IOException {
        Frame df = Datasets.loadISLAdvertising().removeVars(VRange.of("ID"));

        Frame copy = df.solidCopy();

        StandardScaler standardScaler = StandardScaler.from(copy, false, true);
        standardScaler.transform(copy);

        for(String varName : df.varNames()) {
            double mean = Mean.from(copy.rvar(varName)).value();
            double sd = Variance.from(copy.rvar(varName)).sdValue();

            Assert.assertNotEquals(0.0, mean, TOL);
            Assert.assertEquals(1.0, sd, TOL);
        }
    }

    @Test
    public void missingData() {
        VarDouble x = VarDouble.from(100, row -> row%2==0 ? row : Double.NaN).withName("x");
        Frame df = SolidFrame.byVars(x);

        Frame copy = df.solidCopy();

        StandardScaler standardScaler = StandardScaler.from(df, true, true);
        standardScaler.transform(df);

        Assert.assertEquals(0.0, Mean.from(df.rvar("x")).value(), TOL);
        Assert.assertEquals(1.0, Variance.from(df.rvar("x")).sdValue(), TOL);

        standardScaler.reverse(df);

        for (int i = 0; i < df.rowCount(); i++) {
            for (int j = 0; j < df.varCount(); j++) {
                Assert.assertEquals(copy.isMissing(i, j), df.isMissing(i, j));
            }
        }
    }
}
