/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
import rapaio.data.filter.var.VFImputeWithRegression;
import rapaio.ml.regression.Regression;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/30/15.
 */
public class FFImputeWithRegression extends AbstractFF {

    private static final long serialVersionUID = -2447577449010618416L;

    Regression model;
    VRange inputRange;

    Map<String, VFImputeWithRegression> filters = new HashMap<>();

    public FFImputeWithRegression(Regression model, VRange inputRange, String...varNames) {
        this(model, inputRange, VRange.of(varNames));
    }

    public FFImputeWithRegression(Regression model, VRange inputRange, VRange vRange) {
        super(vRange);
        this.inputRange = inputRange;
        this.model = model.newInstance();
    }

    @Override
    public FFImputeWithRegression newInstance() {
        return new FFImputeWithRegression(model, inputRange, vRange);
    }

    @Override
    public void train(Frame df) {
        parse(df);
        filters.clear();
        for (String varName : varNames) {
            VFImputeWithRegression filter = new VFImputeWithRegression(model, inputRange, varName);
            filter.fit(df.varStream().toArray(Var[]::new));
            filters.put(varName, filter);
        }
    }

    @Override
    public Frame apply(Frame df) {
        Var[] vars = new Var[df.getVarCount()];
        int pos = 0;
        for (String varName : df.getVarNames()) {
            if (filters.containsKey(varName)) {
                vars[pos++] = filters.get(varName).apply(df.varStream().toArray(Var[]::new));
            } else {
                vars[pos++] = df.getVar(varName);
            }
        }
        return BoundFrame.byVars(vars);
    }
}
