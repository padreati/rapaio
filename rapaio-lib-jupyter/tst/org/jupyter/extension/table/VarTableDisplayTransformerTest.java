/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package org.jupyter.extension.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rapaio.jupyter.kernel.display.table.DataType;
import org.rapaio.jupyter.kernel.display.table.TableDisplay;

import rapaio.data.Var;
import rapaio.data.VarDouble;

public class VarTableDisplayTransformerTest {

    private final VarTableDisplayTransformer transformer = new VarTableDisplayTransformer();
    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void testCanTransform() {
        assertFalse(transformer.canTransform(null));
        assertTrue(transformer.canTransform(VarDouble.empty()));
        assertFalse(transformer.canTransform(random));
    }

    @Test
    void testTransformedClass() {
        assertEquals(TableDisplay.class, transformer.transformedClass());
    }

    @Test
    void testTransform() {
        Var v = VarDouble.wrap(1.0, Double.NaN, 3.0).name("test");

        var o = transformer.transform(v);
        assertNotNull(o);
        assertInstanceOf(TableDisplay.class, o);

        TableDisplay td = (TableDisplay) o;

        assertEquals(v.size(), td.getRows());
        assertEquals(1, td.getCols());
        assertEquals(v.name(), td.columnName(0));
        assertEquals(DataType.FLOAT, td.columnType(0));
    }
}
