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

package rapaio.data.transform;

import java.io.Serial;
import java.util.List;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;

/**
 * Adds an intercept: a numeric variable with all values equal with 1.0 used in general for regression setups.
 * <p>
 * In case there is already a column called intercept, nothing will happen.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class AddIntercept extends AbstractTransform {

    public static AddIntercept transform() {
        return new AddIntercept();
    }

    @Serial
    private static final long serialVersionUID = -7268280264499694765L;
    public static final String INTERCEPT = "(Intercept)";

    private AddIntercept() {
        super(VarRange.all());
    }

    @Override
    public AddIntercept newInstance() {
        return new AddIntercept();
    }

    @Override
    public void coreFit(Frame df) {
    }

    @Override
    public Frame coreApply(Frame df) {
        List<String> names = df.varStream().map(Var::name).toList();
        if (names.contains(INTERCEPT)) {
            return df;
        }
        VarDouble intercept = VarDouble.fill(df.rowCount(), 1.0).name(INTERCEPT);
        return SolidFrame.byVars(intercept).bindVars(df);
    }
}
