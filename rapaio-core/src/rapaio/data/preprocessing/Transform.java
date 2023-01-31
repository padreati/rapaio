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

import java.io.Serializable;

import rapaio.data.Frame;
import rapaio.data.Preprocessing;

/**
 * Transform data frame into another one with added value.
 * <p>
 * {@link Transform} is implemented by all frame transformation useful in the process
 * of feature engineering. The transformations are collected into {@link Preprocessing} collections
 * whih could be used to tranform data into features.
 * <p>
 * The two main operations a transform has are fit and apply. In the fit operation the transformation
 * finalize its process of fitting eventual parameter values computed from data. In the apply operation
 * it transforms the data, using eventually fitted parameters from the fit step. To keep a clean environment
 * the following constraints are enforced:
 *
 * <ul>
 * <li>apply operation cannot be called without a previous call to fit</li>
 * <li>once a fit operation was called, you cannot fit again on other data; in order to use
 * the same transformation for another data fit one has to call {@link #newInstance()} which
 * creates a new unfitted transform</li>
 * <li>one can call {@link #fapply(Frame)} to call fit and apply subsequently; still the transformation
 * cannot be previously fitted</li>
 * <li>once the transform is fitted either by {@link #fit(Frame)} or {@link #fapply(Frame)}, the transformation
 * can be applied any number of times</li>
 * <li>also, if the transformation is fitted, it can be serialized and deserialized for later usage</li>
 * </ul>
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public interface Transform extends Serializable {

    /**
     * @return an array with variable names which describes the domain of this filter
     */
    String[] varNames();

    /**
     * Train the transformer on a data frame. Note that in this function a transformer learns its domain using
     * {@link rapaio.data.VarRange} fitted to df. This function handles also various artifacts
     * required by a transform. A trained transformer can be applied multiple times on different data frames,
     * using the same trained transformation.
     *
     * @param df given data frame
     */
    void fit(Frame df);

    /**
     * Apply trained transformation to the given data frame.
     *
     * @param df given data frame
     * @return the transformed frame
     */
    Frame apply(Frame df);

    /**
     * A chained call to fit and apply methods.
     *
     * This method is equivalent to calling {@link #fit(Frame)} and {@link #apply(Frame)} in chain.
     *
     * @param df given data frame
     * @return transformed data frame
     */
    default Frame fapply(Frame df) {
        fit(df);
        return apply(df);
    }

    /**
     * Builds a new instance of the transformer without trained artifacts, but with the same parameters
     * as the original transformation. One needs to use this method if he wants to have multiple instances
     * of the same transformations fitted to different data.
     *
     * @return new filter with same parameters
     */
    Transform newInstance();
}
