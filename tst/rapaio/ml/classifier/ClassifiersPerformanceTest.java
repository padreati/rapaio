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
import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.data.Mapping;
import rapaio.data.VarType;
import rapaio.data.filter.frame.FFShuffle;
import rapaio.data.sample.RowSampler;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.boost.AdaBoostSAMME;
import rapaio.ml.classifier.boost.GBTClassifier;
import rapaio.ml.classifier.ensemble.CForest;
import rapaio.ml.classifier.tree.CTree;
import rapaio.experiment.ml.eval.CEvaluation;
import rapaio.ml.classifier.tree.CTreeTest;
import rapaio.ml.eval.Confusion;
import rapaio.ml.regression.tree.RTree;
import rapaio.ml.regression.tree.RTreeTestFunction;
import rapaio.util.Pair;
import rapaio.util.Util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static rapaio.sys.WS.print;

/**
 * This test is not intended as a benchmark. It's sole purpose
 * is to get a smoke test for various classifiers.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ClassifiersPerformanceTest extends AbstractBenchmark {

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private Frame df;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        Frame src = Datasets.loadIrisDataset();
        for (int i = 0; i < 8; i++) {
            Frame copy = src.solidCopy();
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < copy.rowCount(); k++) {
                    copy.setValue(k, j, copy.value(k, j) + RandomSource.nextDouble());
                }
            }
            src = src.bindRows(copy);
        }
        df = src.solidCopy();
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 2)
    public void performanceCartNumericRandomRunsDepth12Serial150k() throws Exception {

        RandomSource.setSeed(1234);

        Classifier c = CTree.newCART()
                .withTest(VarType.NUMERIC, CTreeTest.NumericRandom)
                .withMaxDepth(12)
                .withSampler(RowSampler.bootstrap(1));
        test(c);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 2)
    public void performanceCartNumericBinaryRunsDepth12Serial150k() throws Exception {

        RandomSource.setSeed(1234);

        Classifier c = CTree.newCART()
                .withTest(VarType.NUMERIC, CTreeTest.NumericBinary)
                .withMaxDepth(12)
                .withSampler(RowSampler.bootstrap(1));
        test(c);
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
        test(c);
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
        test(c);
    }

    //    @Test
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 2)
    public void performanceGBTCartRuns200Depth12() throws Exception {
        Frame src = Datasets.loadIrisDataset();
        Classifier c = new GBTClassifier()
                .withSampler(RowSampler.bootstrap(1))
                .withTree(RTree.buildCART().withMaxDepth(6))
                .withRuns(10);
        test(c);
    }

    private void test(Classifier c) {
        test(c, null);
    }

    private void test(Classifier c, Long seed) {
        long next = seed == null ? RandomSource.getRandom().nextLong() : seed;
        RandomSource.setSeed(next);
        try {
            c.train(df, "class");
            c.fit(df, true, true);
        } catch (Throwable th) {
            System.out.println("seed: " + next);
            System.out.println(th.getMessage());
        }
    }
}
