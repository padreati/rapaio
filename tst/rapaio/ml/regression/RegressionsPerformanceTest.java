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
import rapaio.data.VRange;
import rapaio.data.sample.RowSampler;
import rapaio.datasets.Datasets;
import rapaio.experiment.ml.regression.tree.RTree;
import rapaio.experiment.ml.regression.tree.rtree.RTreeNumericTest;

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
        Frame src = Datasets.loadISLAdvertising().removeVars(VRange.of("ID")).copy();
        Mapping mapping_5 = Mapping.wrap(SamplingTools.sampleWR(src.rowCount(), 5_000));
        df_5k = src.mapRows(mapping_5).copy();
        Mapping mapping_50 = Mapping.wrap(SamplingTools.sampleWR(src.rowCount(), 50_000));
        df_50k = src.mapRows(mapping_50).copy();
        Mapping mapping_200 = Mapping.wrap(SamplingTools.sampleWR(src.rowCount(), 200_000));
        df_200k = src.mapRows(mapping_200).copy();
    }

    @Before
    public void setUp() {
        RandomSource.setSeed(1234);
    }


    @Test
    @BenchmarkOptions(benchmarkRounds = 15, warmupRounds = 10)
    public void performanceRCartDepth12Serial5k() {
        Regression r = RTree.newCART()
                .withNumericTest(RTreeNumericTest.binary())
                .withMaxDepth(12)
                .withSampler(RowSampler.bootstrap(1));
        test(r, df_5k);
    }

//    @Test
    @BenchmarkOptions(benchmarkRounds = 15, warmupRounds = 10)
    public void performanceRCartDepth12Serial50k() {
        Regression r = RTree.newCART()
                .withNumericTest(RTreeNumericTest.binary())
                .withMaxDepth(12)
                .withSampler(RowSampler.bootstrap(1));
        test(r, df_50k);
    }

//    @Test
    @BenchmarkOptions(benchmarkRounds = 15, warmupRounds = 10)
    public void performanceRCartDepth12Serial200k() {
        Regression r = RTree.newCART()
                .withNumericTest(RTreeNumericTest.binary())
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
