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

package rapaio.ml.regression.linear;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.filter.FIntercept;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.regression.AbstractRegressionModel;
import rapaio.printer.Printable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/8/19.
 */
public abstract class BaseLinearRegressionModel<M extends BaseLinearRegressionModel<M>>
        extends AbstractRegressionModel<M, LinearRegressionResult<M>> implements Printable {

    private static final long serialVersionUID = 6171912601688633921L;

    // parameters

    protected boolean intercept = true;

    // learning artifacts

    protected DMatrix beta;

    /**
     * @return true if the linear model adds an intercept
     */
    public boolean hasIntercept() {
        return intercept;
    }

    /**
     * Configure the model to introduce an intercept or not.
     *
     * @param intercept if true an intercept variable will be generated, false otherwise
     * @return linear model instance
     */
    public BaseLinearRegressionModel withIntercept(boolean intercept) {
        this.intercept = intercept;
        return this;
    }

    public DVector firstCoefficients() {
        return beta.mapCol(0);
    }

    public DVector getCoefficients(int targetIndex) {
        return beta.mapCol(targetIndex);
    }

    public DMatrix allCoefficients() {
        return beta;
    }

    @Override
    protected FitSetup prepareFit(Frame df, Var weights, String... targetVarNames) {
        if (intercept) {
            return super.prepareFit(FIntercept.filter().apply(df), weights, targetVarNames);
        }
        return super.prepareFit(df, weights, targetVarNames);
    }

    @Override
    protected PredSetup preparePredict(Frame df, boolean withResiduals) {
        if (intercept) {
            return super.preparePredict(FIntercept.filter().apply(df), withResiduals);
        }
        return super.preparePredict(df, withResiduals);
    }

    @Override
    protected LinearRegressionResult<M> corePredict(Frame df, boolean withResiduals) {
        LinearRegressionResult<M> rp = new LinearRegressionResult<>((M)this, df, withResiduals);
        for (int i = 0; i < targetNames().length; i++) {
            String target = targetName(i);
            for (int j = 0; j < rp.prediction(target).rowCount(); j++) {
                double fit = 0.0;
                for (int k = 0; k < inputNames().length; k++) {
                    fit += beta.get(k, i) * df.getDouble(j, inputName(k));
                }
                rp.prediction(target).setDouble(j, fit);
            }
        }

        rp.buildComplete();
        return rp;
    }

    private String joinMax(int max, String[] tokens) {
        StringBuilder sb = new StringBuilder();
        int len = Math.min(tokens.length, max);
        for (int i = 0; i < len; i++) {
            sb.append(tokens[i]);
            if (i < len - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fullName());
        if (!isFitted()) {
            sb.append(", not fitted.");
        } else {
            sb.append(", fitted on: ")
                    .append(inputNames.length).append(" IVs [").append(joinMax(5, inputNames)).append("], ")
                    .append(targetNames.length).append(" DVs [").append(joinMax(5, targetNames)).append("].");
        }
        return sb.toString();
    }

    @Override
    public String toContent() {
        return toSummary();
    }

    @Override
    public String toFullContent() {
        return toSummary();
    }

}
