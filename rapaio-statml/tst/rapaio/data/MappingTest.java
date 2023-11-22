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

package rapaio.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class MappingTest {

    private static final double TOL = 1e-20;

    @Test
    void testMappingBuilders() {
        Mapping empty1 = Mapping.empty();
        assertEquals(0, empty1.size());

        VarInt wrap1 = VarInt.wrap(1, 3, 5, 7);

        testMap(Mapping.wrap(1, 3, 5, 7), 1, 3, 5, 7);
        testMap(Mapping.wrap(wrap1), 1, 3, 5, 7);
        testMap(Mapping.copy(wrap1), 1, 3, 5, 7);
        testMap(Mapping.from(wrap1, x -> x + 1), 2, 4, 6, 8);

        testMap(Mapping.range(5), 0, 1, 2, 3, 4);
        testMap(Mapping.range(2, 5), 2, 3, 4);
    }

    @Test
    void testCollector() {
        Mapping map1 = IntStream.range(5, 10).boxed().collect(Mapping.collector());
        testMap(map1, 5, 6, 7, 8, 9);

        Mapping map2 = IntStream.range(0, 1000).parallel().boxed().collect(Mapping.collector());
        double cnt = map2.stream().mapToDouble(x -> x).sum();
        assertEquals(499500, cnt, TOL);
    }

    private void testMap(Mapping mapping, int... values) {
        assertArrayEquals(mapping.stream().toArray(), values);
    }
}
