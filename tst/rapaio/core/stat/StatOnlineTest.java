/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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

package rapaio.core.stat;

import org.junit.Test;
import rapaio.core.distributions.Normal;
import rapaio.data.Numeric;
import rapaio.data.Vector;
import rapaio.data.Vectors;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Points;
import rapaio.printer.LocalPrinter;

import static rapaio.workspace.Workspace.draw;
import static rapaio.workspace.Workspace.setPrinter;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class StatOnlineTest {

    @Test
    public void testVariance() {

//        RandomSource.setSeed(1223);
        setPrinter(new LocalPrinter());

        int LEN = 1_000;
        Vector v = new Normal(0, 1).sample(LEN);

        StatOnline statOnline = new StatOnline();

        Vector index = Vectors.newSeq(LEN);
        Vector varLeft = new Numeric(new double[LEN]);
        Vector varRight = new Numeric(new double[LEN]);
        Vector varSum = new Numeric(new double[LEN]);

        for (int i = 0; i < LEN; i++) {
            statOnline.update(v.getValue(i));
            if (i > 0) {
                varLeft.setValue(i, statOnline.getVariance());
            }
        }
        statOnline.clean();
        for (int i = LEN - 1; i >= 0; i--) {
            statOnline.update(v.getValue(i));
            if (i < LEN - 1) {
                varRight.setValue(i, statOnline.getVariance());
            }
        }
        for (int i = 0; i < LEN; i++) {
            varSum.setValue(i, (varLeft.getValue(i) + varRight.getValue(i)) / 2);
        }

        draw(new Plot()
                        .add(new Points(index, varLeft).setCol(1))
                        .add(new Points(index, varRight).setCol(2))
                        .add(new Points(index, varSum).setCol(3))
                        .setYLim(0.5, 1.5)
                        .setSize(0.4)
        );
    }
}
