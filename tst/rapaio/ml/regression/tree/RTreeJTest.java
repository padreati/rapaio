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
import rapaio.ml.common.VarSelector;
import rapaio.ml.eval.metric.RMSE;
import rapaio.ml.loss.L2RegressionLoss;
import rapaio.ml.regression.RegressionModel;
import rapaio.ml.regression.RegressionResult;
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
        BiFunction<RegressionModel, Integer, Boolean> myStoppingHook = (rTree, integer) -> false;
        BiConsumer<RegressionModel, Integer> myRunningHook = (rTree, integer) -> {
        };
        RTree rt1 = RTree.newCART()
                .maxDepth.set(2)
                .maxSize.set(10)
                .test.set(VType.BINARY, RTreeTest.Ignore)
                .test.set(VType.INT, RTreeTest.Ignore)
                .test.set(VType.LONG, RTreeTest.Ignore)
                .test.set(VType.DOUBLE, RTreeTest.Ignore)
                .test.set(VType.NOMINAL, RTreeTest.Ignore)
                .test.set(VType.STRING, RTreeTest.Ignore)
                .loss.set(new L2RegressionLoss())
                .splitter.set(RTreeSplitter.IGNORE)
                .varSelector.set(VarSelector.auto())
                .runs.set(10)
                .poolSize.set(10)
                .sampler.set(RowSampler.bootstrap())
                .runningHook.set(myRunningHook)
                .stoppingHook.set(myStoppingHook);
        RTree rt2 = rt1.newInstance();

        assertEquals("RTree", rt2.name());

        assertEquals(2, rt2.maxDepth.get());
        assertEquals(10, rt2.maxSize.get());
        assertEquals(rt1.test.get(), rt2.test.get());
        assertEquals("L2", rt2.loss.get().name());
        assertEquals("Ignore", rt2.splitter.get().name());
        assertEquals("VarSelector[AUTO]", rt2.varSelector.get().name());
        assertEquals(10, rt2.runs.get());
        assertEquals("Bootstrap(p=1)", rt2.sampler.get().name());
        assertEquals(10, rt2.poolSize.get());
        assertEquals(myStoppingHook, rt2.stoppingHook.get());
        assertEquals(myRunningHook, rt2.runningHook.get());

        assertNull(rt2.root());

        assertEquals("RTree{minCount=1,minScore=0,maxDepth=2,maxSize=10,tests=[bin:Ignore,int:Ignore,nom:Ignore,dbl:Ignore,long:Ignore],loss=loss,split=splitter,varSelector=varSelector,runs=10,poolSize=10,sampler=Bootstrap(p=1)}", rt2.fullName());

        Map<VType, RTreeTest> emptyMap = new HashMap<>();
        assertEquals(emptyMap, RTree.newDecisionStump().test.set(emptyMap).test.get());
    }

    @Test
    void testBuilders() {
        assertEquals("RTree{minCount=1,minScore=0,maxDepth=2,maxSize=2147483647,tests=[nom:NomBin],loss=loss,split=splitter,varSelector=varSelector,runs=1,poolSize=-1,sampler=Identity}", RTree.newDecisionStump().fullName());

        assertEquals("RTree{minCount=2,minScore=0,maxDepth=2147483647,maxSize=2147483647,tests=[],loss=loss,split=splitter,varSelector=varSelector,runs=1,poolSize=-1,sampler=Identity}", RTree.newC45().fullName());

        assertEquals("RTree{minCount=1,minScore=0,maxDepth=2147483647,maxSize=2147483647,tests=[nom:NomBin],loss=loss,split=splitter,varSelector=varSelector,runs=1,poolSize=-1,sampler=Identity}", RTree.newCART().fullName());
    }

    @Test
    void testSimple() throws IOException {
        Frame df = Datasets.loadISLAdvertising().removeVars(VRange.of("ID", "Radio", "Newspaper"));

        String v = "TV";
        Frame t = FRefSort.by(df.rvar(v).refComparator()).fapply(df);

        RTree tree = RTree.newCART()
                .maxDepth.set(10)
                .minCount.set(4);
        tree.fit(t, "Sales");

        assertEquals("\n" +
                " > RTree{minCount=4,minScore=0,maxDepth=10,maxSize=2147483647,tests=[nom:NomBin],loss=loss,split=splitter,varSelector=varSelector,runs=1,poolSize=-1,sampler=Identity}\n" +
                " model fitted: true\n" +
                "\n" +
                "description:\n" +
                "split, mean (total weight) [* if is leaf]\n" +
                "\n" +
                "|root  14.0225 (200) \n" +
                "|   |TV <= 122.05  9.7759036 (83) \n" +
                "|   |   |TV <= 30.05  6.7423077 (26) \n" +
                "|   |   |   |TV <= 8.65  4.6714286 (7)  *\n" +
                "|   |   |   |TV > 8.65  7.5052632 (19) \n" +
                "|   |   |   |   |TV <= 21.7  7.1 (12) \n" +
                "|   |   |   |   |   |TV <= 17.05  6.82 (5)  *\n" +
                "|   |   |   |   |   |TV > 17.05  7.3 (7)  *\n" +
                "|   |   |   |   |TV > 21.7  8.2 (7)  *\n" +
                "|   |   |TV > 30.05  11.1596491 (57) \n" +
                "|   |   |   |TV <= 67.35  9.7833333 (18) \n" +
                "|   |   |   |   |TV <= 49.15  10.14 (10) \n" +
                "|   |   |   |   |   |TV <= 41.25  9.92 (5)  *\n" +
                "|   |   |   |   |   |TV > 41.25  10.36 (5)  *\n" +
                "|   |   |   |   |TV > 49.15  9.3375 (8)  *\n" +
                "|   |   |   |TV > 67.35  11.7948718 (39) \n" +
                "|   |   |   |   |TV <= 108.6  11.37 (30) \n" +
                "|   |   |   |   |   |TV <= 94.05  11.647619 (21) \n" +
                "|   |   |   |   |   |   |TV <= 75.2  11.925 (8)  *\n" +
                "|   |   |   |   |   |   |TV > 75.2  11.4769231 (13) \n" +
                "|   |   |   |   |   |   |   |TV <= 77.3  10.88 (5)  *\n" +
                "|   |   |   |   |   |   |   |TV > 77.3  11.85 (8)  *\n" +
                "|   |   |   |   |   |TV > 94.05  10.7222222 (9)  *\n" +
                "|   |   |   |   |TV > 108.6  13.2111111 (9)  *\n" +
                "|   |TV > 122.05  17.0350427 (117) \n" +
                "|   |   |TV <= 240.9  16.1089888 (89) \n" +
                "|   |   |   |TV <= 181.7  14.4617647 (34) \n" +
                "|   |   |   |   |TV <= 140.8  13.8461538 (13) \n" +
                "|   |   |   |   |   |TV <= 135.7  14.4857143 (7)  *\n" +
                "|   |   |   |   |   |TV > 135.7  13.1 (6)  *\n" +
                "|   |   |   |   |TV > 140.8  14.8428571 (21) \n" +
                "|   |   |   |   |   |TV <= 167.6  15.2583333 (12) \n" +
                "|   |   |   |   |   |   |TV <= 159.95  14.5 (7)  *\n" +
                "|   |   |   |   |   |   |TV > 159.95  16.32 (5)  *\n" +
                "|   |   |   |   |   |TV > 167.6  14.2888889 (9)  *\n" +
                "|   |   |   |TV > 181.7  17.1272727 (55) \n" +
                "|   |   |   |   |TV <= 221.45  17.8472222 (36) \n" +
                "|   |   |   |   |   |TV <= 210.15  16.7363636 (22) \n" +
                "|   |   |   |   |   |   |TV <= 199.45  17.6428571 (14) \n" +
                "|   |   |   |   |   |   |   |TV <= 193.45  16.8875 (8)  *\n" +
                "|   |   |   |   |   |   |   |TV > 193.45  18.65 (6)  *\n" +
                "|   |   |   |   |   |   |TV > 199.45  15.15 (8)  *\n" +
                "|   |   |   |   |   |TV > 210.15  19.5928571 (14) \n" +
                "|   |   |   |   |   |   |TV <= 217.25  20.0375 (8)  *\n" +
                "|   |   |   |   |   |   |TV > 217.25  19 (6)  *\n" +
                "|   |   |   |   |TV > 221.45  15.7631579 (19) \n" +
                "|   |   |   |   |   |TV <= 227.6  12.6 (5)  *\n" +
                "|   |   |   |   |   |TV > 227.6  16.8928571 (14) \n" +
                "|   |   |   |   |   |   |TV <= 233.3  18.44 (5)  *\n" +
                "|   |   |   |   |   |   |TV > 233.3  16.0333333 (9)  *\n" +
                "|   |   |TV > 240.9  19.9785714 (28) \n" +
                "|   |   |   |TV <= 286.8  19.5045455 (22) \n" +
                "|   |   |   |   |TV <= 262.8  21.1444444 (9)  *\n" +
                "|   |   |   |   |TV > 262.8  18.3692308 (13) \n" +
                "|   |   |   |   |   |TV <= 276.8  16.6833333 (6)  *\n" +
                "|   |   |   |   |   |TV > 276.8  19.8142857 (7)  *\n" +
                "|   |   |   |TV > 286.8  21.7166667 (6)  *\n", tree.toSummary());
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
                .maxDepth.set(10).minCount.set(1);
        tree.fit(df, "target");
        RegressionResult result = tree.predict(df);
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
                .maxDepth.set(10)
                .minCount.set(2);
        tree.fit(train, "Sales");
        double treeRSquare = tree.predict(test, true).rSquare("Sales");

        tree = RTree.newDecisionStump();
        tree.fit(train, "Sales");

        double dsRSquare = tree.predict(test, true).rSquare("Sales");

        assertTrue(dsRSquare < treeRSquare);
    }
}
