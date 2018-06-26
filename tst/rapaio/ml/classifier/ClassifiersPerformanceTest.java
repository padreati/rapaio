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

package rapaio.ml.classifier;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import rapaio.core.RandomSource;
import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.VarType;
import rapaio.data.sample.RowSampler;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.boost.GBTClassifier;
import rapaio.ml.classifier.ensemble.CForest;
import rapaio.ml.classifier.tree.CTree;
import rapaio.ml.classifier.tree.CTreeTest;
import rapaio.ml.regression.tree.RTree;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * This test is not intended as a benchmark. It's sole purpose
 * is to get a smoke test for various classifiers.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ClassifiersPerformanceTest extends AbstractBenchmark {

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private Frame df_5k;
    private Frame df_50k;
    private Frame df_200k;

    public ClassifiersPerformanceTest() throws IOException {
        RandomSource.setSeed(1234);
        Frame src = Datasets.loadCoverType();
        Mapping mapping_5 = Mapping.copy(SamplingTools.sampleWR(src.rowCount(), 5_000));
        df_5k = src.mapRows(mapping_5).solidCopy();
        Mapping mapping_50 = Mapping.copy(SamplingTools.sampleWR(src.rowCount(), 50_000));
        df_50k = src.mapRows(mapping_50).solidCopy();
        Mapping mapping_200 = Mapping.copy(SamplingTools.sampleWR(src.rowCount(), 200_000));
        df_200k = src.mapRows(mapping_200).solidCopy();
    }

    @Before
    public void setUp() {
        RandomSource.setSeed(1234);
    }

//    @Test
    @BenchmarkOptions(benchmarkRounds = 7, warmupRounds = 2)
    public void performanceCartRuns12Serial5k() {
        Classifier c = CTree.newCART()
                .withMaxDepth(12)
                .withMinCount(5)
                .withSampler(RowSampler.bootstrap(1));
        test(c, df_5k);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 7, warmupRounds = 2)
    public void performanceCartRuns12Serial50k() {
        Classifier c = CTree.newCART()
                .withMaxDepth(12)
                .withMinCount(5)
                .withSampler(RowSampler.bootstrap(1));
        test(c, df_50k);
    }

//    @Test
    @BenchmarkOptions(benchmarkRounds = 7, warmupRounds = 2)
    public void performanceCartRuns12Serial200k() {
        Classifier c = CTree.newCART()
                .withMaxDepth(12)
                .withMinCount(5)
                .withSampler(RowSampler.bootstrap(1));
        test(c, df_200k);
    }

    //    @Test
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 2)
    public void performanceCartNumericBinaryRunsDepth12Serial150k() throws Exception {

        RandomSource.setSeed(1234);

        Classifier c = CTree.newCART()
                .withTest(VarType.NUMERIC, CTreeTest.NumericBinary)
                .withMaxDepth(12)
                .withSampler(RowSampler.bootstrap(1));
        test(c, df_5k);
    }

    //    @Test
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 2)
    public void performanceRFCartNumericRandomRuns200Depth12Serial5k() throws Exception {

        RandomSource.setSeed(1234);
        Classifier c = CForest.newRF()
                .withClassifier(CTree.newCART()
                        .withTest(VarType.NUMERIC, CTreeTest.NumericRandom)
                        .withMaxDepth(12)
                )
                .withRuns(200)
                .withSampler(RowSampler.bootstrap(1));
        test(c, df_5k);
    }

    //    @Test
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 2)
    public void performanceRFCartNumericBinaryRuns200Depth12Serial5k() throws Exception {

        RandomSource.setSeed(1234);

        Frame src = Datasets.loadIrisDataset();
        Classifier c = CForest.newRF()
                .withClassifier(CTree.newCART()
                        .withTest(VarType.NUMERIC, CTreeTest.NumericBinary)
                        .withMaxDepth(12)
                )
                .withRuns(200)
                .withSampler(RowSampler.bootstrap(1));
        test(c, df_5k);
    }

    //    @Test
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 2)
    public void performanceGBTCartRuns200Depth12() throws Exception {
        Frame src = Datasets.loadIrisDataset();
        Classifier c = new GBTClassifier()
                .withSampler(RowSampler.bootstrap(1))
                .withTree(RTree.buildCART().withMaxDepth(6))
                .withRuns(10);
        test(c, df_5k);
    }

    private void test(Classifier c, Frame df) {
        test(c, df, null);
    }

    private void test(Classifier c, Frame df, Long seed) {
        long next = seed == null ? RandomSource.getRandom().nextLong() : seed;
        RandomSource.setSeed(next);
        try {
            c.fit(df, "Cover_Type");
            c.predict(df, true, true);
        } catch (Throwable th) {
            System.out.println("seed: " + next);
            System.out.println("Exception: " + th.getMessage());
        }
    }
}
