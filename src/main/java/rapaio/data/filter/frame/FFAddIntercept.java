/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

import rapaio.data.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adds an intercept column: a numeric column with all values equal with 1.0,
 * used in general for linear regression like setups.
 * <p>
 * In case there is already a column called intercept, nothing will happen.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class FFAddIntercept extends FFDefault {

    private static final long serialVersionUID = -7268280264499694765L;

    public static String INTERCEPT = "(Intercept)";

    public FFAddIntercept() {
        super(VRange.all());
    }

    @Override
    public FFAddIntercept newInstance() {
        return new FFAddIntercept();
    }

    @Override
    public void train(Frame df) {
    }

    public Frame apply(Frame df) {
        List<String> names = df.varStream().map(Var::name).collect(Collectors.toList());
        if (names.contains(INTERCEPT)) {
            return df;
        }
        Numeric intercept = Numeric.newFill(df.rowCount(), 1.0).withName(INTERCEPT);
        return SolidFrame.newByVars(intercept).bindVars(df);
    }
}
