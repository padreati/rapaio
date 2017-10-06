/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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

package rapaio.graphics.plot;

import rapaio.core.distributions.Distribution;
import rapaio.data.NumVar;
import rapaio.data.Var;
import rapaio.data.filter.var.VFSort;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.plot.plotcomp.Points;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class QQPlot extends Plot {

    private static final long serialVersionUID = -3244871850592508515L;

    public QQPlot(Var points, Distribution distribution, GOption... opts) {

        this.options.bind(opts);
        Var x = new VFSort().fitApply(points);
        Var y = NumVar.empty(x.rowCount());
        for (int i = 0; i < y.rowCount(); i++) {
            double p = (i + 1) / (y.rowCount() + 1.);
            y.setValue(i, distribution.quantile(p));
        }
        add(new Points(y, x));
        yLab("Sampling Quantiles");
        xLab("Theoretical Quantiles");
        title("QQPlot - sample vs. " + distribution.name());
    }
}
