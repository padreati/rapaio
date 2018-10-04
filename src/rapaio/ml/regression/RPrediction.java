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

package rapaio.ml.regression;

import rapaio.core.stat.Mean;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.printer.Printable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.nCopies;

/**
 * Result of a regression predict.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/20/14.
 */
public class RPrediction implements Printable {
    protected final Regression model;
    protected final Frame df;
    protected final boolean withResiduals;
    protected final Map<String, VarDouble> fit;
    protected final Map<String, VarDouble> residuals;
    protected final Map<String, Double> tss;
    protected final Map<String, Double> ess;
    protected final Map<String, Double> rss;
    protected final Map<String, Double> rsquare;

    // static builder

    protected RPrediction(final Regression model, final Frame df, final boolean withResiduals) {
        this.df = df;
        this.model = model;
        this.withResiduals = withResiduals;

        this.fit = new HashMap<>();
        this.residuals = new HashMap<>();
        this.tss = new HashMap<>();
        this.ess = new HashMap<>();
        this.rss = new HashMap<>();
        this.rsquare = new HashMap<>();
        for (String targetName : model.targetNames()) {
            fit.put(targetName, VarDouble.empty(df.rowCount()).withName(targetName));
            residuals.put(targetName, VarDouble.empty(df.rowCount()).withName(targetName + "-residual"));
            tss.put(targetName, Double.NaN);
            ess.put(targetName, Double.NaN);
            rss.put(targetName, Double.NaN);
            rsquare.put(targetName, Double.NaN);
        }
    }

    // private constructor

    public static RPrediction build(Regression model, Frame df, boolean withResiduals) {
        return new RPrediction(model, df, withResiduals);
    }

    public Frame getFrame() {
        return df;
    }

    public boolean isWithResiduals() {
        return withResiduals;
    }

    /**
     * Returns target variables built at learning time
     *
     * @return target variable names
     */
    public String[] targetNames() {
        return model.targetNames();
    }

    /**
     * Returns first target variable built at learning time
     *
     * @return target variable names
     */
    public String firstTargetName() {
        return model.firstTargetName();
    }

    /**
     * Returns predicted target predict for each target variable name
     *
     * @return map with numeric variables as predicted values
     */
    public Map<String, VarDouble> fitMap() {
        return fit;
    }

    /**
     * Returns predicted target predict for each target variable name
     *
     * @return frame with fitted variables as columns
     */
    public Frame fitFrame() {
        return SolidFrame.byVars(Arrays.stream(targetNames()).map(fit::get).collect(Collectors.toList()));
    }

    /**
     * Returns fitted target var for first target variable name
     *
     * @return numeric variable with predicted values
     */
    public VarDouble firstFit() {
        return fit.get(firstTargetName());
    }

    /**
     * Returns fitted target values for given target variable name
     *
     * @param targetVar given target variable name
     * @return numeric variable with predicted values
     */
    public VarDouble fit(String targetVar) {
        return fit.get(targetVar);
    }

    public double firstRSquare() {
        return rSquare(firstTargetName());
    }

    public double rSquare(String targetVar) {
        return rsquare.get(targetVar);
    }

    public double firstTss() {
        return tss(firstTargetName());
    }

    public double tss(String targetVar) {
        return tss.get(targetVar);
    }

    public double firstEss() {
        return ess(firstTargetName());
    }

    public double ess(String targetVar) {
        return ess.get(targetVar);
    }

    public double firstRss() {
        return rss(firstTargetName());
    }

    public double rss(String targetVar) {
        return rss.get(targetVar);
    }

    public Map<String, VarDouble> residualsMap() {
        return residuals;
    }

    public Frame residualsFrame() {
        return SolidFrame.byVars(Arrays.stream(targetNames()).map(residuals::get).collect(Collectors.toList()));
    }

    public VarDouble firstResidual() {
        return residuals.get(firstTargetName());
    }

    public VarDouble residual(String targetVar) {
        return residuals.get(targetVar);
    }

    public void buildComplete() {
        if (withResiduals) {
            for (String target : targetNames()) {
                for (int i = 0; i < df.rowCount(); i++) {
                    residuals.get(target).setDouble(i, df.getDouble(i, target) - fit(target).getDouble(i));
                }

                double mu = Mean.of(df.rvar(target)).value();
                double tssValue = 0;
                double essValue = 0;
                double rssValue = 0;

                for (int i = 0; i < df.rowCount(); i++) {
                    tssValue += Math.pow(df.getDouble(i, target) - mu, 2);
                    essValue += Math.pow(fit(target).getDouble(i) - mu, 2);
                    rssValue += Math.pow(df.getDouble(i, target) - fit(target).getDouble(i), 2);
                }

                tss.put(target, tssValue);
                ess.put(target, essValue);
                rss.put(target, rssValue);
                rsquare.put(target, 1 - rssValue / tssValue);
            }
        }
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();

        sb.append(model.headerSummary());
        sb.append("\n");

        for (String target : model.targetNames()) {
            sb.append("Fit and residuals for ").append(target).append("\n");
            sb.append("======================")
                    .append(String.join("", nCopies(target.length(), "=")));

            String fullSummary = SolidFrame.byVars(fit(target), residual(target)).summary();
            List<String> list = Arrays.stream(fullSummary.split("\n")).skip(8).collect(Collectors.toList());
            int pos = 0;
            for (String line : list) {
                pos++;
                if (line.trim().isEmpty()) {
                    break;
                }
            }
            sb.append(list.stream().collect(Collectors.joining("\n", "", "\n")));

            double max = Math.max(Math.max(tss.get(target), ess.get(target)), rss.get(target));
            int dec = 1 + 3;
            while (max > 1) {
                dec++;
                max /= 10;
            }

            sb.append(String.format("Total sum of squares     (TSS) : %" + dec + ".3f\n", tss.get(target)));
            sb.append(String.format("Explained sum of squares (ESS) : %" + dec + ".3f\n", ess.get(target)));
            sb.append(String.format("Residual sum of squares  (RSS) : %" + dec + ".3f\n", rss.get(target)));
            sb.append("\n");
            sb.append(String.format("Coeff. of determination  (R^2) : %" + dec + ".3f\n", 1 - rss.get(target) / tss.get(target)));
            sb.append("\n");
        }

        return sb.toString();
    }
}
