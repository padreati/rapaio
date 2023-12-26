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


import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;


/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class VarRangeTest {

    @Test
    void testSmoke() {
        Frame df = SolidFrame.byVars(
                VarDouble.empty().name("a"),
                VarDouble.empty().name("b"),
                VarDouble.empty().name("c"),
                VarDouble.empty().name("d"),
                VarNominal.empty(0, "A", "B").name("x"),
                VarNominal.empty(0, "c", "D").name("y")
        );

        test(VarRange.of(0, 2), df,
                new int[]{0, 2},
                new String[]{"a", "c"},
                new String[]{"b", "d", "x", "y"});

        test(VarRange.of("a", "c"), df,
                new int[]{0, 2},
                new String[]{"a", "c"},
                new String[]{"b", "d", "x", "y"});

        test(VarRange.of(Arrays.asList("a", "c")), df,
                new int[]{0, 2},
                new String[]{"a", "c"},
                new String[]{"b", "d", "x", "y"});

        test(VarRange.byName(name -> name.compareTo("x") >= 0), df,
                new int[]{4, 5},
                new String[]{"x", "y"},
                new String[]{"a", "b", "c", "d"});

        test(VarRange.byFilter(rvar -> rvar.name().compareTo("x") >= 0), df,
                new int[]{4, 5},
                new String[]{"x", "y"},
                new String[]{"a", "b", "c", "d"});

        test(VarRange.onlyTypes(VarType.DOUBLE), df,
                new int[]{4, 5},
                new String[]{"x", "y"},
                new String[]{"a", "b", "c", "d"});

        test(VarRange.all(), df,
                new int[]{0, 1, 2, 3, 4, 5},
                new String[]{"a", "b", "c", "d", "x", "y"},
                new String[]{});

        test(VarRange.of("all"), df,
                new int[]{0, 1, 2, 3, 4, 5},
                new String[]{"a", "b", "c", "d", "x", "y"},
                new String[]{});

        test(VarRange.of("a~d"), df,
                new int[]{0, 1, 2, 3},
                new String[]{"a", "b", "c", "d"},
                new String[]{"x", "y"});

        test(VarRange.of("0~3"), df,
                new int[]{0, 1, 2, 3},
                new String[]{"a", "b", "c", "d"},
                new String[]{"x", "y"});
    }

    @Test
    public void testInvalidRanges() {
        Frame df = SolidFrame.byVars(
                VarDouble.empty().name("a"),
                VarDouble.empty().name("b"),
                VarDouble.empty().name("c"),
                VarDouble.empty().name("d"),
                VarNominal.empty(0, "A", "B").name("x"),
                VarNominal.empty(0, "c", "D").name("y")
        );

        test(VarRange.of("0~af,a~q,q,q~a"), df, new int[0], new String[0], new String[0]);
    }

    @Test
    public void testNoIndexes() {
        assertThrows(IllegalArgumentException.class, () -> VarRange.of(new int[]{}));
    }

    private void test(VarRange range, Frame df, int[] indexes, String[] names, String[] reverse) {
        int[] indexesReal = range.parseVarIndexes(df).stream().mapToInt(x -> x).toArray();
        String[] namesReal = range.parseVarNames(df).toArray(new String[0]);
        String[] reverseReal = range.parseInverseVarNames(df).toArray(new String[0]);
    }
}
