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
import java.util.HashMap;
import java.util.Map;

import rapaio.data.Frame;
import rapaio.data.VarRange;

/**
 * Transform numeric variables into standardized values.
 * <p>
 * The transformation is f(x) = (x-mu)/sd
 * <p>
 * where
 * <p>
 * mu is the mean of the values
 * sd is the standard deviation
 * <p>
 * Take care that the filter works, as usual, on the same variables, thus if you want
 * to not alter the original vector you have to pass to the filter a solid copy
 * of the original vector.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/30/15.
 */
public class StandardScaler extends AbstractTransform {

    public static StandardScaler on(VarRange varRange) {
        return new StandardScaler(varRange);
    }

    @Serial
    private static final long serialVersionUID = -2447577449010618416L;
    private final Map<String, VarStandardScaler> filters = new HashMap<>();

    private StandardScaler(VarRange varRange) {
        super(varRange);
    }

    @Override
    public StandardScaler newInstance() {
        return new StandardScaler(varRange);
    }

    @Override
    public void coreFit(Frame df) {
        filters.clear();
        for (String varName : varNames) {
            VarStandardScaler filter = VarStandardScaler.filter();
            filter.fit(df.rvar(varName));
            filters.put(varName, filter);
        }
    }

    @Override
    public Frame coreApply(Frame df) {
        for (String varName : df.varNames()) {
            if (filters.containsKey(varName)) {
                filters.get(varName).apply(df.rvar(varName));
            }
        }
        return df;
    }
}
