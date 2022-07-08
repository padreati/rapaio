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

package rapaio.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.data.preprocessing.AddIntercept;
import rapaio.data.preprocessing.ApplyTransform;
import rapaio.datasets.Datasets;

public class PreprocessingTest {

    private static final double TOL = 1e-12;

    @Test
    void testBuilder() {
        var transform = Preprocessing.newProcess()
                .add(AddIntercept.transform())
                .add(AddIntercept.transform());
        assertEquals(2, transform.transformers().size());
        assertTrue(transform.transformers().get(0) instanceof AddIntercept);
        assertTrue(transform.transformers().get(0) instanceof AddIntercept);
    }

    @Test
    void testEmptyTransformation() {

        var transform = Preprocessing.newProcess();
        var df = SolidFrame.byVars(VarDouble.empty(10).name("x"), VarNominal.empty(10, "a", "b").name("c"));
        var dft = transform.fapply(df);

        // equal on instance
        assertSame(df, dft);
        assertEquals(df, dft);
        assertTrue(df.deepEquals(dft));
    }

    @Test
    void testSequence() {
        var iris = Datasets.loadIrisDataset();
        var transform = Preprocessing.newProcess()
                .add(ApplyTransform.onDouble(x -> x + 10, VarRange.onlyTypes(VarType.DOUBLE)))
                .add(ApplyTransform.onLabel(cl -> cl + "-x", VarRange.of("class")));

        var transformed = transform.fapply(iris.copy());

        assertFalse(iris.deepEquals(transformed));
        transformed.rvar("class").forEachSpot(s -> assertTrue(s.getLabel().endsWith("-x")));
        transformed.rvar(0).forEachSpot(s -> assertEquals(iris.getDouble(s.row(), 0), s.getDouble() - 10, TOL));

        var second = transform.newInstance().fapply(iris.copy());

        assertTrue(second.deepEquals(transformed));
    }

    @Test
    void testApplyBeforeFit() {
        var iris = Datasets.loadIrisDataset();
        var transform = Preprocessing.newProcess()
                .add(ApplyTransform.onDouble(x -> x+10, VarRange.onlyTypes(VarType.DOUBLE)));
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> transform.apply(iris));
        assertEquals("Transformation not fitted on data before applying it.", ex.getMessage());
    }
}
