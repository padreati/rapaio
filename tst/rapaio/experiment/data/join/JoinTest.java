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

package rapaio.experiment.data.join;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;
import rapaio.data.VarRange;
import rapaio.data.filter.FRefSort;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/22/18.
 */
public class JoinTest {

    private Frame df1;
    private Frame df2;

    @BeforeEach
    void beforeEach() {
        df1 = SolidFrame.byVars(
                VarNominal.copy("a", "b", "c", "a", "b").name("id"),
                VarInt.copy(20, 20, 40, 30, 40).name("age"),
                VarDouble.wrap(1, 2, 3, 0, 0).name("children")
        );

        df2 = SolidFrame.byVars(
                VarNominal.copy("a", "c", "d", "a", "d").name("id"),
                VarNominal.copy("Iasi", "Iasi", "Bucharest", "Bucharest", "Constanta").name("city")
        );
    }

    @Test
    void leftJoinTest() {
        assertEquals("    id   city    age children     id   city    age children \n" +
                        "[0]  a      Iasi  20    1     [4]  c      Iasi  40    3     \n" +
                        "[1]  a Bucharest  20    1     [5]  d Bucharest   ?    ?     \n" +
                        "[2]  a      Iasi  30    0     [6]  d Constanta   ?    ?     \n" +
                        "[3]  a Bucharest  30    0     \n",
                Join.from(df2, df1, VarRange.of("id"), VarRange.of("id"), Join.Type.LEFT).head(7));

        assertTrue(Join.leftJoin(df1, df2).deepEquals(Join.from(df1, df2, VarRange.of("id"), VarRange.of("id"), Join.Type.LEFT)));
        assertTrue(Join.leftJoin(df2, df1).deepEquals(Join.leftJoin(df2, df1, VarRange.of("id"))));
    }

    @Test
    void rightJoinTest() {

        Frame a1 = Join.leftJoin(df1, df2);
        a1 = a1.fapply(FRefSort.by(a1.rvar(0).refComparator(), a1.rvar(0).refComparator()));
        Frame b1 = Join.rightJoin(df2, df1);
        b1 = b1.fapply(FRefSort.by(b1.rvar(0).refComparator(), b1.rvar(0).refComparator()));
        assertTrue(a1.deepEquals(b1));

        assertTrue(Join.rightJoin(df1, df2).deepEquals(Join.leftJoin(df2, df1)));
    }

}
