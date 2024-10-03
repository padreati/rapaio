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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import rapaio.data.Frame;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/4/18.
 */
public class RefSortTest {

    @Test
    void testRefSort() {

        Frame src = TransformTestUtil.allDoubleNominal(new Random(), 10, 2, 2);

        Frame sort1 = src.fapply(RefSort.by(src.rvar("v2").refComparator()));
        Frame sort2 = sort1.fapply(RefSort.by(sort1.rvar("v1").refComparator()));
        Frame sort3 = src.fapply(RefSort.by(src.rvar("v1").refComparator(), src.rvar("v2").refComparator()).newInstance());

        assertTrue(sort2.deepEquals(sort3));
    }
}
