/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TokensTest {

    @Test
    public void testBasicUsage() {

        Tokens a = Tokens.from("a", "b", "c", "d");
        Tokens b = Tokens.parse(" [\"a, \"b\", c,    d, , ]");
        Tokens c = Tokens.parse("b,d");
        Tokens d = Tokens.parse("");
        Tokens e = Tokens.parse("b,c,d,e");

        // those should be equal
        assertEquals(0, a.compareTo(b));

        // this is valid for to equal index tuples
        assertEquals(true, a.contains(b));
        assertEquals(true, b.contains(a));

        // this is valid only in one way
        assertEquals(true, a.contains(c));
        assertEquals(false, c.contains(a));

        // this is valid for contains with empty
        assertEquals(true, a.contains(d));
        assertEquals(false, d.contains(a));

        // test overlaps
        assertEquals(true, a.overlaps(e));
        assertEquals(false, e.overlaps(a));

        assertEquals(Tokens.parse("a,b,c,d,e"), a.joinOverlap(e));

        Tokens x = Tokens.parse("a");
        Tokens y = Tokens.parse("b");

        assertEquals(true, x.overlaps(y));
        assertEquals(true, y.overlaps(x));

        assertEquals(Tokens.parse("a,b"), x.joinOverlap(y));
        assertEquals(Tokens.parse("b,a"), y.joinOverlap(x));
    }
}
