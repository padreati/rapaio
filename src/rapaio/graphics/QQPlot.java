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

package rapaio.graphics;

import rapaio.core.distributions.Distribution;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.graphics.plot.Points;

import static rapaio.data.filters.BaseFilters.sort;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class QQPlot extends Plot {

    public QQPlot() {
        yLab("StatSampling Quantiles");
        xLab("Theoretical Quantiles");
    }

    public QQPlot add(Var points, Distribution distribution) {
        Var x = sort(points);
        Var y = new Numeric(x.rowCount());
        for (int i = 0; i < y.rowCount(); i++) {
            double p = (i + 1) / (y.rowCount() + 1.);
            y.setValue(i, distribution.quantile(p));
        }

        Points pts = new Points(y, x);
        add(pts);
        pts.color(0);
        return this;
    }
}
