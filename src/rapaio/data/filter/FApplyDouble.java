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

import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.filter.ffilter.AbstractFFilter;

import java.util.function.Function;

/**
 * Update a double variable by changing it's value using a function.
 *
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/15/14.
 */
public class FApplyDouble extends AbstractFFilter {

    public static FApplyDouble on(Function<Double, Double> fun, VRange vRange) {
        return new FApplyDouble(fun, vRange);
    }

    private static final long serialVersionUID = 3982915877968295381L;
    private final Function<Double, Double> f;

    private FApplyDouble(Function<Double, Double> f, VRange vRange) {
        super(vRange);
        this.f = f;
    }

    @Override
    public FFilter newInstance() {
        return new FApplyDouble(f, vRange);
    }

    @Override
    protected void coreFit(Frame df) {
    }

    @Override
    public Frame apply(Frame df) {
        for (String name : varNames) {
            int varIndex = df.varIndex(name);
            for (int i = 0; i < df.rowCount(); i++) {
                df.setDouble(i, varIndex, f.apply(df.getDouble(i, varIndex)));
            }
        }
        return df;
    }
}
