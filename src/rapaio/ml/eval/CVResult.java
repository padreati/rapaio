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

package rapaio.ml.eval;

import rapaio.core.stat.*;
import rapaio.data.*;
import rapaio.data.filter.frame.*;
import rapaio.ml.regression.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Container for the results of a cross validation evaluation on regression
 * models.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/13/19.
 */
public class CVResult {

    private final RegressionEval parent;
    private final int folds;
    private final Map<String, VarDouble> metricMap = new HashMap<>();

    protected CVResult(RegressionEval parent, int folds) {
        this.parent = parent;
        this.folds = folds;
        for (String modelId : parent.models.keySet()) {
            metricMap.put(modelId, VarDouble.empty(folds).withName(modelId));
        }
    }

    public Frame getFrame() {
        return parent.df;
    }

    public String getTargetName() {
        return parent.targetName;
    }

    public Map<String, RegressionModel> getModels() {
        return parent.models;
    }

    public boolean isDebug() {
        return parent.debug;
    }

    public int getFolds() {
        return folds;
    }

    public void putScore(String modelId, int fold, double score) {
        metricMap.get(modelId).setDouble(fold, score);
    }

    public double getScore(String modelId, int fold) {
        return metricMap.get(modelId).getDouble(fold);
    }

    public double getMean(String modelId) {
        return Mean.of(metricMap.get(modelId)).value();
    }

    public double getSE(String modelId) {
        return Variance.of(metricMap.get(modelId)).sdValue();
    }

    public Frame getSummaryFrame() {
        Var modelName = VarNominal.empty(0).withName("model");
        Var meanScore = VarDouble.empty().withName("mean");
        Var stdScore = VarDouble.empty().withName("se");
        for (String modelId : metricMap.keySet()) {
            modelName.addLabel(modelId);
            meanScore.addDouble(Mean.of(metricMap.get(modelId)).value());
            stdScore.addDouble(Variance.of(metricMap.get(modelId)).sdValue());
        }
        return SolidFrame.byVars(modelName, meanScore, stdScore).fapply(FRefSort.by(meanScore.refComparator()));
    }

}

