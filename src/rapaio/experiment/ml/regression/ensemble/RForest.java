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

package rapaio.experiment.ml.regression.ensemble;

import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.sample.Sample;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.AbstractRegressionModel;
import rapaio.ml.regression.RegressionModel;
import rapaio.ml.regression.RegressionResult;
import rapaio.ml.regression.tree.RTree;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/15/15.
 */
public class RForest extends AbstractRegressionModel<RForest, RegressionResult<RForest>>
        implements Printable {

    private static final long serialVersionUID = -3926256335736143438L;

    // parameter

    private RegressionModel r = RTree.newC45();

    // learning artifacts

    private List<RegressionModel> regressors = new ArrayList<>();

    public static RForest newRF() {
        return new RForest();
    }

    private RForest() {
    }

    @Override
    public RForest newInstance() {
        return newInstanceDecoration(new RForest())
                .withRegression(this.r);
    }

    @Override
    public String name() {
        return "RForest";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("\n");
        sb.append("{\n");
        sb.append("r=").append(r.fullName()).append(",\n");
        sb.append("runs=").append(runs()).append(",\n");
        sb.append("}\n");
        return sb.toString();
    }

    @Override
    public Capabilities capabilities() {
        return Capabilities.builder()
                .minInputCount(1).maxInputCount(1_000_000)
                .minTargetCount(1).maxTargetCount(1)
                .inputTypes(Arrays.asList(VType.BINARY, VType.INT, VType.DOUBLE, VType.NOMINAL))
                .targetType(VType.DOUBLE)
                .allowMissingInputValues(true)
                .allowMissingTargetValues(false)
                .build();
    }

    public RForest withRegression(RegressionModel r) {
        this.r = r;
        return this;
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        regressors.clear();
        IntStream.range(0, runs()).forEach(i -> {
            RegressionModel rnew = r.newInstance();
            Sample sample = sampler().nextSample(df, weights);
            rnew.fit(sample.df, sample.weights, firstTargetName());
            regressors.add(rnew);
            if (runningHook() != null) {
                runningHook().accept(this, i + 1);
            }
        });
        return true;
    }

    public List<RegressionModel> getRegressors() {
        return regressors;
    }

    @Override
    protected RegressionResult<RForest> corePredict(Frame df, boolean withResiduals) {
        RegressionResult<RForest> fit = RegressionResult.build(this, df, withResiduals);
        List<VarDouble> results = regressors
                .parallelStream()
                .map(r -> r.predict(df, false).firstPrediction())
                .collect(Collectors.toList());
        for (int i = 0; i < df.rowCount(); i++) {
            double sum = 0;
            for (VarDouble result : results) {
                sum += result.getDouble(i);
            }
            fit.firstPrediction().setDouble(i, sum / regressors.size());
        }
        if (withResiduals)
            fit.buildComplete();
        return fit;
    }

    @Override
    public String toSummary(Printer printer, POption... options) {
        throw new IllegalArgumentException("not implemented");
    }
}
