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

package rapaio.data.sample;

import rapaio.data.Frame;
import rapaio.data.Var;

import java.io.Serializable;

/**
 * Interface which defines a function which produces a data frame with a sample
 * of rows from the given data frame.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/29/15.
 */
public interface RowSampler extends Serializable {

    /**
     * @return name of the row sampling technique, if there is one.
     */
    String name();

    /**
     * Builds a new sample from the given data frame
     *
     * @param df source data frame
     * @return new sample
     */
    Sample nextSample(Frame df, Var weight);

    static RowSampler identity() {
        return new Identity();
    }

    static RowSampler bootstrap() {
        return new Bootstrap(1.0);
    }

    static RowSampler bootstrap(double p) {
        return new Bootstrap(p);
    }

    static RowSampler subsample(double p) {
        return new SubSampler(p);
    }
}

