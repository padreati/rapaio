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

import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.filter.var.VFStandardize;

import java.util.HashMap;
import java.util.Map;

/**
 * Transform numeric variables into standardized values.
 *
 * The transformation is f(x) = (x-mu)/sd
 *
 * where
 *
 * mu is the mean of the values
 * sd is the standard deviation
 *
 * Take care that the filter works, as usual, on the same variables, thus if you want
 * to not alter the original vector you have to pass to the filter a solid copy
 * of the original vector.
 *
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/30/15.
 */
public class FFStandardize extends AbstractFF {

    private static final long serialVersionUID = -2447577449010618416L;

    Map<String, VFStandardize> filters = new HashMap<>();

    public FFStandardize(String...varNames) {
        super(VRange.of(varNames));
    }

    public FFStandardize(VRange vRange) {
        super(vRange);
    }

    @Override
    public FFStandardize newInstance() {
        return new FFStandardize(vRange);
    }

    @Override
    public void train(Frame df) {
        parse(df);
        filters.clear();
        for (String varName : varNames) {
            VFStandardize filter = new VFStandardize();
            filter.fit(df.getVar(varName));
            filters.put(varName, filter);
        }
    }

    @Override
    public Frame apply(Frame df) {
       for (String varName : df.getVarNames()) {
            if (filters.containsKey(varName)) {
                filters.get(varName).apply(df.getVar(varName));
            }
        }
        return df;
    }
}
