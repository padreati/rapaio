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
 */

package rapaio.ml.regressor;

import rapaio.core.Printable;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Result of a regression fit.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/20/14.
 */
public class RegressorFit implements Printable {
    private final Regressor model;
    private final Frame df;
    private final List<String> targetVars;
    private final boolean withResiduals;
    private final Map<String, Numeric> fit;
    private final Map<String, Numeric> residuals;

    // builder

    public static RegressorFit newEmpty(Regressor model, Frame df, boolean withResiduals) {
        return new RegressorFit(model, df, withResiduals);
    }

    // private constructor

    protected RegressorFit(final Regressor model, final Frame df, final boolean withResiduals) {
        this.model = model;
        this.df = df;
        this.targetVars = new ArrayList<>();
        this.withResiduals = withResiduals;

        this.fit = new HashMap<>();
        this.residuals = new HashMap<>();
    }

    public RegressorFit addTarget(String targetName) {
        targetVars.add(targetName);
        fit.put(targetName, Numeric.newEmpty(df.rowCount()).withName(targetName));
        if (withResiduals) {
            residuals.put(targetName, Numeric.newEmpty(df.rowCount()).withName(targetName + "-residual"));
        }
        return this;
    }

    public Frame getFrame() {
        return df;
    }

    public int getRows() {
        return df.rowCount();
    }

    /**
     * Returns target variables built at learning time
     *
     * @return target variable names
     */
    public String[] targetVars() {
        return targetVars.toArray(new String[targetVars.size()]);
    }

    /**
     * Returns first target variable built at learning time
     *
     * @return target variable names
     */
    public String firstTargetVar() {
        return targetVars.get(0);
    }

    /**
     * Returns predicted target fit for each target variable name
     *
     * @return map with numeric variables as predicted values
     */
    public Map<String, Numeric> fitMap() {
        return fit;
    }

    /**
     * Returns predicted target fit for each target variable name
     *
     * @return frame with fitted variables as columns
     */
    public Frame fitFrame() {
        return SolidFrame.newWrapOf(targetVars.stream().map(fit::get).collect(Collectors.toList()));
    }

    /**
     * Returns fitted target var for first target variable name
     *
     * @return numeric variable with predicted values
     */
    public Numeric firstFit() {
        return fit.get(firstTargetVar());
    }

    /**
     * Returns fitted target values for given target variable name
     *
     * @param targetVar given target variable name
     * @return numeric variable with predicted values
     */
    public Numeric fit(String targetVar) {
        return fit.get(targetVar);
    }

    public Map<String, Numeric> residualsMap() {
        return residuals;
    }

    public Frame residualsFrame() {
        return SolidFrame.newWrapOf(targetVars.stream().map(residuals::get).collect(Collectors.toList()));
    }

    public Numeric firstResidual() {
        return residuals.get(firstTargetVar());
    }

    public Numeric residual(String targetVar) {
        return residuals.get(targetVar);
    }

    public void buildComplete() {
        if (withResiduals) {
            for (String targetVar : targetVars) {
                for (int i = 0; i < df.rowCount(); i++) {
                    residuals.get(targetVar).setValue(i, df.getVar(targetVar).value(i) - fit(targetVar).value(i));
                }
            }
        }
    }

    @Override
    public void buildSummary(StringBuilder sb) {

        sb.append("> Regression Result Summary").append("\n");
        sb.append("  =========================\n");
        sb.append("\n");

        sb.append("Model type: ").append(model.name()).append("\n");
        sb.append("Model instance: ").append(model.fullName()).append("\n");
        sb.append("\n");

        sb.append("Predicted frame summary:\n");
        sb.append("- rows: ").append(df.rowCount()).append("\n");
        sb.append("- vars: ").append(df.varCount()).append("\n");
        sb.append("- targets: ").append(Arrays.deepToString(model.targetNames())).append("\n");
        sb.append("\n");

        sb.append("Regression results:").append("\n");
        sb.append("TO BE DONE").append("\n");
    }
}
