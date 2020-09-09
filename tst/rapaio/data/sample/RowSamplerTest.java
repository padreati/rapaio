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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.stat.Mean;
import rapaio.core.tests.ChiSqGoodnessOfFit;
import rapaio.core.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;

import java.util.stream.DoubleStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for row sampling tools
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/26/16.
 */
public class RowSamplerTest {

    private Frame df;
    private VarDouble w;

    @BeforeEach
    public void setUp() {
        df = Datasets.loadIrisDataset();
        w = VarDouble.from(df.rowCount(), row -> (double) df.getInt(row, "class")).withName("w");
        assertEquals(w.stream().mapToDouble().sum(), 50 * (1 + 2 + 3), 1e-20);
    }

    @Test
    void identitySamplerTest() {
        RowSampler.Sample s = RowSampler.identity().nextSample(df, w);
        assertTrue(s.getDf().deepEquals(df));
        assertTrue(s.getWeights().deepEquals(w));
    }

    @Test
    void bootstrapTest() {
        RandomSource.setSeed(123);

        int N = 1_000;
        VarDouble count = VarDouble.empty().withName("bcount");
        for (int i = 0; i < N; i++) {
            RowSampler.Sample s = RowSampler.bootstrap(1.0).nextSample(df, w);
            count.addDouble(1.0 * s.getMapping().stream().distinct().count() / df.rowCount());
        }

        // close to 1 - 1 / exp(1)
        assertEquals(0.63328, Mean.of(count).value(), 1e-5);
    }

    @Test
    void subsampleTest() {
        RandomSource.setSeed(123);

        int N = 1_000;
        VarDouble count = VarDouble.fill(df.rowCount(), 0.0).withName("sscount");
        for (int i = 0; i < N; i++) {
            RowSampler.Sample s = RowSampler.subsampler(0.5).nextSample(df, w);
            s.getMapping().stream().forEach(r -> count.setDouble(r, count.getDouble(r) + 1));
        }

        // uniform counts close to 500
        count.printContent();

        var freq = DensityVector.emptyByLabels(df.rowCount());
        for (int i = 0; i < df.rowCount(); i++) {
            freq.set(i, count.getDouble(i));
        }
        double[] p = DoubleStream.generate(() -> 1 / 150.).limit(150).toArray();
        ChiSqGoodnessOfFit chiTest = ChiSqGoodnessOfFit.from(freq, VarDouble.wrap(p));
        chiTest.printSummary();

        // chi square goodness of predict

        assertTrue(chiTest.pValue() > 0.99);
    }

    @Test
    void nameSamplerTest() {
        assertEquals("Identity", RowSampler.identity().name());
        assertEquals("Bootstrap(p=1)", RowSampler.bootstrap().name());
        assertEquals("Bootstrap(p=0.2)", RowSampler.bootstrap(0.2).name());
        assertEquals("SubSampler(p=1)", RowSampler.subsampler(1.0).name());
        assertEquals("SubSampler(p=0.2)", RowSampler.subsampler(0.2).name());
    }
}
