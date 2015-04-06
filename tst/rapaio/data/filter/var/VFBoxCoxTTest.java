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
 */

package rapaio.data.filter.var;

import org.junit.Test;
import rapaio.WS;
import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Uniform;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.graphics.plot.GridLayer;
import rapaio.graphics.plot.Histogram;
import rapaio.graphics.plot.Plot;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/11/14.
 */
public class VFBoxCoxTTest {

    @Test
    public void basicTest() {
        Var a = Numeric.newEmpty();
        Distribution d = new Uniform(-100, 100);
        for (int i = 0; i < 1_000; i++) {
            a.addValue(Math.sqrt(1 + d.sampleNext()));
        }

        Var b = new VFBoxCoxT(5, 10).fitApply(a.solidCopy());

        WS.draw(new GridLayer(1, 2)
                        .add(1, 1, new Plot().add(new Histogram(a)).title("original"))
                        .add(1, 2, new Plot().add(new Histogram(b)).title("transformed"))
        );
    }
}
