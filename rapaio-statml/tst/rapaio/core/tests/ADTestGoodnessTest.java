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

package rapaio.core.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import rapaio.data.VarDouble;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/9/17.
 */
public class ADTestGoodnessTest {

    private static final double TOL = 1e-5;

    @Test
    void basicTest() {
        VarDouble x = VarDouble.wrap(6.0747159, -8.9637424, -1.1363964, 1.5831864, -3.4660379, 2.6695147, 3.0571496, 0.8348192, -11.3294910, 13.8572907);

        ADTestGoodness test = ADTestGoodness.from(x, 1, 5);
        assertEquals(0.5318083, test.pValue(), TOL);
    }
}
