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

package rapaio.ml.regression.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.filter.FRefSort;
import rapaio.data.sample.RowSampler;
import rapaio.datasets.Datasets;
import rapaio.experiment.ml.regression.ensemble.RForest;
import rapaio.ml.common.VarSelector;
import rapaio.ml.eval.metric.RMSE;
import rapaio.ml.loss.L2RegressionLoss;
import rapaio.ml.regression.RegressionResult;
import rapaio.ml.regression.tree.rtree.RTreePredictor;
import rapaio.ml.regression.tree.rtree.RTreePurityFunction;
import rapaio.ml.regression.tree.rtree.RTreeSplitter;
import rapaio.ml.regression.tree.rtree.RTreeTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for regression decision trees
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/5/15.
 */
public class RTreeJTest {

    public static final String Sales = "Sales";
    private static final double TOL = 1e-20;

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(12434);
    }

    @Test
    void testNewInstance() {
        BiFunction<RTree, Integer, Boolean> myStoppingHook = (rTree, integer) -> false;
        BiConsumer<RTree, Integer> myRunningHook = (rTree, integer) -> {
        };
        RTree rt1 = RTree.newCART()
                .withMaxDepth(2)
                .withMaxSize(10)
                .withTest(VType.BINARY, RTreeTest.Ignore)
                .withTest(VType.INT, RTreeTest.Ignore)
                .withTest(VType.LONG, RTreeTest.Ignore)
                .withTest(VType.DOUBLE, RTreeTest.Ignore)
                .withTest(VType.NOMINAL, RTreeTest.Ignore)
                .withTest(VType.STRING, RTreeTest.Ignore)
                .withRegressionLoss(new L2RegressionLoss())
                .withPurityFunction(RTreePurityFunction.WEIGHTED_VAR_GAIN)
                .withSplitter(RTreeSplitter.REMAINS_IGNORED)
                .withPredictor(RTreePredictor.STANDARD)
                .withVarSelector(VarSelector.auto())
                .withRuns(10)
                .withSampler(RowSampler.bootstrap())
                .withPoolSize(10)
                .withStoppingHook(myStoppingHook)
                .withRunningHook(myRunningHook);
        RTree rt2 = rt1.newInstance();

        assertEquals("RTree", rt2.name());

        assertEquals(2, rt2.maxDepth());
        assertEquals(10, rt2.maxSize());
        assertEquals(rt1.testMap(), rt2.testMap());
        assertEquals("L2", rt2.regressionLoss().name());
        assertEquals("WEIGHTED_VAR_GAIN", rt2.purityFunction().name());
        assertEquals("REMAINS_IGNORED", rt2.splitter().name());
        assertEquals("STANDARD", rt2.predictor().name());
        assertEquals("VarSelector[AUTO]", rt2.varSelector().name());
        assertEquals(10, rt2.runs());
        assertEquals("Bootstrap(p=1)", rt2.sampler().name());
        assertEquals(10, rt2.poolSize());
        assertEquals(myStoppingHook, rt2.stoppingHook());
        assertEquals(myRunningHook, rt2.runningHook());

        assertNull(rt2.root());

        assertEquals("TreeClassifier {  minCount=1,\n" +
                "  minScore=0,\n" +
                "  maxDepth=2,\n" +
                "  maxSize=10,\n" +
                "  test[bin]=Ignore,\n" +
                "  test[int]=Ignore,\n" +
                "  test[nom]=Ignore,\n" +
                "  test[dbl]=Ignore,\n" +
                "  test[long]=Ignore,\n" +
                "  test[str]=Ignore,\n" +
                "  regressionLoss=L2\n" +
                "  purityFunction=WEIGHTED_VAR_GAIN,\n" +
                "  splitter=REMAINS_IGNORED,\n" +
                "  predictor=STANDARD\n" +
                "  varSelector=VarSelector[AUTO],\n" +
                "  runs=10,\n" +
                "  poolSize=10,\n" +
                "  sampler=Bootstrap(p=1),\n" +
                "}", rt2.fullName());

        Map<VType, RTreeTest> emptyMap = new HashMap<>();
        assertEquals(emptyMap, RTree.newDecisionStump().withTests(emptyMap).testMap());
    }

    @Test
    void testBuilders() {
        assertEquals("TreeClassifier {  minCount=1,\n" +
                "  minScore=0,\n" +
                "  maxDepth=2,\n" +
                "  maxSize=2147483647,\n" +
                "  test[bin]=NumericBinary,\n" +
                "  test[int]=NumericBinary,\n" +
                "  test[nom]=NominalBinary,\n" +
                "  test[dbl]=NumericBinary,\n" +
                "  test[long]=NumericBinary,\n" +
                "  test[str]=Ignore,\n" +
                "  regressionLoss=L2\n" +
                "  purityFunction=WEIGHTED_VAR_GAIN,\n" +
                "  splitter=REMAINS_TO_MAJORITY,\n" +
                "  predictor=STANDARD\n" +
                "  varSelector=VarSelector[ALL],\n" +
                "  runs=1,\n" +
                "  poolSize=-1,\n" +
                "  sampler=Identity,\n" +
                "}", RTree.newDecisionStump().fullName());

        assertEquals("TreeClassifier {  minCount=2,\n" +
                "  minScore=0,\n" +
                "  maxDepth=2147483647,\n" +
                "  maxSize=2147483647,\n" +
                "  test[bin]=NumericBinary,\n" +
                "  test[int]=NumericBinary,\n" +
                "  test[nom]=NominalFull,\n" +
                "  test[dbl]=NumericBinary,\n" +
                "  test[long]=NumericBinary,\n" +
                "  test[str]=Ignore,\n" +
                "  regressionLoss=L2\n" +
                "  purityFunction=WEIGHTED_VAR_GAIN,\n" +
                "  splitter=REMAINS_TO_RANDOM,\n" +
                "  predictor=STANDARD\n" +
                "  varSelector=VarSelector[ALL],\n" +
                "  runs=1,\n" +
                "  poolSize=-1,\n" +
                "  sampler=Identity,\n" +
                "}", RTree.newC45().fullName());

        assertEquals("TreeClassifier {  minCount=1,\n" +
                "  minScore=0,\n" +
                "  maxDepth=2147483647,\n" +
                "  maxSize=2147483647,\n" +
                "  test[bin]=NumericBinary,\n" +
                "  test[int]=NumericBinary,\n" +
                "  test[nom]=NominalBinary,\n" +
                "  test[dbl]=NumericBinary,\n" +
                "  test[long]=NumericBinary,\n" +
                "  test[str]=Ignore,\n" +
                "  regressionLoss=L2\n" +
                "  purityFunction=WEIGHTED_VAR_GAIN,\n" +
                "  splitter=REMAINS_TO_RANDOM,\n" +
                "  predictor=STANDARD\n" +
                "  varSelector=VarSelector[ALL],\n" +
                "  runs=1,\n" +
                "  poolSize=-1,\n" +
                "  sampler=Identity,\n" +
                "}", RTree.newCART().fullName());
    }

    @Test
    void testSimple() throws IOException {
        Frame df = Datasets.loadISLAdvertising().removeVars(VRange.of("ID", "Radio", "Newspaper"));

        String v = "TV";
        Frame t = FRefSort.by(df.rvar(v).refComparator()).fapply(df);

        RTree tree = RTree.newCART()
                .withMaxDepth(10)
                .withMinCount(4)
                .withPurityFunction(RTreePurityFunction.WEIGHTED_SD_GAIN);
        tree.fit(t, "Sales");

        assertEquals("\n" +
                " > TreeClassifier {  minCount=4,\n" +
                "  minScore=0,\n" +
                "  maxDepth=10,\n" +
                "  maxSize=2147483647,\n" +
                "  test[bin]=NumericBinary,\n" +
                "  test[int]=NumericBinary,\n" +
                "  test[nom]=NominalBinary,\n" +
                "  test[dbl]=NumericBinary,\n" +
                "  test[long]=NumericBinary,\n" +
                "  test[str]=Ignore,\n" +
                "  regressionLoss=L2\n" +
                "  purityFunction=WEIGHTED_SD_GAIN,\n" +
                "  splitter=REMAINS_TO_RANDOM,\n" +
                "  predictor=STANDARD\n" +
                "  varSelector=VarSelector[ALL],\n" +
                "  runs=1,\n" +
                "  poolSize=-1,\n" +
                "  sampler=Identity,\n" +
                "}\n" +
                " model fitted: true\n" +
                "\n" +
                "description:\n" +
                "split, mean (total weight) [* if is leaf]\n" +
                "\n" +
                "|root  14.0225 (200) \n" +
                "|   |TV <= 108.6  9.3581081 (74) \n" +
                "|   |   |TV <= 30.05  6.7423077 (26) \n" +
                "|   |   |   |TV <= 15.05  5.2818182 (11) \n" +
                "|   |   |   |   |TV <= 8.1  4.44 (5)  *\n" +
                "|   |   |   |   |TV > 8.1  5.9833333 (6)  *\n" +
                "|   |   |   |TV > 15.05  7.8133333 (15) \n" +
                "|   |   |   |   |TV <= 21.7  7.475 (8)  *\n" +
                "|   |   |   |   |TV > 21.7  8.2 (7)  *\n" +
                "|   |   |TV > 30.05  10.775 (48) \n" +
                "|   |   |   |TV <= 67.35  9.7833333 (18) \n" +
                "|   |   |   |   |TV <= 49.15  10.14 (10) \n" +
                "|   |   |   |   |   |TV <= 41.25  9.92 (5)  *\n" +
                "|   |   |   |   |   |TV > 41.25  10.36 (5)  *\n" +
                "|   |   |   |   |TV > 49.15  9.3375 (8)  *\n" +
                "|   |   |   |TV > 67.35  11.37 (30) \n" +
                "|   |   |   |   |TV <= 94.05  11.647619 (21) \n" +
                "|   |   |   |   |   |TV <= 77.3  11.5230769 (13) \n" +
                "|   |   |   |   |   |   |TV <= 75.2  11.925 (8)  *\n" +
                "|   |   |   |   |   |   |TV > 75.2  10.88 (5)  *\n" +
                "|   |   |   |   |   |TV > 77.3  11.85 (8)  *\n" +
                "|   |   |   |   |TV > 94.05  10.7222222 (9)  *\n" +
                "|   |TV > 108.6  16.7619048 (126) \n" +
                "|   |   |TV <= 181.7  14.2 (43) \n" +
                "|   |   |   |TV <= 140.8  13.5863636 (22) \n" +
                "|   |   |   |   |TV <= 122.05  13.2111111 (9)  *\n" +
                "|   |   |   |   |TV > 122.05  13.8461538 (13) \n" +
                "|   |   |   |   |   |TV <= 137.05  14.325 (8)  *\n" +
                "|   |   |   |   |   |TV > 137.05  13.08 (5)  *\n" +
                "|   |   |   |TV > 140.8  14.8428571 (21) \n" +
                "|   |   |   |   |TV <= 171.9  15.0333333 (15) \n" +
                "|   |   |   |   |   |TV <= 150.65  14.5 (5)  *\n" +
                "|   |   |   |   |   |TV > 150.65  15.3 (10) \n" +
                "|   |   |   |   |   |   |TV <= 165.05  15.68 (5)  *\n" +
                "|   |   |   |   |   |   |TV > 165.05  14.92 (5)  *\n" +
                "|   |   |   |   |TV > 171.9  14.3666667 (6)  *\n" +
                "|   |   |TV > 181.7  18.0891566 (83) \n" +
                "|   |   |   |TV <= 240.9  17.1272727 (55) \n" +
                "|   |   |   |   |TV <= 221.45  17.8472222 (36) \n" +
                "|   |   |   |   |   |TV <= 210.15  16.7363636 (22) \n" +
                "|   |   |   |   |   |   |TV <= 199.45  17.6428571 (14) \n" +
                "|   |   |   |   |   |   |   |TV <= 193.45  16.8875 (8)  *\n" +
                "|   |   |   |   |   |   |   |TV > 193.45  18.65 (6)  *\n" +
                "|   |   |   |   |   |   |TV > 199.45  15.15 (8)  *\n" +
                "|   |   |   |   |   |TV > 210.15  19.5928571 (14) \n" +
                "|   |   |   |   |   |   |TV <= 218.05  19.9666667 (9)  *\n" +
                "|   |   |   |   |   |   |TV > 218.05  18.92 (5)  *\n" +
                "|   |   |   |   |TV > 221.45  15.7631579 (19) \n" +
                "|   |   |   |   |   |TV <= 227.6  12.6 (5)  *\n" +
                "|   |   |   |   |   |TV > 227.6  16.8928571 (14) \n" +
                "|   |   |   |   |   |   |TV <= 233.3  18.44 (5)  *\n" +
                "|   |   |   |   |   |   |TV > 233.3  16.0333333 (9)  *\n" +
                "|   |   |   |TV > 240.9  19.9785714 (28) \n" +
                "|   |   |   |   |TV <= 262.8  21.1444444 (9)  *\n" +
                "|   |   |   |   |TV > 262.8  19.4263158 (19) \n" +
                "|   |   |   |   |   |TV <= 276.8  16.6833333 (6)  *\n" +
                "|   |   |   |   |   |TV > 276.8  20.6923077 (13) \n" +
                "|   |   |   |   |   |   |TV <= 286.8  19.8142857 (7)  *\n" +
                "|   |   |   |   |   |   |TV > 286.8  21.7166667 (6)  *\n", tree.toSummary());
    }

    @Test
    void testLinearSeparableTest() {
        Var[] vars = new Var[]{
                VarDouble.empty().withName("x"),
                VarNominal.empty().withName("cat"),
                VarDouble.empty().withName("target")};

        // first region - target 0
        for (int i = 0; i < 100; i++) {
            vars[0].addDouble(RandomSource.nextDouble());
            vars[1].addLabel("a");
            vars[2].addDouble(0);
        }

        // second region separated in x - target 1
        for (int i = 0; i < 100; i++) {
            vars[0].addDouble(RandomSource.nextDouble() + 10);
            vars[1].addLabel("a");
            vars[2].addDouble(1);
        }

        // third region separated by cat - target 2
        for (int i = 0; i < 100; i++) {
            vars[0].addDouble(RandomSource.nextDouble() * 3);
            vars[1].addLabel("b");
            vars[2].addDouble(2);
        }

        Frame df = SolidFrame.byVars(vars);

        RTree tree = RTree.newCART()
                .withPurityFunction(RTreePurityFunction.WEIGHTED_VAR_GAIN)
                .withMaxDepth(10).withMinCount(1);
        tree.fit(df, "target");
        RegressionResult<RTree> result = tree.predict(df);
        RMSE rmse = RMSE.newMetric().compute(df.rvar("target"), result);
        assertEquals(0, Math.pow(rmse.getScore().getValue(), 2), TOL);
    }

    @Test
    void testISLR() throws IOException {
        Frame df = Datasets.loadISLAdvertising().removeVars(VRange.of("ID"));

        Frame[] frames = SamplingTools.randomSampleSlices(df, 0.7, 0.3);

        Frame train = frames[0];
        Frame test = frames[1];

        RTree tree = RTree.newCART()
                .withMaxDepth(10)
                .withMinCount(2)
                .withPurityFunction(RTreePurityFunction.WEIGHTED_VAR_GAIN);
        tree.fit(train, "Sales");
        double treeRSquare = tree.predict(test, true).rSquare("Sales");

        RForest rf = RForest.newRF()
                .withRegression(tree)
                .withSampler(RowSampler.subsampler(0.8))
                .withRuns(100);

        rf.fit(train, "Sales");
        double rfRSquare = rf.predict(test, true).rSquare("Sales");

        tree = RTree.newDecisionStump();
        tree.fit(train, "Sales");

        double dsRSquare = tree.predict(test, true).rSquare("Sales");

        assertTrue(dsRSquare < treeRSquare);
        assertTrue(treeRSquare < rfRSquare);
    }
}
