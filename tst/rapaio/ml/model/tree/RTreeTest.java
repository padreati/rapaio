/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.model.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.data.sample.RowSampler;
import rapaio.data.preprocessing.RefSort;
import rapaio.datasets.Datasets;
import rapaio.ml.common.VarSelector;
import rapaio.ml.eval.metric.RMSE;
import rapaio.ml.eval.metric.RegressionScore;
import rapaio.ml.loss.L2Loss;
import rapaio.ml.model.RegressionResult;
import rapaio.ml.model.RunInfo;
import rapaio.ml.model.tree.rtree.Search;
import rapaio.ml.model.tree.rtree.Splitter;

/**
 * Test for regression decision trees
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/5/15.
 */
public class RTreeTest {

    private static final double TOL = 1e-20;

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(12434);
    }

    @Test
    void testNewInstance() {
        Function<RunInfo<RTree>, Boolean> myStoppingHook = info -> false;
        Consumer<RunInfo<RTree>> myRunningHook = info -> {};
        RTree rt1 = RTree.newCART()
                .maxDepth.set(2)
                .maxSize.set(10)
                .test.add(VarType.BINARY, Search.Ignore)
                .test.add(VarType.INT, Search.Ignore)
                .test.add(VarType.LONG, Search.Ignore)
                .test.add(VarType.DOUBLE, Search.Ignore)
                .test.add(VarType.NOMINAL, Search.Ignore)
                .test.add(VarType.STRING, Search.Ignore)
                .loss.set(new L2Loss())
                .splitter.set(Splitter.Ignore)
                .varSelector.set(VarSelector.auto())
                .runs.set(10)
                .poolSize.set(10)
                .rowSampler.set(RowSampler.bootstrap())
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
        assertEquals("Bootstrap(p=1)", rt2.rowSampler.get().name());
        assertEquals(10, rt2.poolSize.get());
        assertEquals(myStoppingHook, rt2.stoppingHook.get());
        assertEquals(myRunningHook, rt2.runningHook.get());

        assertNull(rt2.root());

        assertEquals("RTree{maxDepth=2,maxSize=10,poolSize=10,rowSampler=Bootstrap(p=1)," +
                "runningHook=Consumer(),runs=10,stopHook=Function()," +
                "testMap={BINARY=Ignore,INT=Ignore,NOMINAL=Ignore,DOUBLE=Ignore,LONG=Ignore,STRING=Ignore}," +
                "varSelector=VarSelector[AUTO]}", rt2.fullName());

        Map<VarType, Search> emptyMap = new HashMap<>();
        assertEquals(emptyMap, RTree.newDecisionStump().test.set(emptyMap).test.get());
    }

    @Test
    void testBuilders() {
        assertEquals("RTree{maxDepth=2,splitter=Majority,testMap={BINARY=NumericBinary,INT=NumericBinary,NOMINAL=NominalBinary," +
                "DOUBLE=NumericBinary,LONG=NumericBinary,STRING=Ignore}}", RTree.newDecisionStump().fullName());

        assertEquals("RTree{minCount=2,splitter=Random}", RTree.newC45().fullName());

        assertEquals("RTree{splitter=Random,testMap={BINARY=NumericBinary,INT=NumericBinary,NOMINAL=NominalBinary," +
                "DOUBLE=NumericBinary,LONG=NumericBinary,STRING=Ignore}}", RTree.newCART().fullName());
    }

    @Test
    void testSimple() {
        Frame df = Datasets.loadISLAdvertising().removeVars(VarRange.of("ID", "Radio", "Newspaper"));

        String v = "TV";
        Frame t = RefSort.by(df.rvar(v).refComparator()).fapply(df);

        RTree tree = RTree.newCART()
                .maxDepth.set(10)
                .minCount.set(4);
        tree.fit(t, "Sales");

        assertEquals("""

                 > RTree{maxDepth=10,minCount=4,splitter=Random,testMap={BINARY=NumericBinary,INT=NumericBinary,NOMINAL=NominalBinary,DOUBLE=NumericBinary,LONG=NumericBinary,STRING=Ignore}}
                 model fitted: true

                description:
                split, mean (total weight) [* if is leaf]

                |root  14.0225 (200)\s
                |   |TV<=122.05  9.7759036 (83)\s
                |   |   |TV<=30.05  6.7423077 (26)\s
                |   |   |   |TV<=8.65  4.6714286 (7)  *
                |   |   |   |TV>8.65  7.5052632 (19)\s
                |   |   |   |   |TV<=21.7  7.1 (12)\s
                |   |   |   |   |   |TV<=17.05  6.82 (5)  *
                |   |   |   |   |   |TV>17.05  7.3 (7)  *
                |   |   |   |   |TV>21.7  8.2 (7)  *
                |   |   |TV>30.05  11.1596491 (57)\s
                |   |   |   |TV<=67.35  9.7833333 (18)\s
                |   |   |   |   |TV<=49.15  10.14 (10)\s
                |   |   |   |   |   |TV<=41.25  9.92 (5)  *
                |   |   |   |   |   |TV>41.25  10.36 (5)  *
                |   |   |   |   |TV>49.15  9.3375 (8)  *
                |   |   |   |TV>67.35  11.7948718 (39)\s
                |   |   |   |   |TV<=108.6  11.37 (30)\s
                |   |   |   |   |   |TV<=94.05  11.647619 (21)\s
                |   |   |   |   |   |   |TV<=75.2  11.925 (8)  *
                |   |   |   |   |   |   |TV>75.2  11.4769231 (13)\s
                |   |   |   |   |   |   |   |TV<=77.3  10.88 (5)  *
                |   |   |   |   |   |   |   |TV>77.3  11.85 (8)  *
                |   |   |   |   |   |TV>94.05  10.7222222 (9)  *
                |   |   |   |   |TV>108.6  13.2111111 (9)  *
                |   |TV>122.05  17.0350427 (117)\s
                |   |   |TV<=240.9  16.1089888 (89)\s
                |   |   |   |TV<=181.7  14.4617647 (34)\s
                |   |   |   |   |TV<=140.8  13.8461538 (13)\s
                |   |   |   |   |   |TV<=135.7  14.4857143 (7)  *
                |   |   |   |   |   |TV>135.7  13.1 (6)  *
                |   |   |   |   |TV>140.8  14.8428571 (21)\s
                |   |   |   |   |   |TV<=167.6  15.2583333 (12)\s
                |   |   |   |   |   |   |TV<=159.95  14.5 (7)  *
                |   |   |   |   |   |   |TV>159.95  16.32 (5)  *
                |   |   |   |   |   |TV>167.6  14.2888889 (9)  *
                |   |   |   |TV>181.7  17.1272727 (55)\s
                |   |   |   |   |TV<=221.45  17.8472222 (36)\s
                |   |   |   |   |   |TV<=210.15  16.7363636 (22)\s
                |   |   |   |   |   |   |TV<=199.45  17.6428571 (14)\s
                |   |   |   |   |   |   |   |TV<=193.45  16.8875 (8)  *
                |   |   |   |   |   |   |   |TV>193.45  18.65 (6)  *
                |   |   |   |   |   |   |TV>199.45  15.15 (8)  *
                |   |   |   |   |   |TV>210.15  19.5928571 (14)\s
                |   |   |   |   |   |   |TV<=217.25  20.0375 (8)  *
                |   |   |   |   |   |   |TV>217.25  19 (6)  *
                |   |   |   |   |TV>221.45  15.7631579 (19)\s
                |   |   |   |   |   |TV<=227.6  12.6 (5)  *
                |   |   |   |   |   |TV>227.6  16.8928571 (14)\s
                |   |   |   |   |   |   |TV<=233.3  18.44 (5)  *
                |   |   |   |   |   |   |TV>233.3  16.0333333 (9)  *
                |   |   |TV>240.9  19.9785714 (28)\s
                |   |   |   |TV<=286.8  19.5045455 (22)\s
                |   |   |   |   |TV<=262.8  21.1444444 (9)  *
                |   |   |   |   |TV>262.8  18.3692308 (13)\s
                |   |   |   |   |   |TV<=276.8  16.6833333 (6)  *
                |   |   |   |   |   |TV>276.8  19.8142857 (7)  *
                |   |   |   |TV>286.8  21.7166667 (6)  *
                """, tree.toSummary());
    }

    @Test
    void testLinearSeparableTest() {
        Var[] vars = new Var[] {
                VarDouble.empty().name("x"),
                VarNominal.empty().name("cat"),
                VarDouble.empty().name("target")};

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
        RegressionScore score = RMSE.newMetric().compute(df.rvar("target"), result);
        assertEquals(0, Math.pow(score.value(), 2), TOL);
    }

    @Test
    void testISLR() {
        Frame df = Datasets.loadISLAdvertising().removeVars(VarRange.of("ID"));

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
