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

package rapaio.data.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import rapaio.data.SolidFrame;
import rapaio.data.VarNominal;
import rapaio.data.VarString;
import rapaio.util.Pair;

public class AddNominalStringMatchTest {

    @Test
    void testIrrelevantBreaks() {

        var df = SolidFrame.byVars(
                VarString.wrap(List.of("ijuh21n key jkwhdfwe", "key jwjhdfklw", "uiweduwe key")).name("t1")
        );
        var tdf1 = AddNominalStringMatch.filter("t1", "t2", false, Pair.from("key", List.of("key"))).fitApply(df);
        var tdf2 = AddNominalStringMatch.filter("t1", "t2", true, Pair.from("key", List.of("key"))).fitApply(df);

        assertTrue(tdf1.deepEquals(tdf2));

        assertEquals(2, tdf1.varCount());
        assertEquals("t2", tdf1.rvar(1).name());

        for (int i = 0; i < 3; i++) {
            assertEquals("key", tdf1.getLabel(i, "t2"));
        }
    }

    @Test
    void testRelevantBreaks() {
        var df = SolidFrame.byVars(VarString.wrap(List.of("hjghdhwedwkey", "hjgsdfhaskeyashgdh", "keyhjghdfw")).name("t1"));

        var tdf1 = AddNominalStringMatch.filter("t1", "t2", true, Pair.from("key", List.of("key"))).fitApply(df);
        var tdf2 = AddNominalStringMatch.filter("t1", "t2", false, Pair.from("key", List.of("key"))).newInstance().fitApply(df);

        for (int i = 0; i < 3; i++) {
            assertTrue(tdf1.isMissing(i));
            assertEquals("key", tdf2.getLabel(i, "t2"));
        }
    }

    @Test
    void testAlreadyExistentTarget() {

        var df = SolidFrame.byVars(VarNominal.copy("b").name("source"), VarNominal.copy("a").name("target"));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> df.fapply(AddNominalStringMatch.filter("source", "target", true, Pair.from("a", List.of()))));
        assertEquals("Frame contains already a variable with name: target", ex.getMessage());
    }

    @Test
    void testNonExistentSource() {
        var df = SolidFrame.byVars(VarNominal.copy("b").name("x"), VarNominal.copy("a").name("y"));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> df.fapply(AddNominalStringMatch.filter("source", "target", true, Pair.from("a", List.of()))));
        assertEquals("Frame does not contain variable with name: source", ex.getMessage());
    }
}
