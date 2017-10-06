/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.data.sample;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rapaio.core.CoreTools;
import rapaio.core.RandomSource;
import rapaio.core.tests.ChiSqGoodnessOfFit;
import rapaio.core.tools.DVector;
import rapaio.data.Frame;
import rapaio.data.NumVar;
import rapaio.datasets.Datasets;

import java.util.stream.DoubleStream;

/**
 * Test for row sampling tools
 *
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/26/16.
 */
public class RowSamplerTest {

    private Frame df;
    private NumVar w;

    @Before
    public void setUp() throws Exception {
        df = Datasets.loadIrisDataset();
        w = NumVar.from(df.rowCount(), row -> (double) df.index(row, "class")).withName("w");
        Assert.assertEquals(w.stream().mapToDouble().sum(), 50 * (1 + 2 + 3), 1e-20);
    }

    @Test
    public void identitySamplerTest() {
        Sample s = RowSampler.identity().nextSample(df, w);
        Assert.assertTrue(s.df.deepEquals(df));
        Assert.assertTrue(s.weights.deepEquals(w));
    }

    @Test
    public void bootstrapTest() {
        RandomSource.setSeed(123);

        int N = 1_000;
        NumVar count = NumVar.empty().withName("bcount");
        for (int i = 0; i < N; i++) {
            Sample s = RowSampler.bootstrap(1.0).nextSample(df, w);
            count.addValue(1.0 * s.mapping.rowStream().distinct().count() / df.rowCount());
        }

        // close to 1 - 1 / exp(1)
        Assert.assertEquals(0.63328, CoreTools.mean(count).value(), 1e-5);
    }

    @Test
    public void subsampleTest() {
        RandomSource.setSeed(123);

        int N = 1_000;
        NumVar count = NumVar.fill(df.rowCount(), 0.0).withName("sscount");
        for (int i = 0; i < N; i++) {
            Sample s = RowSampler.subsampler(0.5).nextSample(df, w);
            s.mapping.rowStream().forEach(r -> count.setValue(r, count.value(r) + 1));
        }

        // uniform counts close to 500
        count.printLines();

        DVector freq = DVector.empty(true, df.rowCount());
        for (int i = 0; i < df.rowCount(); i++) {
            freq.set(i, count.value(i));
        }
        double[] p = DoubleStream.generate(() -> 1 / 150.).limit(150).toArray();
        ChiSqGoodnessOfFit chiTest = ChiSqGoodnessOfFit.from(freq, NumVar.wrap(p));
        chiTest.printSummary();

        // chi square goodness of fit

        Assert.assertTrue(chiTest.pValue() > 0.99);
    }

    @Test
    public void nameSamplerTest() {
        Assert.assertEquals("Identity", RowSampler.identity().name());
        Assert.assertEquals("Bootstrap(p=1)", RowSampler.bootstrap().name());
        Assert.assertEquals("Bootstrap(p=0.2)", RowSampler.bootstrap(0.2).name());
        Assert.assertEquals("SubSampler(p=1)", RowSampler.subsampler(1.0).name());
        Assert.assertEquals("SubSampler(p=0.2)", RowSampler.subsampler(0.2).name());
    }
}
