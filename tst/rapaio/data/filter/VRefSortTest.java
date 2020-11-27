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

package rapaio.data.filter;

import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/28/18.
 */
public class VRefSortTest {

    @Test
    void testSortRef() {
        RandomSource.setSeed(1);
        Var x1 = VarNominal.copy("z", "q", "a", "b", "d", "c");
        Var x2 = VarDouble.copy(7, 6, 1, 2, 5, 4);
        Var x3 = x2.copy().fapply(VRefSort.from());
        Var x4 = x1.copy().fapply(VRefSort.from());
        for (int i = 0; i < x3.size() - 1; i++) {
            assertTrue(Double.compare(x3.getDouble(i), x3.getDouble(i + 1)) <= 0);
        }
        for (int i = 0; i < x4.size() - 1; i++) {
            assertTrue(x4.getLabel(i).compareTo(x4.getLabel(i + 1)) <= 0);
        }

        // TODO test aggregate comparators
    }
}
