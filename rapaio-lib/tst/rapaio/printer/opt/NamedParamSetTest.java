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

package rapaio.printer.opt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.function.Function;

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

    static final class GOpt<V> extends NamedParam<GOpts, V> {

        public GOpt(String name, Function<GOpts, V> fun) {
            super(name, fun);
        }

        public GOpt(GOpt<V> p, Function<GOpts, V> fun) {
            super(p.getName(), fun);
        }
    }

    static final class GOpts extends NamedParamSet<GOpts, GOpt<?>> {

        // named parameters definitions with default values

        private static final GOpt<Boolean> _verbose = new GOpt<>("verbose", __ -> true);
        private static final GOpt<Color> _color = new GOpt<>("colour", __ -> Color.GREEN);
        private static final GOpt<DecimalFormat> _floatFormat = new GOpt<>("floatFormat",
                s -> s.getVerbose() ? new DecimalFormat("0.00000000") : new DecimalFormat("0.00"));
        private static final GOpt<Color[]> _repeatColors = new GOpt<>("repeatColors", __ -> new Color[] {Color.GREEN});

        // named parameter static builders

        public static GOpt<Boolean> verbose(boolean verbose) {
            return new GOpt<>(_verbose, __ -> verbose);
        }

        public static GOpt<Color> color(Color c) {
            return new GOpt<>(_color, __ -> c);
        }

        public static GOpt<Color[]> repeatColors(int len) {
            return new GOpt<>(_repeatColors, s -> {
                Color[] array = new Color[len];
                Arrays.fill(array, s.getColor());
                return array;
            });
        }

        public GOpts(GOpts parent) {
            super(parent);
        }

        public GOpts bind(GOpt<?>... parameters) {
            return new GOpts(this).apply(parameters);
        }

        // getters for parameter values

        public Boolean getVerbose() {
            return (Boolean) getParamValue(_verbose);
        }

        public Color getColor() {
            return (Color) getParamValue(_color);
        }

        public DecimalFormat getFloatFormat() {
            return (DecimalFormat) getParamValue(_floatFormat);
        }

        public Color[] getRepeatColors() {
            return (Color[]) getParamValue(_repeatColors);
        }
    }
}

