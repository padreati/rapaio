/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.experiment.math.linear.dense;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import rapaio.experiment.math.linear.StandardDVectorTest;
import rapaio.experiment.math.linear.DVector;
import rapaio.util.collection.DoubleArrays;

public class DVectorDenseTest extends StandardDVectorTest {

    @Override
    public DVector generateFill(int size, double fill) {
        return new DVectorDense(10, size, DoubleArrays.newFill(10 + size, fill));
    }

    @Override
    public DVector generateSeq(int end) {
        double[] base = new double[10 + end];
        for (int i = 0; i < end; i++) {
            base[10 + i] = i;
        }
        return new DVectorDense(10, end, base);
    }

    @Override
    public DVector generateCopy(double[] values) {
        return new DVectorDense(0, values.length, Arrays.copyOf(values, values.length));
    }

    @Override
    public String className() {
        return DVectorDense.class.getSimpleName();
    }

    @Test
    void testBuilders() {
        DVectorDense v = DVectorDense.empty(6);
        assertEquals(6, v.size());
        assertEquals(0, v.get(2));

        double[] array = new double[] {1, 2, 3, 4, 5, 6};
        v = DVectorDense.wrapAt(2, 2, array);
        assertEquals(2, v.size());
        assertEquals(3, v.get(0));
        assertEquals(4, v.get(1));
    }
}
