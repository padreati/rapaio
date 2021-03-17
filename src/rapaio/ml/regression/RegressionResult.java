/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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
import rapaio.printer.Format;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.nCopies;

/**
 * Result of a regression prediction.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/20/14.
 */
public class RegressionResult implements Printable {

    public static RegressionResult build(RegressionModel model, Frame df, boolean withResiduals, double[] quantiles) {
        return new RegressionResult(model, df, withResiduals, quantiles);
    }

    protected final RegressionModel model;
    protected final Frame df;
    protected final boolean withResiduals;
    protected final double[] quantiles;
    protected final Map<String, VarDouble> prediction;
    protected final Map<String, VarDouble[]> predictionQuantiles;
    protected final Map<String, VarDouble> residuals;
    protected final Map<String, Double> tss;
    protected final Map<String, Double> ess;
    protected final Map<String, Double> rss;
    protected final Map<String, Double> rsquare;

    protected RegressionResult(final RegressionModel model, final Frame df, final boolean withResiduals, final double[] quantiles) {
        this.df = df;
        this.model = model;
        this.withResiduals = withResiduals;
        this.quantiles = quantiles != null ? quantiles : new double[0];

        this.prediction = new HashMap<>();
        this.predictionQuantiles = new HashMap<>();
        this.residuals = new HashMap<>();
        this.tss = new HashMap<>();
        this.ess = new HashMap<>();
        this.rss = new HashMap<>();
        this.rsquare = new HashMap<>();
        for (String targetName : model.targetNames()) {
            prediction.put(targetName, VarDouble.empty(df.rowCount()).name(targetName));
            VarDouble[] pq = new VarDouble[this.quantiles.length];
            for (int i = 0; i < this.quantiles.length; i++) {
                pq[i] = VarDouble.empty(df.rowCount()).name(targetName + "_q" + Format.floatFlexShort(this.quantiles[i]));
            }
            predictionQuantiles.put(targetName, pq);
            residuals.put(targetName, VarDouble.empty(df.rowCount()).name(targetName));
            tss.put(targetName, Double.NaN);
            ess.put(targetName, Double.NaN);
            rss.put(targetName, Double.NaN);
            rsquare.put(targetName, Double.NaN);
        }
    }

    // private constructor

    public RegressionModel getModel() {
        return model;
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
    public Map<String, VarDouble> predictionMap() {
        return prediction;
    }

    /**
     * Returns predicted target predict for each target variable name
     *
     * @return frame with fitted variables as columns
     */
    public Frame predictionFrame() {
        return SolidFrame.byVars(Arrays.stream(targetNames()).map(prediction::get).collect(Collectors.toList()));
    }

    /**
     * Returns fitted target var for first target variable name
     *
     * @return numeric variable with predicted values
     */
    public VarDouble firstPrediction() {
        return prediction.get(firstTargetName());
    }

    /**
     * Returns fitted target values for given target variable name
     *
     * @param targetVar given target variable name
     * @return numeric variable with predicted values
     */
    public VarDouble prediction(String targetVar) {
        return prediction.get(targetVar);
    }

    /**
     * Returns predicted quantile target predict for each target variable name
     *
     * @return map with numeric variables as predicted values
     */
    public Map<String, VarDouble[]> predictionQuantilesMap() {
        return predictionQuantiles;
    }

    /**
     * Returns predicted quantiles target predict for all target variable name
     *
     * @return frame with fitted variables as columns
     */
    public Frame predictionQuantilesFrame() {
        VarDouble[] array = new VarDouble[targetNames().length * quantiles.length];
        int pos = 0;
        for (int i = 0; i < targetNames().length; i++) {
            for (int j = 0; j < quantiles.length; j++) {
                array[pos++] = predictionQuantiles.get(targetNames()[i])[j];
            }
        }
        return SolidFrame.byVars(array);
    }

    /**
     * Returns fitted quantiles target var for first target variable name
     *
     * @return numeric variable with predicted values
     */
    public VarDouble[] firstPredictionQuantiles() {
        return predictionQuantiles.get(firstTargetName());
    }

    /**
     * Returns fitted quantiles target values for given target variable name
     *
     * @param targetVar given target variable name
     * @return numeric variable with predicted values
     */
    public VarDouble[] predictionQuantiles(String targetVar) {
        return predictionQuantiles.get(targetVar);
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
                    residuals.get(target).setDouble(i, df.getDouble(i, target) - prediction(target).getDouble(i));
                }

                double mu = Mean.of(df.rvar(target)).value();
                double tssValue = 0;
                double essValue = 0;
                double rssValue = 0;

                for (int i = 0; i < df.rowCount(); i++) {
                    tssValue += Math.pow(df.getDouble(i, target) - mu, 2);
                    essValue += Math.pow(prediction(target).getDouble(i) - mu, 2);
                    rssValue += Math.pow(df.getDouble(i, target) - prediction(target).getDouble(i), 2);
                }

                tss.put(target, tssValue);
                ess.put(target, essValue);
                rss.put(target, rssValue);
                rsquare.put(target, 1 - rssValue / tssValue);
            }
        }
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();

        sb.append(model.headerSummary());
        sb.append("\n");

        for (String target : model.targetNames()) {
            sb.append("Fit and residuals for ").append(target).append("\n");
            sb.append("======================")
                    .append(String.join("", nCopies(target.length(), "="))).append('\n');

            String fullSummary = SolidFrame.byVars(prediction(target), residual(target)).toSummary(printer, options);
            List<String> list = Arrays.stream(fullSummary.split("\n")).skip(10).collect(Collectors.toList());
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
