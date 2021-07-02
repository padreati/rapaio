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

package rapaio.experiment.ml.common.predicate;

import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.stream.FSpot;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/21/17.
 */
public class RowPredicateTest {

    private boolean test(FSpot s, RowPredicate rp) {
        return rp.test(s.row(), s.df());
    }

    @Test
    void testNumPredicates() {

        Frame df = SolidFrame.byVars(VarDouble.from(50, Math::sqrt).name("x"));

        // test basic numeric predicates

        RowPredicate rp1 = RowPredicate.numLessEqual("x", 4);
        RowPredicate rp2 = RowPredicate.numLess("x", 4);
        RowPredicate rp3 = RowPredicate.numGreater("x", 4);
        RowPredicate rp4 = RowPredicate.numGreaterEqual("x", 4);

        assertEquals(17, df.stream().filter(s -> test(s, rp1)).count());
        assertEquals(16, df.stream().filter(s -> test(s, rp2)).count());
        assertEquals(33, df.stream().filter(s -> test(s, rp3)).count());
        assertEquals(34, df.stream().filter(s -> test(s, rp4)).count());

        // add some missing values, and test the count sum to be correct

        RandomSource.setSeed(123);

        for (int row : SamplingTools.sampleWOR(50, 10)) {
            df.setMissing(row, "x");
        }

        assertEquals(40, df.stream().filter(s -> test(s, rp1)).count() + df.stream().filter(s -> test(s, rp3)).count());
        assertEquals(40, df.stream().filter(s -> test(s, rp2)).count() + df.stream().filter(s -> test(s, rp4)).count());

        // set all missing to be sure we have 0 passes

        for (int i = 0; i < df.rowCount(); i++) {
            df.setMissing(i, "x");
        }

        assertEquals(0, df.stream().filter(s -> test(s, rp1)).count());
        assertEquals(0, df.stream().filter(s -> test(s, rp2)).count());
        assertEquals(0, df.stream().filter(s -> test(s, rp3)).count());
        assertEquals(0, df.stream().filter(s -> test(s, rp4)).count());

        // now check the names

        assertEquals("x<=4", rp1.toString());
        assertEquals("x<4", rp2.toString());
        assertEquals("x>4", rp3.toString());
        assertEquals("x>=4", rp4.toString());
    }

    @Test
    void testBinaryPredicates() {

        int[] values = SamplingTools.sampleWR(2, 100);
        SolidFrame df = SolidFrame.byVars(VarBinary.from(values.length, row -> values[row] == 1).name("x"));

        assertEquals(100, df.stream().filter(s -> RowPredicate.binEqual("x", true).test(s.row(), s.df())).count()
                + df.stream().filter(s -> RowPredicate.binEqual("x", false).test(s.row(), s.df())).count());
    }
}
