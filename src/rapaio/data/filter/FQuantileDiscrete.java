/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.filter;

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
public class FQuantileDiscrete extends AbstractFFilter {

    public static FQuantileDiscrete split(VarRange varRange, int k) {
        if (k <= 1) {
            throw new IllegalArgumentException("Frame quantile discrete filter allows only splits greater than 1.");
        }
        double[] p = new double[k - 1];
        double step = 1.0 / k;
        for (int i = 0; i < p.length; i++) {
            p[i] = step * (i + 1);
        }
        return new FQuantileDiscrete(p, varRange);
    }

    public static FQuantileDiscrete on(VarRange varRange, double... p) {
        if (p.length < 1) {
            throw new IllegalArgumentException("Frame quantile discrete filter requires at least one probability.");
        }
        return new FQuantileDiscrete(p, varRange);
    }

    @Serial
    private static final long serialVersionUID = -2447577449010618416L;

    private final Map<String, VQuantileDiscrete> filters = new HashMap<>();
    private final double[] p;

    private FQuantileDiscrete(double[] p, VarRange varRange) {
        super(varRange);
        this.p = Arrays.copyOf(p, p.length);
    }

    @Override
    public FQuantileDiscrete newInstance() {
        return new FQuantileDiscrete(p, varRange);
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

        FApplyCommon fApplyQuantileDiscrete = new FApplyCommon();
        return fApplyQuantileDiscrete.applyQuantileDiscrete(df,filters);
    }
}
