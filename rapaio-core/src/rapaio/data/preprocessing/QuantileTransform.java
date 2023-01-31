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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.data.preprocessing;

import java.io.Serial;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/30/15.
 */
public class QuantileTransform extends AbstractTransform {

    public static QuantileTransform split(VarRange varRange, int k) {
        if (k <= 1) {
            throw new IllegalArgumentException("Frame quantile discrete filter allows only splits greater than 1.");
        }
        double[] p = new double[k - 1];
        double step = 1.0 / k;
        for (int i = 0; i < p.length; i++) {
            p[i] = step * (i + 1);
        }
        return new QuantileTransform(p, varRange);
    }

    public static QuantileTransform on(VarRange varRange, double... p) {
        if (p.length < 1) {
            throw new IllegalArgumentException("Frame quantile discrete filter requires at least one probability.");
        }
        return new QuantileTransform(p, varRange);
    }

    @Serial
    private static final long serialVersionUID = -2447577449010618416L;

    private final Map<String, VarQuantileTransform> filters = new HashMap<>();
    private final double[] p;

    private QuantileTransform(double[] p, VarRange varRange) {
        super(varRange);
        this.p = Arrays.copyOf(p, p.length);
    }

    @Override
    public QuantileTransform newInstance() {
        return new QuantileTransform(p, varRange);
    }

    @Override
    public void coreFit(Frame df) {
        filters.clear();
        for (String varName : varNames) {
            VarQuantileTransform filter = VarQuantileTransform.with(p);
            filter.fit(df.rvar(varName));
            filters.put(varName, filter);
        }
    }

    @Override
    public Frame coreApply(Frame df) {

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
