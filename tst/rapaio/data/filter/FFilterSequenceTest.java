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

package rapaio.data.filter;

import org.junit.jupiter.api.Test;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.datasets.Datasets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/21/20.
 */
public class FFilterSequenceTest {

    private static final double TOL = 1e-12;

    @Test
    void smokeTest() {
        var iris = Datasets.loadIrisDataset();
        var filters = FFilterSequence.of(
                FApply.onDouble(x -> x + 10, VarRange.onlyTypes(VarType.DOUBLE)),
                FApply.onLabel(cl -> cl + "-x", VarRange.of("class"))
        );

        var transformed = iris.copy().fapply(filters);

        assertFalse(iris.deepEquals(transformed));
        transformed.rvar("class").forEachSpot(s -> assertTrue(s.getLabel().endsWith("-x")));
        transformed.rvar(0).forEachSpot(s -> assertEquals(iris.getDouble(s.row(), 0), s.getDouble() - 10, TOL));

        var second = iris.copy().fapply(filters.newInstance());

        assertTrue(second.deepEquals(transformed));

        Set<String> names = new HashSet<>(Arrays.asList(filters.varNames()));
        assertEquals(names, new HashSet<>(Arrays.asList(iris.varNames())));
    }
}
