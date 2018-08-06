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

package rapaio.experiment.ml.feature.generator;

import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.Var;

import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/28/18.
 */
public interface FeatureGroupGenerator {

    /**
     * @return the range on which the feature generator will be applied
     */
    VRange getVRange();

    /**
     * Build the grouping artifacts required to generate feature
     * @param source source frame
     * @param keys variable names which are considered keys
     */
    void fit(Frame source, List<String> keys);

    /**
     * Generate variables used to decorate given frame.
     * Will produce a list of variable with the same rows as df
     * and with values corresponding identified by keys.
     *
     * @param df frame which will be decorated
     * @return generated features
     */
    List<Var> generate(Frame df, List<String> keys);

    static FeatureGroupGenerator nomLabelCount(VRange range, boolean normalized) {
        return new NomLabelCount(range, normalized);
    }

    static FeatureGroupGenerator numVarStat(VRange range, List<NumValueSummary.Aggregate> aggregates) {
        return new NumValueSummary(range, aggregates);
    }
}
