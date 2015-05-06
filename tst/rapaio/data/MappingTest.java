/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.data;

import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class MappingTest {

    @Test
    public void testMappingBuilders() {
        Mapping m = Mapping.newEmpty();
        assertEquals(0, m.size());

        m = Mapping.newCopyOf(1, 3, 5, 7);
        assertEquals(4, m.size());
        assertEquals(1, m.get(0));
        assertEquals(7, m.get(3));

        m = Mapping.newCopyOf(Arrays.asList(1, 3, 5, 7));
        assertEquals(4, m.size());
        assertEquals(1, m.get(0));
        assertEquals(7, m.get(3));
    }

    @Test
    public void testIntervalMappingNotReadOnly() {
        Mapping m = Mapping.newRangeOf(0, 100);
        try {
            m.add(100);
        } catch (IllegalArgumentException ignored) {
            assertTrue("should not raise an exception", false);
        }

        try {
            m.addAll(Arrays.asList(1, 0));
        } catch (IllegalArgumentException ignored) {
            assertTrue("should not raise an exception", false);
        }
    }

    @Test
    public void testListMappingManage() {
        Mapping m = Mapping.newCopyOf(IntStream.range(0, 100).mapToObj(i -> i).collect(Collectors.toList()));

        m.add(1000);
        assertEquals(1000, m.get(m.size() - 1));

        m = Mapping.newCopyOf(0, 1, 3, 4);

        try {
            m.get(10000);
            assertTrue("should raise an exception", false);
        } catch (IllegalArgumentException ignored) {
        }

        assertEquals(4, m.get(3));
        assertEquals(1, m.get(1));

        m.addAll(Arrays.asList(100, 101));

        assertEquals(100, m.get(4));
        assertEquals(101, m.get(5));
        assertEquals(3, m.get(2));
    }
}
