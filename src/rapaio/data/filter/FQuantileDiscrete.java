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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.data.filter;

import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.Var;
import rapaio.data.filter.ffilter.AbstractFFilter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/30/15.
 */
public class FQuantileDiscrete extends AbstractFFilter {

    public static FQuantileDiscrete split(VRange vRange, int k) {
        if (k <= 1) {
            throw new IllegalArgumentException("Frame quantile discrete filter allows only splits greater than 1.");
        }
        double[] p = new double[k - 1];
        double step = 1.0 / k;
        for (int i = 0; i < p.length; i++) {
            p[i] = step * (i + 1);
        }
        return new FQuantileDiscrete(p, vRange);
    }

    public static FQuantileDiscrete on(VRange vRange, double... p) {
        if (p.length < 1) {
            throw new IllegalArgumentException("Frame quantile discrete filter requires at least one probability.");
        }
        return new FQuantileDiscrete(p, vRange);
    }

    private static final long serialVersionUID = -2447577449010618416L;

    private final Map<String, VQuantileDiscrete> filters = new HashMap<>();
    private final double[] p;

    private FQuantileDiscrete(double[] p, VRange vRange) {
        super(vRange);
        this.p = Arrays.copyOf(p, p.length);
    }

    @Override
    public FQuantileDiscrete newInstance() {
        return new FQuantileDiscrete(p, vRange);
    }

    @Override
    public void coreFit(Frame df) {
        filters.clear();
        for (String varName : varNames) {
            VQuantileDiscrete filter = VQuantileDiscrete.with(p);
            filter.fit(df.rvar(varName));
            filters.put(varName, filter);
        }
    }

    @Override
    public Frame apply(Frame df) {
        Var[] vars = new Var[df.varCount()];
        int pos = 0;
        for (String varName : df.varNames()) {
            if (filters.containsKey(varName)) {
                vars[pos++] = filters.get(varName).apply(df.rvar(varName));
            } else {
                vars[pos++] = df.rvar(varName);
            }
        }
        return BoundFrame.byVars(vars);
    }
}
