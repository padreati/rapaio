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

package rapaio.math.linear.base;

import org.junit.jupiter.api.Test;
import rapaio.data.VarDouble;
import rapaio.math.linear.DV;
import rapaio.math.linear.StandardDVTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/9/20.
 */
public class DVBaseTest extends StandardDVTest {

    @Override
    public DV.Type type() {
        return DV.Type.BASE;
    }

    @Override
    public DV generateWrap(double[] values) {
        return DVBase.wrap(values);
    }

    @Override
    public DV generateFill(int size, double fill) {
        double[] array = new double[size];
        for (int i = 0; i < size; i++) {
            array[i] = fill;
        }
        return DVBase.wrap(array);
    }

    @Override
    public String className() {
        return "DVBase";
    }

    @Test
    void testBuilders() {
        DV vector = DVBase.wrap(values);

        assertEquals(100, vector.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(values[i], vector.get(i), TOL);
        }

        DV copy = vector.copy();
        for (int i = 0; i < copy.size(); i++) {
            copy.set(i, 10);
            assertEquals(10, copy.get(i));
        }
    }

    @Test
    void testStream() {
        double[] streamValues = x.valueStream().toArray();
        assertArrayEquals(values, streamValues, TOL);
    }

    @Test
    void testToVarDouble() {
        VarDouble v = x.asVarDouble();
        assertEquals(100, v.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(values[i], v.getDouble(i), TOL);
        }
    }

}
