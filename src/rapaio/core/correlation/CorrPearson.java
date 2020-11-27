/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.core.correlation;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * /**
 * Pearson product-moment correlation coefficient.
 * <p>
 * See
 * http://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CorrPearson extends AbstractCorrelation {

    private static final long serialVersionUID = -7342261109217205843L;

    public static CorrPearson of(Frame df) {
        return new CorrPearson(df);
    }

    public static CorrPearson of(Var... vars) {
        return new CorrPearson(vars);
    }

    private CorrPearson(Frame df) {
        this(df.varStream().toArray(Var[]::new), df.varNames());
    }

    private CorrPearson(Var... vars) {
        this(vars, Arrays.stream(vars).map(Var::name).toArray(String[]::new));
    }

    private CorrPearson(Var[] vars, String[] names) {
        super(vars, names);
        for (int i = 0; i < vars.length; i++) {
            d.set(i, i, 1);
            for (int j = i + 1; j < vars.length; j++) {
                d.set(i, j, compute(vars[i], vars[j]));
            }
        }
    }

    private double compute(Var x, Var y) {

        double sum = 0;
        int len = Math.min(x.size(), y.size());

        Mapping map = Mapping.wrap(IntStream.range(0, len)
                .filter(i -> !(x.isMissing(i) || y.isMissing(i)))
                .toArray());
        double xMean = Mean.of(x.mapRows(map)).value();
        double yMean = Mean.of(y.mapRows(map)).value();

        double sdp = Variance.of(x.mapRows(map)).sdValue() * Variance.of(y.mapRows(map)).sdValue();
        for (int i = 0; i < map.size(); i++) {
            int pos = map.get(i);
            sum += ((x.getDouble(pos) - xMean) * (y.getDouble(pos) - yMean));
        }
        return sdp == 0 ? Double.NaN : sum / (sdp * (map.size() - 1));
    }

    @Override
    protected String corrName() {
        return "pearson";
    }

    @Override
    protected String corrDescription() {
        return "Pearson product-moment correlation coefficient";
    }

}
