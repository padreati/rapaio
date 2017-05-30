/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.ml.regression.ensemble;

import rapaio.data.Frame;
import rapaio.data.NumericVar;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.data.sample.Sample;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.AbstractRegression;
import rapaio.ml.regression.RFit;
import rapaio.ml.regression.Regression;
import rapaio.ml.regression.tree.RTree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/15/15.
 */
public class RForest extends AbstractRegression {

    private static final long serialVersionUID = -3926256335736143438L;

    private Regression r = RTree.buildC45();
    private List<Regression> regressors = new ArrayList<>();

    public static RForest newRF() {
        return new RForest();
    }

    private RForest() {
    }

    @Override
    public Regression newInstance() {
        return new RForest();
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
        return new Capabilities()
                .withInputCount(1, 1_000_000)
                .withTargetCount(1, 1)
                .withInputTypes(VarType.BINARY, VarType.INDEX, VarType.NUMERIC, VarType.ORDINAL, VarType.NOMINAL)
                .withTargetTypes(VarType.NUMERIC)
                .withAllowMissingInputValues(true)
                .withAllowMissingTargetValues(false);
    }

    public RForest withRegression(Regression r) {
        this.r = r;
        return this;
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {
        regressors.clear();
        IntStream.range(0, runs()).forEach(i -> {
                Regression rnew = r.newInstance();
                Sample sample = sampler().nextSample(df, weights);
                rnew.train(sample.df, sample.weights, firstTargetName());
                regressors.add(rnew);
                if (runningHook() != null) {
                    runningHook().accept(this, i + 1);
                }
        });
        return true;
    }

    public List<Regression> getRegressors() {
        return regressors;
    }

    @Override
    protected RFit coreFit(Frame df, boolean withResiduals) {
        RFit fit = RFit.build(this, df, withResiduals);
        List<NumericVar> results = regressors
                .parallelStream()
                .map(r -> r.fit(df, false).firstFit())
                .collect(Collectors.toList());
        for (int i = 0; i < df.getRowCount(); i++) {
            double sum = 0;
            for (NumericVar result : results) {
                sum += result.getValue(i);
            }
            fit.firstFit().setValue(i, sum / regressors.size());
        }
        if (withResiduals)
            fit.buildComplete();
        return fit;
    }

    @Override
    public String getSummary() {
        throw new IllegalArgumentException("not implemented");
    }
}
