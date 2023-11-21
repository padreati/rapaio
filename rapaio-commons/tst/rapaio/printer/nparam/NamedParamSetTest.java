/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.printer.nparam;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;

import org.junit.jupiter.api.Test;

public class NamedParamSetTest {

    @Test
    void testNamed() {

        GOpts set = new GOpts(null);
        assertEquals(true, set.getVerbose());
        assertEquals(Color.GREEN, set.getColor());
        assertEquals("0.10000000", set.getFloatFormat().format(0.1));
        assertArrayEquals(new Color[] {Color.GREEN}, set.getRepeatColors());

        set = set.bind(GOpts.color(Color.BLUE), GOpts.repeatColors(2));
        assertEquals(true, set.getVerbose());
        assertEquals(Color.BLUE, set.getColor());
        assertEquals("0.10000000", set.getFloatFormat().format(0.1));
        assertArrayEquals(new Color[] {Color.BLUE, Color.BLUE}, set.getRepeatColors());

        set = set.bind(GOpts.verbose(false));
        assertEquals(false, set.getVerbose());
        assertEquals(Color.BLUE, set.getColor());
        assertEquals("0.10", set.getFloatFormat().format(0.1));
        assertArrayEquals(new Color[] {Color.BLUE, Color.BLUE}, set.getRepeatColors());
    }
}

