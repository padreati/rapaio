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

package rapaio.experiment.ml.classifier.bayes.data;

import rapaio.data.Frame;
import rapaio.experiment.ml.classifier.bayes.estimator.BinaryEstimator;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class BinaryData extends NaiveBayesData implements Serializable {

    private static final long serialVersionUID = 2898972777354002992L;

    public BinaryEstimator binEstimator;
    public Map<String, BinaryEstimator> binMap;

    public BinaryData(BinaryEstimator binEstimator) {
        this.binEstimator = binEstimator;
    }

    public Set<String> keySet() {
        return binMap.keySet();
    }

    @Override
    public double calcSumLog(String testCol, Frame df, int i, String firstTargetLevel) {
        return binMap.get(testCol).cpValue(df.getLabel(i, testCol), firstTargetLevel);
    }
}