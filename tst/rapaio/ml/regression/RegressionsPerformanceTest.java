package rapaio.ml.regression;

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
import rapaio.data.sample.RowSampler;
import rapaio.datasets.Datasets;
import rapaio.ml.regression.tree.RTree;
import rapaio.ml.regression.tree.RTreeNumericTest;

import java.io.IOException;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/30/17.
 */
public class RegressionsPerformanceTest extends AbstractBenchmark {

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private Frame df_5k;
    private Frame df_50k;
    private Frame df_200k;

    public RegressionsPerformanceTest() throws IOException {
        RandomSource.setSeed(1234);
        Frame src = Datasets.loadISLAdvertising().removeVars("ID").solidCopy();
        Mapping mapping_5 = Mapping.wrap(SamplingTools.sampleWR(src.rowCount(), 5_000));
        df_5k = src.mapRows(mapping_5).solidCopy();
        Mapping mapping_50 = Mapping.wrap(SamplingTools.sampleWR(src.rowCount(), 50_000));
        df_50k = src.mapRows(mapping_50).solidCopy();
        Mapping mapping_200 = Mapping.wrap(SamplingTools.sampleWR(src.rowCount(), 200_000));
        df_200k = src.mapRows(mapping_200).solidCopy();
    }

    @Before
    public void setUp() {
        RandomSource.setSeed(1234);
    }


    @Test
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 2)
    public void performanceRCartDepth12Serial5k() {
        Regression r = RTree.newCART()
                .withNumericMethod(RTreeNumericTest.BINARY)
                .withMaxDepth(12)
                .withSampler(RowSampler.bootstrap(1));
        test(r, df_5k);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 2)
    public void performanceRCartDepth12Serial50k() {
        Regression r = RTree.newCART()
                .withNumericMethod(RTreeNumericTest.BINARY)
                .withMaxDepth(12)
                .withSampler(RowSampler.bootstrap(1));
        test(r, df_50k);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 2)
    public void performanceRCartDepth12Serial200k() {
        Regression r = RTree.newCART()
                .withNumericMethod(RTreeNumericTest.BINARY)
                .withMaxDepth(12)
                .withSampler(RowSampler.bootstrap(1));
        test(r, df_200k);
    }

    private void test(Regression c, Frame df) {
        long seed = RandomSource.getRandom().nextLong();
        RandomSource.setSeed(seed);
        try {
            c.fit(df, "Sales");
            c.predict(df, true);
        } catch (Throwable th) {
            th.printStackTrace();
            System.out.println("seed: " + seed);
            System.out.println(th.getMessage());
        }
    }
}
