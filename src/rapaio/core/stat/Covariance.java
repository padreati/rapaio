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

package rapaio.core.stat;

import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.printer.Printable;

import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static rapaio.WS.formatFlex;
import static rapaio.core.CoreStat.mean;

/**
 * Compute covariance of two variables
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/24/15.
 */
public class Covariance implements Printable {

    private final String varName1;
    private final String varName2;
    private final double value;
    private int completeCount;
    private int missingCount;

    public Covariance(Var var1, Var var2) {
        this.varName1 = var1.name();
        this.varName2 = var2.name();
        this.value = compute(var1, var2);
    }

    private double compute(final Var x, final Var y) {

        Mapping map = Mapping.newWrapOf(IntStream.range(0, Math.min(x.rowCount(), y.rowCount())).filter(row -> !x.missing(row) && !y.missing(row)).boxed().collect(toList()));
        completeCount = map.size();
        missingCount = Math.max(x.rowCount(), y.rowCount()) - completeCount;

        if (map.size() < 2) {
            return 0;
        }

        Var xx = x.mapRows(map);
        Var yy = y.mapRows(map);

        double m1 = mean(xx).value();
        double m2 = mean(yy).value();
        double cov = 0;
        for (int i = 0; i < completeCount; i++) {
            cov += (xx.value(i) - m1) * (yy.value(i) - m2);
        }
        return cov / (completeCount - 1.0);
    }

    public double value() {
        return value;
    }

    @Override
    public void buildPrintSummary(StringBuilder sb) {
        sb.append(String.format("> cov[%s, %s]\n", varName1, varName2));
        sb.append(String.format("total rows: %d (complete: %d, missing: %d)\n", completeCount + missingCount, completeCount, missingCount));
        sb.append(String.format("covariance: %s\n", formatFlex(value)));
    }
}
