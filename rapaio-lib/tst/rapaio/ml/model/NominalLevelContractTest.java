/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.ml.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import rapaio.core.tools.DensityTable;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.datasets.Datasets;
import rapaio.ml.analysis.LDA;
import rapaio.ml.eval.metric.ROC;
import rapaio.ml.model.boost.AdaBoost;
import rapaio.ml.model.rule.OneRule;
import rapaio.ml.model.tree.CTree;

/**
 * Pins the nominal level contract introduced when the missing label was removed from index 0 of
 * {@code VarNominal.levels()}: levels contain only real classes, missing is index -1, and every model
 * must treat level index 0 as an ordinary class. Each test here failed on code that still assumed the
 * old layout.
 */
public class NominalLevelContractTest {

    @Test
    void levelsDoNotContainTheMissingLabel() {
        VarNominal v = VarNominal.copy("a", "b", "?", "c");
        assertEquals(List.of("a", "b", "c"), v.levels());
        assertEquals(List.of("?", "a", "b", "c"), v.levels(true));
        assertTrue(v.isMissing(2));
        assertEquals(-1, v.getInt(2));
        assertEquals(0, v.getInt(0));
    }

    @Test
    void densityTableMinimumCountIncludesFirstClass() {
        // rows: test levels x, y; cols: target classes a, b; all weight sits in the first class column
        VarNominal test = VarNominal.copy("x", "x", "x", "y", "y", "y");
        VarNominal target = VarNominal.copy("a", "a", "a", "a", "a", "a");
        DensityTable<String, String> dt = DensityTable.fromLabels(false, test, target, null);
        assertTrue(dt.hasColsWithMinimumCount(3, 2));
        assertFalse(dt.hasColsWithMinimumCount(4, 2));
    }

    @Test
    void ctreeFallbackDensityIncludesFirstClass() {
        // a row whose test value is missing at prediction time takes the weighted fallback path in predictPoint
        Frame train = SolidFrame.byVars(
                VarDouble.copy(1, 2, 3, 4, 11, 12, 13, 14).name("x"),
                VarNominal.copy("a", "a", "a", "a", "b", "b", "b", "b").name("y"));
        CTree tree = CTree.newDecisionStump().minCount.set(1).fit(train, "y");
        Frame test = SolidFrame.byVars(VarDouble.copy(Double.NaN).name("x"), VarNominal.copy("a").name("y"));
        var result = tree.predict(test, true, true);
        double pa = result.firstDensity().getDouble(0, "a");
        double pb = result.firstDensity().getDouble(0, "b");
        assertEquals(1.0, pa + pb, 1e-12);
        assertEquals(0.5, pa, 1e-12);
    }

    @Test
    void oneRuleWritesFirstClassDensityAndHandlesUnsortedRows() {
        Frame iris = Datasets.loadIrisDataset();
        // shuffle rows so that sorted position and row index differ
        Frame shuffled = iris.mapRows(rapaio.core.SamplingTools.sampleWOR(new Random(7), iris.rowCount(), iris.rowCount()));
        OneRule sorted = OneRule.newModel().fit(iris, "class");
        OneRule unsorted = OneRule.newModel().fit(shuffled, "class");
        assertEquals(sorted.toString(), unsorted.toString());

        var result = sorted.predict(iris, true, true);
        // setosa is level 0; its density must be written for setosa rows
        for (int i = 0; i < 50; i++) {
            assertEquals(1.0, result.firstDensity().getDouble(i, "setosa"), 1e-12, "row " + i);
        }
    }

    @Test
    void adaBoostTrainsOnBinaryTarget() {
        Frame df = SolidFrame.byVars(
                VarDouble.from(200, row -> (double) row).name("x"),
                VarNominal.from(200, row -> row < 100 ? "a" : "b").name("y"));
        AdaBoost ab = AdaBoost.newModel().runs.set(3).seed.set(1L).fit(df, "y");
        var result = ab.predict(df, true, true);
        for (int i = 0; i < df.rowCount(); i++) {
            double pa = result.firstDensity().getDouble(i, "a");
            double pb = result.firstDensity().getDouble(i, "b");
            assertTrue(Double.isFinite(pa) && Double.isFinite(pb), "row " + i);
            assertEquals(1.0, pa + pb, 1e-9, "row " + i);
        }
        assertEquals("a", result.firstClasses().getLabel(0));
        assertEquals("b", result.firstClasses().getLabel(199));
    }

    @Test
    void ldaKeepsAllClasses() {
        Frame iris = Datasets.loadIrisDataset();
        LDA lda = LDA.newModel();
        lda.fit(iris, "class");
        // with setosa dropped the projection of setosa rows used to fall onto the other two classes' means;
        // with all three classes the first discriminant separates setosa from the rest completely
        var projected = lda.transform(iris.removeVars("class"), 1);
        double minSetosa = Double.POSITIVE_INFINITY, maxSetosa = Double.NEGATIVE_INFINITY;
        double minOthers = Double.POSITIVE_INFINITY, maxOthers = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < iris.rowCount(); i++) {
            double v = projected.getDouble(i, 0);
            if (iris.getLabel(i, "class").equals("setosa")) {
                minSetosa = Math.min(minSetosa, v);
                maxSetosa = Math.max(maxSetosa, v);
            } else {
                minOthers = Math.min(minOthers, v);
                maxOthers = Math.max(maxOthers, v);
            }
        }
        // the two ranges must not overlap (sign of the axis is arbitrary)
        assertTrue(maxSetosa < minOthers || maxOthers < minSetosa,
                "setosa range [" + minSetosa + "," + maxSetosa + "] overlaps others [" + minOthers + "," + maxOthers + "]");
    }

    @Test
    void rocIndexIsZeroBasedOverLevels() {
        Var score = VarDouble.copy(0.9, 0.8, 0.2, 0.1);
        Var actual = VarNominal.copy("pos", "pos", "neg", "neg");
        ROC byLabel = ROC.from(score, actual, "pos");
        ROC byIndex = ROC.from(score, actual, 0);
        assertEquals(byLabel.auc(), byIndex.auc(), 1e-12);
        assertEquals(1.0, byIndex.auc(), 1e-12);
    }
}
