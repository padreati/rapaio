package rapaio.ml.regression;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.sample.RowSampler;
import rapaio.datasets.Datasets;
import rapaio.ml.regression.tree.RTree;
import rapaio.ml.regression.tree.RTreeNumericMethod;

import java.io.IOException;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/30/17.
 */
public class RegressionsPerformanceTest extends AbstractBenchmark {

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private Frame df;

    @Before
    public void setUp() throws IOException {
        Frame src = Datasets.loadISLAdvertising().removeVars("ID").solidCopy();
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

//    @Test
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 2)
    public void performanceRCartDepth12Serial50k() throws Exception {

        RandomSource.setSeed(1234);

        Regression r = RTree.buildCART()
                .withNumericMethod(RTreeNumericMethod.BINARY)
                .withMaxDepth(12)
                .withSampler(RowSampler.bootstrap(1));
        test(r, null);
    }

    private void test(Regression c, Long seed) {
        long next = seed == null ? RandomSource.getRandom().nextLong() : seed;
        RandomSource.setSeed(next);
        try {
            c.train(df, "Sales");
            c.fit(df, true);
        } catch (Throwable th) {
            System.out.println("seed: " + next);
            System.out.println(th.getMessage());
        }
    }
}
