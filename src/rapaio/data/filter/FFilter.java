/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.filter;

import rapaio.data.Frame;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public interface FFilter extends Serializable {

    /**
     * @return an array with variable names which describes the domain of this filter
     */
    String[] varNames();

    /**
     * Builds a new filter from a data frame. Note that
     * in this function a filter learns it's domain using var range
     * fitted to df. This function handles also various artifacts
     * required by a filter. Thus a filter after is trained can be applied
     * multiple times on different data frames, using the same
     * trained transformation.
     *
     * @param df given data frame
     */
    void fit(Frame df);

    /**
     * Apply trained transformation to the given data frame.
     * Whenever is possible the returned frame is the same as the original
     * or a frame referenced by the original. This is done for performance
     * and flexibility reasons. If you want to not alter the original frame
     * you have to pass a solid copy of the original frame.
     *
     * @param df given data frame
     * @return the transformed frame
     */
    Frame apply(Frame df);

    /**
     * A chained call to predict and apply methods.
     *
     * @param df given data frame
     * @return transformed data frame
     */
    default Frame fapply(Frame df) {
        fit(df);
        return apply(df);
    }

    /**
     * Builds a new instance of the filter without
     * trained artifacts but with the same parameters
     * as the original filter.
     *
     * @return new filter with same parameters
     */
    FFilter newInstance();
}
