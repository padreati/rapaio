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

package rapaio.ml.model.tree.ctree;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.datasets.Datasets;
import rapaio.ml.model.tree.CTree;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class SearchTest {

    private Frame play;

    @BeforeEach
    void beforeEach() {
    /*
     outlook  temp humidity windy class
 [0]    sunny  75     70     true   play
 [1]    sunny  80     90     true noplay
 [2]    ?      85     85    false noplay
 [3]    sunny  72     95    false noplay
 [4]    sunny  69     70    false   play
 [5] overcast  ?      90     true   play
 [6] overcast  83     78    false   play
 [7] overcast  64     65     true   play
 [8] overcast  81     ?     false   play
 [9]     rain  71     80     true noplay
[10]     rain  65     70     true noplay
[11]     rain  75     80    false   play
[12]     rain  68     80    ?       play
[13]     rain  70     96    false   play
     */
        RandomSource.setSeed(123);

        play = Datasets.loadPlay();
        play.setMissing(2, "outlook");
        play.setMissing(5, "temp");
        play.setMissing(8, "humidity");
        play.setMissing(12, "windy");
    }

    @Test
    void ignoreTest() {
        assertNull(Search.Ignore.computeCandidate(null, play, null, "temp", "class", Purity.GiniGain));
    }

    @Test
    void numericRandomTestWithNoPenalty() {

        var candidate = Search.NumericRandom.computeCandidate(
                CTree.newDecisionStump().missingPenalty.set(false),
                play, VarDouble.fill(play.rowCount(), 1.0), "temp", "class", Purity.GiniGain);

        assertEquals("temp", candidate.testName());
        assertEquals(2, candidate.groupPredicates().size());
        assertEquals("temp<=75", candidate.groupPredicates().get(0).toString());
        assertEquals("temp>75", candidate.groupPredicates().get(1).toString());
        assertEquals(0.011834319526627168, candidate.score());
    }

    @Test
    void numericRandomTestWithPenalty() {

        var candidate = Search.NumericRandom.computeCandidate(
                CTree.newDecisionStump().missingPenalty.set(true),
                play, VarDouble.fill(play.rowCount(), 1.0), "temp", "class", Purity.GiniGain);

        assertEquals("temp", candidate.testName());
        assertEquals(2, candidate.groupPredicates().size());
        assertEquals("temp<=75", candidate.groupPredicates().get(0).toString());
        assertEquals("temp>75", candidate.groupPredicates().get(1).toString());
        assertEquals(13 * 0.011834319526627168 / 14, candidate.score());
    }

    @Test
    void binaryBinaryTestNoPenalty() {
        var df = SolidFrame.byVars(
                VarBinary.copy(0, 0, 0, 1, 1, -1).name("x"),
                VarNominal.copy("a", "a", "b", "b", "b", "b").name("y")
        );

        var candidate = Search.Binary.computeCandidate(
                CTree.newDecisionStump().missingPenalty.set(false),
                df, null, "x", "y", Purity.GiniGain);

        assertEquals("x", candidate.testName());
        assertEquals(2, candidate.groupPredicates().size());
        assertEquals("x=1", candidate.groupPredicates().get(0).toString());
        assertEquals("x=0", candidate.groupPredicates().get(1).toString());
        assertEquals(0.21333333333333326, candidate.score());
    }

    @Test
    void binaryBinaryTestWithPenalty() {
        var df = SolidFrame.byVars(
                VarBinary.copy(0, 0, 0, 1, 1, -1).name("x"),
                VarNominal.copy("a", "a", "b", "b", "b", "b").name("y")
        );

        var candidate = Search.Binary.computeCandidate(
                CTree.newDecisionStump().missingPenalty.set(true),
                df, VarDouble.fill(6, 1), "x", "y", Purity.GiniGain);

        assertEquals("x", candidate.testName());
        assertEquals(2, candidate.groupPredicates().size());
        assertEquals("x=1", candidate.groupPredicates().get(0).toString());
        assertEquals("x=0", candidate.groupPredicates().get(1).toString());
        assertEquals(5 * 0.21333333333333326 / 6, candidate.score());
    }

    @Test
    void numericBinaryTest() {
        var candidate = Search.NumericBinary.computeCandidate(
                CTree.newDecisionStump().minCount.set(2),
                play, VarDouble.fill(play.rowCount(), 1.0), "temp", "class", Purity.GiniGain);

        assertEquals("temp", candidate.testName());
        assertEquals("temp<=70.5", candidate.groupPredicates().get(0).toString());
    }

    @Test
    void nominalFullTest() {
        var candidate = Search.NominalFull.computeCandidate(CTree.newDecisionStump().minCount.set(1),
                play, VarDouble.fill(play.rowCount(), 1), "outlook", "class", Purity.GiniGain);

        assertEquals("outlook", candidate.testName());
        assertEquals("outlook='sunny'", candidate.groupPredicates().get(0).toString());
        assertEquals("outlook='overcast'", candidate.groupPredicates().get(1).toString());
        assertEquals("outlook='rain'", candidate.groupPredicates().get(2).toString());
    }

    @Test
    void nominalBinary() {

        var candidate = Search.NominalBinary.computeCandidate(CTree.newDecisionStump(),
                play, VarDouble.fill(play.rowCount(), 1), "outlook", "class", Purity.GiniGain);

        assertEquals("outlook", candidate.testName());
        assertEquals(2, candidate.groupPredicates().size());
        assertEquals("outlook in ['overcast']", candidate.groupPredicates().get(0).toString());
        assertEquals("outlook not in ['overcast']", candidate.groupPredicates().get(1).toString());

        var test = VarNominal.copy("a", "a", "a", "b", "b", "c", "c", "c").name("test");
        var target = VarNominal.copy("1", "1", "1", "0", "0", "1", "1", "0").name("target");

        candidate = Search.NominalBinary.computeCandidate(CTree.newDecisionStump(),
                SolidFrame.byVars(test, target), VarDouble.fill(8, 1), "test", "target", Purity.GiniGain
        );

        assertEquals("test", candidate.testName());
        assertEquals(0.26041666666666674, candidate.score());
        assertEquals(2, candidate.groupPredicates().size());
        assertEquals("test in ['a','c']", candidate.groupPredicates().get(0).toString());
        assertEquals("test not in ['a','c']", candidate.groupPredicates().get(1).toString());


        test = VarNominal.copy("a", "a", "a", "b", "b", "c", "c", "c").name("test");
        target = VarNominal.copy("1", "1", "1", "0", "0", "2", "2", "0").name("target");

        candidate = Search.NominalBinary.computeCandidate(CTree.newDecisionStump(),
                SolidFrame.byVars(test, target), VarDouble.fill(8, 1), "test", "target", Purity.GiniGain
        );

        assertEquals("test", candidate.testName());
        assertEquals(0.35625, candidate.score());
        assertEquals(2, candidate.groupPredicates().size());
        assertEquals("test='a'", candidate.groupPredicates().get(0).toString());
        assertEquals("test!='a'", candidate.groupPredicates().get(1).toString());

        test = VarNominal.copy("a", "a", "a", "b", "b", "b", "b", "c", "c", "c").name("test");
        target = VarNominal.copy("1", "1", "1", "0", "0", "0", "0", "2", "2", "0").name("target");

        candidate = Search.NominalBinary.computeCandidate(CTree.newDecisionStump().minCount.set(5),
                SolidFrame.byVars(test, target), VarDouble.fill(10, 1), "test", "target", Purity.GiniGain);
        assertNull(candidate);
    }
}
