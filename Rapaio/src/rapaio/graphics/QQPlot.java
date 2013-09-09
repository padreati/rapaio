/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

import rapaio.data.OneIndexVector;
import rapaio.data.NumericVector;
import rapaio.data.Vector;
import rapaio.distributions.Distribution;
import static rapaio.filters.RowFilters.*;
import rapaio.graphics.plot.Points;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class QQPlot extends Plot {

    public QQPlot() {
        setLeftLabel("Sample Quantiles");
        setBottomLabel("Theoretical Quantiles");
    }

    public void add(Vector points, Distribution distribution) {
        Vector x = sort(points);

        Vector y = new NumericVector("pdf", x.getRowCount());
        for (int i = 0; i < y.getRowCount(); i++) {
            double p = (i + 1) / (y.getRowCount() + 1.);
            y.setValue(i, distribution.quantile(p));
        }

        Points pts = new Points(this, y, x);
        pts.opt().setColorIndex(new OneIndexVector(0));
        this.add(pts);
    }

}
