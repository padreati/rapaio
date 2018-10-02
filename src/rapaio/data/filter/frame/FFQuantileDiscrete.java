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

package rapaio.data.filter.frame;

import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.Var;
import rapaio.data.filter.var.VQuantileDiscrete;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/30/15.
 */
public class FFQuantileDiscrete extends AbstractFF {

    private static final long serialVersionUID = -2447577449010618416L;

    Map<String, VQuantileDiscrete> filters = new HashMap<>();
    int k;

    public FFQuantileDiscrete(int k, VRange vRange) {
        super(vRange);
        this.k = k;
    }

    @Override
    public FFQuantileDiscrete newInstance() {
        return new FFQuantileDiscrete(k, vRange);
    }

    @Override
    public void coreFit(Frame df) {
        filters.clear();
        for (String varName : varNames) {
            VQuantileDiscrete filter = VQuantileDiscrete.split(k);
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
