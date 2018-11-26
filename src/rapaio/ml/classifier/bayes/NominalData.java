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

package rapaio.ml.classifier.bayes;

import rapaio.data.Frame;
import rapaio.ml.classifier.bayes.estimator.NominalEstimator;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class NominalData extends NaiveBayesData implements Serializable {

    private static final long serialVersionUID = 6894116115802577531L;

    public NominalEstimator nomEstimator;
    public Map<String, NominalEstimator> nomMap;

    public NominalData(NominalEstimator nomEstimator) {
        this.nomEstimator = nomEstimator;
    }

    public Set<String> keySet() {
        return nomMap.keySet();
    }

    @Override
    public double calcSumLog(String testCol, Frame df, int i, String firstTargetLevel) {
        return nomMap.get(testCol).cpValue(df.getLabel(i, testCol), firstTargetLevel);
    }
}