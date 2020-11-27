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
public class VSortTest {

    @Test
    void testSortNominal() {
        RandomSource.setSeed(1);
        Var x1 = VarNominal.copy("z", "q", "a", "b", "d", "c");
        Var x2 = x1.fapply(VSort.asc());
        for (int i = 0; i < x2.size() - 1; i++) {
            assertTrue(x2.getLabel(i).compareTo(x2.getLabel(i + 1)) <= 0);
        }
        Var x3 = x1.fapply(VSort.asc(false));
        for (int i = 0; i < x3.size() - 1; i++) {
            assertTrue(x3.getLabel(i).compareTo(x3.getLabel(i + 1)) >= 0);
        }
    }

    @Test
    void testSortNumeric() {
        RandomSource.setSeed(1);
        Var x1 = VarDouble.copy(7, 5, 1, 2, 5, 4);
        Var x2 = x1.fapply(VSort.asc());
        for (int i = 0; i < x2.size() - 1; i++) {
            assertTrue(Double.compare(x2.getDouble(i), x2.getDouble(i + 1)) <= 0);
        }
        Var x3 = x1.fapply(VSort.asc(false));
        for (int i = 0; i < x3.size() - 1; i++) {
            assertTrue(Double.compare(x3.getDouble(i), x3.getDouble(i + 1)) >= 0);
        }
    }
}
