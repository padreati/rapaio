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

package rapaio.ml.eval;

import org.junit.Test;
import rapaio.data.Numeric;
import rapaio.experiment.ml.eval.NormalizedGini;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/17/15.
 */
public class NormalizedGiniTest {

    @Test
    public void testSmoke() {

        Numeric x = Numeric.copy(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Numeric y = Numeric.copy(1, 4, 7, 2, 9, 3, 8, 5, 6);

        double eval = new NormalizedGini(x, y).value();
        System.out.println(eval);

        System.out.println(new NormalizedGini(x, x).value());
        System.out.println(new NormalizedGini(y, x).value());
    }
}
