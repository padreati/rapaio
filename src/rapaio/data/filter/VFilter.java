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

package rapaio.data.filter;

import rapaio.printer.Printable;
import rapaio.data.Var;

import java.io.Serializable;

/**
 * This defines a filter for variables.
 * <p>
 * A filter is a transformation of one or multiple variables into
 * a single output variable. A filter can
 * take any number of parameters given at construction time.
 * However, it is possible for a filter to tune its parameters
 * using data, and save those values for later use.
 * <p>
 * A good example is centering the data. Centering data implies
 * that one computes a mean value first from the data, and this
 * step can be executed using {@link #fit(Var...)} method.
 * <p>
 * After fitting parameter values from data, those parameters
 * can be used to apply filter to the same data or to
 * other possible data. This step is executed by calling
 * {@link #apply(Var...)} method.
 * <p>
 * However, ofter a filter is used only to alter the data into a single
 * step. For this purpose one can use {@link VFilter#fitApply(Var...)} method
 * which executes both steps (fit and apply) with a single method call.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/3/14.
 */
public interface VFilter extends Serializable, Printable {

    /**
     * Method for fitting eventual parameter values from given data
     *
     * @param vars given data from which values to be fitted
     */
    void fit(Var... vars);

    /**
     * Transforms the given variable by applying the algorithm
     * of the filer using current filter parameters.
     *
     * @param vars input variables
     * @return filtered/transformed variable
     */
    Var apply(Var... vars);

    /**
     * Method which allows one to make a single
     * call to fit filter parameters and apply those filter
     * on the same data in a single call.
     *
     * @param vars input variable
     * @return filtered/transformed variable
     */
    default Var fitApply(Var... vars) {
        fit(vars);
        return apply(vars);
    }

    @Override
    default String getSummary() {
        return "filter summary not implemented";
    }
}
