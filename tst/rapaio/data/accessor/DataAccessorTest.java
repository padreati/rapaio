/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.accessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/18/18.
 */
public class DataAccessorTest {

    private static final double TOL = 1e-20;

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(1234);
    }

    @Test
    void testVarDoubleDataAccessor() {
        VarDouble x = VarDouble.from(100, RandomSource::nextDouble);

        double[] data = x.elements();
        for (int i = 0; i < x.size(); i++) {
            data[i] = 1.;
        }
        x.setElements(data, 10);

        assertEquals(10, x.size());
        for (int i = 0; i < x.size(); i++) {
            assertEquals(1.0, x.getDouble(i), TOL);
        }
    }

    @Test
    void testVarLongDataAccessor() {
        VarLong x = VarLong.from(100, () -> (long) RandomSource.nextDouble() * 100);

        long[] data = x.getArray();
        int rows = x.size();
        for (int i = 0; i < rows; i++) {
            data[i] = 10L;
        }
        x.setArray(data, 10);

        assertEquals(10, x.size());
        for (int i = 0; i < 10; i++) {
            assertEquals(10L, x.getLong(i));
        }
    }

    @Test
    void testVarIntDataAccessor() {
        VarInt x = VarInt.from(100, row -> (int) RandomSource.nextDouble() * 100);

        int[] data = x.elements();
        int rows = x.size();
        for (int i = 0; i < rows; i++) {
            data[i] = 10;
        }
        x.setElements(data, 10);

        assertEquals(10, x.size());
        for (int i = 0; i < 10; i++) {
            assertEquals(10L, x.getInt(i));
        }
    }
}
