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

package rapaio.ml.regressor;

import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
import rapaio.printer.Printable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Result of a regression fit.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/20/14.
 */
public class RFit implements Printable {
    private final Regression model;
    private final Frame df;
    private final List<String> targetVars;
    private final boolean withResiduals;
    private final Map<String, Numeric> fit;
    private final Map<String, Numeric> residuals;

    // builder

    protected RFit(final Regression model, final Frame df, final boolean withResiduals) {
        this.model = model;
        this.df = df;
        this.targetVars = new ArrayList<>();
        this.withResiduals = withResiduals;

        this.fit = new HashMap<>();
        this.residuals = new HashMap<>();
    }

    // private constructor

    public static RFit newEmpty(Regression model, Frame df, boolean withResiduals) {
        return new RFit(model, df, withResiduals);
    }

    public RFit addTarget(String targetName) {
        targetVars.add(targetName);
        fit.put(targetName, Numeric.empty(df.rowCount()).withName(targetName));
        if (withResiduals) {
            residuals.put(targetName, Numeric.empty(df.rowCount()).withName(targetName + "-residual"));
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
                    residuals.get(targetVar).setValue(i, df.var(targetVar).value(i) - fit(targetVar).value(i));
                }
            }
        }
    }

    @Override
    public String summary() {

        return "\n" +
                "> Regression Result Summary\n" +
                "=========================\n" +
                "\n" +
                "Model type: " + model.name() + "\n" +
                "Model instance: " + model.fullName() + "\n" +
                "\n" +
                "Predicted frame printSummary:\n" +
                "- rows: " + df.rowCount() + "\n" +
                "- vars: " + df.varCount() + "\n" +
                "- targets: " + Arrays.deepToString(model.targetNames()) + "\n" +
                "\n" +
                "Regression results:" + "\n" +
                "TO BE DONE" + "\n";
    }
}
