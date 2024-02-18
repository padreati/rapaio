/*
 *
 *  * Apache License
 *  * Version 2.0, January 2004
 *  * http://www.apache.org/licenses/
 *  *
 *  * Copyright 2013 - 2022 Aurelian Tutuianu
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package rapaio.ml.eval.metric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.Tensors;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ConfusionTest {

    private static final double TOL = 1e-20;

    @Test
    void smokeTest() {
        Var actual = VarNominal.empty(0, "cat", "dog", "mouse");
        Var predict = VarNominal.empty(0, "cat", "dog", "mouse");

        actual.addLabels(Arrays.asList("cat", "cat", "dog", "dog", "mouse", "mouse", "mouse", "cat", "cat", "mouse", "mouse"));
        predict.addLabels(Arrays.asList("cat", "dog", "dog", "mouse", "mouse", "mouse", "dog", "cat", "mouse", "mouse", "mouse"));

        Confusion cm = Confusion.from(actual, predict);

        Tensor<Double> frequency = Tensors.stride(Shape.of(3, 3),
                2, 1, 1,
                0, 1, 1,
                0, 1, 4);

        assertTrue(frequency.deepEquals(cm.frequencyMatrix()));
        assertTrue(frequency.copy().mul(1 / 11.0).deepEquals(cm.probabilityMatrix()));
    }

    @Test
    void validationTest() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Confusion.from(VarNominal.empty(), VarNominal.empty(2)));
        assertEquals("Row size does not match.", e.getMessage());

        e = assertThrows(IllegalArgumentException.class, () -> Confusion.from(VarDouble.empty(), VarNominal.empty()));
        assertEquals("Actual values variable must be nominal or binary.", e.getMessage());

        e = assertThrows(IllegalArgumentException.class, () -> Confusion.from(VarNominal.empty(), VarDouble.empty()));
        assertEquals("Predicted values variable must be nominal.", e.getMessage());

        e = assertThrows(IllegalArgumentException.class, () -> Confusion.from(VarBinary.empty(), VarNominal.empty(0, "a", "b", "c")));
        assertEquals("Actual and predict variables does not have the same nominal level size.", e.getMessage());

        e = assertThrows(IllegalArgumentException.class, () -> Confusion.from(VarBinary.empty(), VarNominal.empty(0, "a", "b")));
        assertEquals("Actual and prediction does not have same nominal levels (actual:?,true,false, predict:?,a,b).", e.getMessage());
    }

    @Test
    void binaryNominalSmokeTest() {
        Var actual = VarNominal.copy("a", "a", "b", "a", "a", "b", "b");
        Var predict = VarNominal.copy("a", "b", "b", "a", "b", "a", "b");

        Confusion cm = Confusion.from(actual, predict);

        Tensor<Double> frequency = Tensors.stride(Shape.of(2, 2), 2, 2, 1, 2);
        assertTrue(frequency.deepEquals(cm.frequencyMatrix()));
        assertTrue(frequency.copy().mul(1.0 / 7.0).deepEquals(cm.probabilityMatrix()));

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
    void binarySmokeTest() {
        Var actual = VarBinary.copy(0, 0, 1, 0, 0, 1, 1);
        Var predict = VarBinary.copy(0, 1, 1, 0, 1, 0, 1);

        Confusion cm = Confusion.from(actual, predict);

        Tensor<Double> frequency = Tensors.stride(Shape.of(2, 2),
                2, 2, 1, 2);
        assertTrue(frequency.deepEquals(cm.frequencyMatrix()));
        assertTrue(frequency.copy().mul(1.0 / 7.0).deepEquals(cm.probabilityMatrix()));

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

        assertEquals("""
                > Confusion matrix
                 - Frequency table
                Ac\\Pr |  a  b | total\s
                ----- |  -  - | -----\s
                    a | >2  2 |     4\s
                    b |  1 >2 |     3\s
                ----- |  -  - | -----\s
                total |  3  4 |     7\s
                 - Probability table
                Ac\\Pr |      a      b | total\s
                ----- |      -      - | -----\s
                    a | >0.286  0.286 | 0.571\s
                    b |  0.143 >0.286 | 0.429\s
                ----- |      -      - | -----\s
                total |  0.429  0.571 | 1.000\s


                Complete cases 7 from 7
                Acc: 0.5714286         (Accuracy )
                F1:  0.5714286         (F1 score / F-measure)
                MCC: 0.1666667         (Matthew correlation coefficient)
                Pre: 0.6666667         (Precision)
                Rec: 0.5         (Recall)
                G:   0.5773503         (G-measure)
                """, cm.toSummary());
        assertEquals(cm.toContent(), cm.toSummary());
        assertEquals(cm.toFullContent(), cm.toSummary());
        assertEquals("ConfusionMatrix(levels:?,a,b)", cm.toString());
    }
}
