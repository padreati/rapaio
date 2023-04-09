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

package rapaio.util.nparam;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;

import org.junit.jupiter.api.Test;

public class NamedParamSetTest {

    @Test
    void testNamed() {

        GOpts set = new GOpts(null);
        assertEquals("GOpts{colour:java.awt.Color[r=0,g=0,b=0],age:10}", set.toString());

        set = set.bind(GOpts.color(Color.BLUE));
        assertEquals("GOpts{colour:java.awt.Color[r=0,g=0,b=255],age:10}", set.toString());

        GOpts set2 = set.bind(GOpts.size(20));
        assertEquals("GOpts{colour:java.awt.Color[r=0,g=0,b=255],age:20}", set2.toString());
    }
}

final class GOpts extends NamedParamSet<GOpts, GOpt<?>> {

    private static final GOpt<Color> pColor = new GOpt<>("colour", Color.BLACK);
    private static final GOpt<Integer> pSize = new GOpt<>("age", 10);

    public static GOpt<Color> color(Color c) {
        return new GOpt<>(pColor, c);
    }

    public static GOpt<Integer> size(int size) {
        return new GOpt<>(pSize, size);
    }

    public GOpts(GOpts parent) {
        super(parent);
    }

    public GOpts bind(GOpt<?>... parameters) {
        return new GOpts(this).apply(parameters);
    }

    public Color getColor() {
        return (Color) getParamValue(pColor);
    }

    public Integer getSize() {
        return (Integer) getParamValue(pSize);
    }
}

final class GOpt<V> extends NamedParam<V> {

    public GOpt(String name, V value) {
        super(name, value);
    }

    public GOpt(GOpt<V> p, V value) {
        super(p.getName(), value);
    }
}
