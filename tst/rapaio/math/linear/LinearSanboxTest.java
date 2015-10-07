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

package rapaio.math.linear;

import org.junit.Test;
import rapaio.sys.WS;
import rapaio.util.Pair;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/7/15.
 */
public class LinearSanboxTest {

    @Test
    public void eigenValueTest() {

        RM A = Linear.newRMWrapOf(4, 4,
                52, 30, 49, 28,
                30, 50, 8, 44,
                49, 8, 46, 16,
                28, 44, 16, 22);

        EigenPair ep = Linear.pdEigenDecomp(A, 100, 1e-20);
        ep.values().printSummary();
        ep.vectors().printSummary();
    }
}
