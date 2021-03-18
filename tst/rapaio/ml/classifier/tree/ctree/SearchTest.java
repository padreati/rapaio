/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.ml.classifier.tree.ctree;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.tree.CTree;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class SearchTest {

    private final Frame play = Datasets.loadPlay();

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void ignoreTest() {
        assertNull(Search.Ignore.computeCandidate(null, play, null, "temp", "class", Purity.GiniGain));
    }

    @Test
    void numericRandomTest() {

        var candidate = Search.NumericRandom.computeCandidate(
                null, play, VarDouble.fill(play.rowCount(), 1.0), "temp", "class", Purity.GiniGain);

        assertEquals("temp", candidate.testName);
        assertEquals("temp<=75", candidate.groupPredicates.get(0).toString());
    }

    @Test
    void numericBinaryTest() {

        var candidate = Search.NumericBinary.computeCandidate(
                CTree.newDecisionStump().minCount.set(2),
                play, VarDouble.fill(play.rowCount(), 1.0), "temp", "class", Purity.GiniGain);

        assertEquals("temp", candidate.testName);
        assertEquals("temp<=70.5", candidate.groupPredicates.get(0).toString());
    }

    @Test
    void binaryBinaryTest() {
        var df = SolidFrame.byVars(
                VarBinary.copy(0, 0, 0, 1, 1, 1).name("x"),
                VarNominal.copy("a", "a", "b", "b", "b", "b").name("y")
        );

        var candidate = Search.BinaryBinary.computeCandidate(CTree.newDecisionStump().minCount.set(1),
                df, null, "x", "y", Purity.GiniGain);

        assertEquals("x", candidate.testName);
        assertEquals("x=1", candidate.groupPredicates.get(0).toString());
    }

    @Test
    void nominalFullTest() {
        var candidate = Search.NominalFull.computeCandidate(CTree.newDecisionStump().minCount.set(1),
                play, VarDouble.fill(play.rowCount(), 1), "outlook", "class", Purity.GiniGain);

        assertEquals("outlook", candidate.testName);
        assertEquals("outlook='sunny'", candidate.groupPredicates.get(0).toString());
        assertEquals("outlook='overcast'", candidate.groupPredicates.get(1).toString());
        assertEquals("outlook='rain'", candidate.groupPredicates.get(2).toString());
    }

    @Test
    void nominalBinaryTest() {
//        play.mapVars("outlook,class").refSort("outlook").printFullContent();

        var candidate = Search.NominalBinary.computeCandidate(CTree.newDecisionStump().minCount.set(1),
                play, VarDouble.fill(play.rowCount(), 1), "outlook", "class", Purity.GiniGain);

        assertEquals("outlook", candidate.testName);
        assertEquals("outlook='rain'", candidate.groupPredicates.get(0).toString());
        assertEquals("outlook!='rain'", candidate.groupPredicates.get(1).toString());
    }
}
