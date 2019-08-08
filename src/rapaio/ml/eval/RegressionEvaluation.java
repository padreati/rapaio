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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import rapaio.core.*;
import rapaio.core.stat.*;
import rapaio.data.*;
import rapaio.data.filter.frame.*;
import rapaio.experiment.ml.eval.*;
import rapaio.ml.regression.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static rapaio.sys.WS.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/6/19.
 */
public class RegressionEvaluation {

    public static RegressionEvaluation instance() {
        return new RegressionEvaluation();
    }

    private Frame df;
    private String targetVarName;
    private RMetric metric;
    private Map<String, RegressionModel> regressionModels = new HashMap<>();
    private boolean debug = true;

    public RegressionEvaluation withFrame(Frame df) {
        this.df = df;
        return this;
    }

    public RegressionEvaluation withTarget(String targetVarName) {
        this.targetVarName = targetVarName;
        return this;
    }

    public RegressionEvaluation withMetric(RMetric metric) {
        this.metric = metric;
        return this;
    }

    public RegressionEvaluation withModel(String modelId, RegressionModel model) {
        regressionModels.put(modelId, model);
        return this;
    }

    public RegressionEvaluation withDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    private void validate() {
        Objects.requireNonNull(df, "Data frame was not configured.");
        Objects.requireNonNull(targetVarName, "Target variable name was not configured.");
        Objects.requireNonNull(metric, "Regression metric was not provided.");
        if (regressionModels.isEmpty()) {
            throw new IllegalStateException("No regression model was provided.");
        }
    }

    public CVResult cv(int folds) {

        validate();

        if (debug)
            print("\nCrossValidation with " + folds + " folds for models: " + String.join(",", regressionModels.keySet()) + "\n");

        List<IntList> strata = buildFolds(df, folds);
        CVResult cvResult = new CVResult(this, folds);

        for (int i = 0; i < folds; i++) {

            // build train and test data for current fold
            Mapping trainMapping = Mapping.empty();
            Mapping testMapping = Mapping.empty();
            for (int j = 0; j < folds; j++) {
                if (j == i) {
                    testMapping.addAll(strata.get(j));
                } else {
                    trainMapping.addAll(strata.get(j));
                }
            }
            Frame train = MappedFrame.byRow(df, trainMapping).copy();
            Frame test = MappedFrame.byRow(df, testMapping).copy();

            // iterate through models

            for (Map.Entry<String, RegressionModel> entry : regressionModels.entrySet()) {
                String modelId = entry.getKey();
                RegressionModel model = entry.getValue().newInstance();
                model.fit(train, targetVarName);
                RegressionResult result = model.predict(test);

                double value = metric.compute(test.rvar(targetVarName), result.firstPrediction());
                cvResult.putScore(modelId, i, value);

                if (debug)
                    print(String.format("model: %s CV %2d:  score=%.6f, mean=%.6f, se=%.6f\n",
                            modelId,
                            i + 1,
                            cvResult.getScore(modelId, i),
                            cvResult.getMean(modelId),
                            cvResult.getSE(modelId)));
            }
        }
        return cvResult;
    }

    public static class CVResult {
        private final Frame df;
        private final String targetVarName;
        private final RMetric metric;
        private final Map<String, RegressionModel> regressionModels;
        private final int folds;

        private final Map<String, VarDouble> metricMap;

        public CVResult(RegressionEvaluation parent, int folds) {
            this.df = parent.df;
            this.targetVarName = parent.targetVarName;
            this.metric = parent.metric;
            this.regressionModels = parent.regressionModels;
            this.folds = folds;
            this.metricMap = new HashMap<>();
            for (String modelId : parent.regressionModels.keySet()) {
                metricMap.put(modelId, VarDouble.empty(folds).withName(modelId));
            }
        }

        public Map<String, RegressionModel> getRegressionModels() {
            return regressionModels;
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

    private List<IntList> buildFolds(Frame df, int folds) {
        IntList shuffle = new IntArrayList();
        for (int i = 0; i < df.rowCount(); i++) {
            shuffle.add(i);
        }
        Collections.shuffle(shuffle, RandomSource.getRandom());
        List<IntList> foldMap = new ArrayList<>();
        for (int i = 0; i < folds; i++) {
            foldMap.add(new IntArrayList());
        }
        int currentFold = 0;
        for (int row : shuffle) {
            foldMap.get(currentFold).add(row);
            currentFold++;
            if (currentFold == folds) {
                currentFold = 0;
            }
        }
        return foldMap;
    }

}
