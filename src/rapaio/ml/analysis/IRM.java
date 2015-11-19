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

package rapaio.ml.analysis;

import rapaio.core.distributions.Uniform;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
import rapaio.math.linear.Linear;
import rapaio.math.linear.RM;
import rapaio.ml.common.distance.Distance;
import rapaio.util.Tag;

import java.util.function.BiConsumer;

/**
 * Iterative re-weighting map
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/6/15.
 */
public class IRM {

    private Tag<Distance> distance = Distance.EUCLIDEAN;

    private double tol = 1e-20;
    private int maxRuns = 100;
    private double s = 2;

    private int len;

    private Numeric x;
    private Numeric y;

    private RM d;

    private BiConsumer<IRM, Integer> runningHook = null;

    public IRM withMaxRuns(int maxRuns) {
        this.maxRuns = maxRuns;
        return this;
    }

    public IRM withRunningHook(BiConsumer<IRM, Integer> c) {
        this.runningHook = c;
        return this;
    }

    public void learn(Frame df) {

        len = df.rowCount();

        Uniform uniform = new Uniform(0, 1);
        x = Numeric.from(len, uniform::sampleNext).withName("x");
        y = Numeric.from(len, uniform::sampleNext).withName("y");

        d = Linear.newRMFill(len, len, 0);
        for (int i = 0; i < len; i++) {
            for (int j = i + 1; j < len; j++) {
                double dist = distance.get().distance(df, i, df, j, df.varNames());
                d.set(i, j, dist);
                d.set(j, i, dist);
            }
        }

        for (int r = 0; r < maxRuns; r++) {

            for (int i = 0; i < len; i++) {
                double xStep = 0;
                double yStep = 0;
                for (int j = 0; j < len; j++) {
                    if (i == j)
                        continue;

                    double xx = x.value(j) - x.value(i);
                    double yy = y.value(j) - y.value(i);
                    double norm = Math.sqrt(xx * xx + yy * yy);

                    xStep += xx * Math.pow((norm - d.get(i, j)) / norm, 2) / s;
                    yStep += yy * Math.pow((norm - d.get(i, j)) / norm, 2) / s;
                    x.setValue(i, x.value(i) + xStep);
                    y.setValue(i, y.value(i) + yStep);
                }
            }
            if (runningHook != null) {
                runningHook.accept(this, r);
            }
        }
    }

    public Frame getMap() {
        return SolidFrame.newWrapOf(x, y);
    }
}
