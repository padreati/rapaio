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

import java.io.Serializable;

import rapaio.data.Var;
import rapaio.printer.Printable;

/**
 * Defines a variable transformation.
 * <p>
 * A variable transformation can take external parameters and also can fit parameters
 * from data for later use. Once a filter fit parameters it can be applied more than once
 * on data. However it cannot refit the parameters on other data. For that purpose
 * create a new variable transformation with {@link #newInstance()} and fit the new
 * transformation.
 * <p>
 * A good example is centering the data. Centering data implies that one computes a mean
 * value first from the data, and this step can be executed using {@link #fit(Var)} method.
 * <p>
 * After fitting parameter values from data, those parameters
 * can be used to apply filter to the same data or to
 * other possible data. This step is executed by calling
 * {@link #apply(Var)} method.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/3/14.
 */
public interface VarTransform extends Serializable, Printable {

    /**
     * Creates a new non fitted variable transformation with the
     * same external parameters.
     *
     * @return new instance of the variable transform
     */
    VarTransform newInstance();

    /**
     * Method for fitting eventual parameter values from given data
     *
     * @param var given data from which values to be fitted
     */
    VarTransform fit(Var var);

    /**
     * Transforms the given variable by applying the algorithm
     * of the filer using current filter parameters.
     *
     * @param var input variable
     * @return filtered/transformed variable
     */
    Var apply(Var var);

    /**
     * Method which allows one to make a single
     * call to predict filter parameters and apply those filter
     * on the same data in a single call.
     *
     * @param var input variable
     * @return filtered/transformed variable
     */
    default Var fapply(Var var) {
        fit(var);
        return apply(var);
    }
}
