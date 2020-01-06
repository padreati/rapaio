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

package rapaio.ml.eval.metric;

import org.junit.jupiter.api.Test;
import rapaio.data.Var;
import rapaio.data.VarNominal;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.SolidRM;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ConfusionTest {

    private static final double TOL = 1e-20;

    @Test
    void smokeTest() {
        Var actual = VarNominal.empty(0, "cat", "dog", "mouse");
        Var predict = VarNominal.empty(0, "cat", "dog", "mouse");

        actual.addLabels(Arrays.asList("cat", "cat", "dog", "dog", "mouse",
                "mouse", "mouse", "cat", "cat", "mouse", "mouse"));

        predict.addLabels(Arrays.asList("cat", "dog", "dog", "mouse", "mouse",
                "mouse", "dog", "cat", "mouse", "mouse", "mouse"));

        Confusion cm = Confusion.from(actual, predict);

        RM frequency = SolidRM.wrap(new double[][]{
                {2, 1, 1},
                {0, 1, 1},
                {0, 1, 4}});

        assertTrue(frequency.isEqual(cm.frequencyMatrix()));
        assertTrue(frequency.copy().dot(1 / 11.0).isEqual(cm.probabilityMatrix()));
    }

    @Test
    void binarySmokeTest() {
        Var actual = VarNominal.copy("a", "a", "b", "a", "a", "b", "b");
        Var predict = VarNominal.copy("a", "b", "b", "a", "b", "a", "b");

        Confusion cm = Confusion.from(actual, predict);

        RM frequency = SolidRM.wrap(new double[][]{{2, 2}, {1, 2}});
        assertTrue(frequency.isEqual(cm.frequencyMatrix()));
        assertTrue(frequency.copy().dot(1.0 / 7.0).isEqual(cm.probabilityMatrix()));

        assertEquals(0.5714285714285714, cm.accuracy(), TOL);
        assertEquals(0.4285714285714286, cm.error(), TOL);

        assertEquals(4, cm.acceptedCases());
        assertEquals(3, cm.errorCases());
        assertEquals(7, cm.completeCases());

        // for binary case we have also meaningful
        assertEquals(2, cm.tp(), TOL);
        assertEquals(2, cm.tn(), TOL);
        assertEquals(1, cm.fp(), TOL);
        assertEquals(2, cm.fn(), TOL);
        assertEquals(0.5714285714285714, cm.f1(), TOL);
        assertEquals(0.16666666666666666, cm.mcc(), TOL);
        assertEquals(0.6666666666666666, cm.precision(), TOL);
        assertEquals(0.5, cm.recall(), TOL);
        assertEquals(0.5773502691896257, cm.gScore(), TOL);
    }

    @Test
    void testPrinting() {
        Var actual = VarNominal.copy("a", "a", "b", "a", "a", "b", "b");
        Var predict = VarNominal.copy("a", "b", "b", "a", "b", "a", "b");

        Confusion cm = Confusion.from(actual, predict);

        assertEquals("> Confusion matrix\n" +
                " - Frequency table\n" +
                "Ac\\Pr |  a  b | total \n" +
                "----- |  -  - | ----- \n" +
                "    a | >2  2 |     4 \n" +
                "    b |  1 >2 |     3 \n" +
                "----- |  -  - | ----- \n" +
                "total |  3  4 |     7 \n" +
                " - Probability table\n" +
                "Ac\\Pr |      a      b | total \n" +
                "----- |      -      - | ----- \n" +
                "    a | >0.286  0.286 | 0.571 \n" +
                "    b |  0.143 >0.286 | 0.429 \n" +
                "----- |      -      - | ----- \n" +
                "total |  0.429  0.571 | 1.000 \n" +
                "\n" +
                "\n" +
                "Complete cases 7 from 7\n" +
                "Acc: 0.5714286         (Accuracy )\n" +
                "F1:  0.5714286         (F1 score / F-measure)\n" +
                "MCC: 0.1666667         (Matthew correlation coefficient)\n" +
                "Pre: 0.6666667         (Precision)\n" +
                "Rec: 0.5         (Recall)\n" +
                "G:   0.5773503         (G-measure)\n", cm.toSummary());
        assertEquals(cm.toContent(), cm.toSummary());
        assertEquals(cm.toFullContent(), cm.toSummary());
        assertEquals("ConfusionMatrix(levels:?,a,b)", cm.toString());
    }
}
