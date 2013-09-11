/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

package rapaio.tutorial.pages;

import java.io.IOException;

import rapaio.correlation.PearsonRCorrelation;
import rapaio.data.Frame;
import rapaio.data.OneIndexVector;
import rapaio.datasets.Datasets;
import static rapaio.explore.Workspace.*;
import static rapaio.explore.Summary.*;
import rapaio.filters.ColFilters;
import static rapaio.filters.NumericFilters.*;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Points;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CorrelationsPage implements TutorialPage {

    @Override
    public String getPageName() {
        return "Correlations";
    }

    @Override
    public String getPageTitle() {
        return "Correlations";
    }

    @Override
    public void render() throws IOException {

        heading(2, "Introduction");
        p("This tutorial presents you the correlations tools offered by Rapaio library.");

        p("We will use the classical iris data set. The numerical columns of this dataset are:");

        Frame df = Datasets.loadIrisDataset();
        df = ColFilters.retainNumeric(df);

        names(df);

        heading(2, "Pearson product-moment correlation");

        p("Pearson product-moment correlation measures the linear " +
                "correlation between two random variables. Among other " +
                "type of correlation measures, the Pearson product-moment " +
                "detects only linear correlations. ");

        heading(4, "Definition:");

        p("Pearson product-moment coefficient measures the linear correlation " +
                "between two random variables \\(X\\) and \\(Y\\), " +
                "giving a value between +1 and −1 inclusive, " +
                "where 1 is total positive correlation, " +
                "0 is no correlation, and −1 is negative correlation.");

        p("Pearson's correlation coefficient when applied to a population " +
                "is commonly represented by the Greek letter \\(\rho\\) (rho) " +
                "and may be referred to as the population correlation " +
                "coefficient or the population Pearson correlation " +
                "coefficient. " +
                "The formula for \\(\\rho\\) is:");

        eqn("\\rho_{X,Y} = \\frac{cov(X,Y)}{\\sigma_X\\sigma_Y} = \\frac{E[(X-\\mu_X)(Y-\\mu_Y)]}{\\sigma_X\\sigma_Y}");

        p("Pearson's correlation coefficient when applied to a " +
                "sample is commonly represented by the " +
                "letter \\(r\\) and may be referred to as " +
                "the sample correlation coefficient or " +
                "the sample Pearson correlation coefficient. " +
                "We can obtain a formula for \\(r\\) by substituting estimates " +
                "of the covariances and variances based on a sample " +
                "into the formula above. That formula for \\(r\\) is:");

        eqn("r = \\frac{\\sum ^n _{i=1}(X_i-\\bar{X})(Y_i-\\bar{Y})}{\\sqrt{\\sum ^n _{i=1}(X_i - \\bar{X})^2} " +
                "\\sqrt{\\sum ^n _{i=1}(Y_i - \\bar{Y})^2}}");

        p("The interpretation of a correlation coefficient depends " +
                "on the context and purposes. A correlation of 0.8 " +
                "may be very low if one is verifying a physical law " +
                "using high-quality instruments, but may be regarded " +
                "as very high in the social sciences where there may be " +
                "a greater contribution from complicating factors.");

        heading(2, "Usa Rapaio for Pearson correlation");

        p("Rapaio library allows one to compute Pearson \\(r\\) for more then one vector at a time. " +
                "Thus the result will be a matrix with computed \\(r\\) values between vectors," +
                "using vectors index position as indexes in resulted matrix. ");

        code("        PearsonRCorrelation corr = new PearsonRCorrelation(df);\n" +
                "        summary(corr);\n");
        PearsonRCorrelation corr = new PearsonRCorrelation(df);
        summary(corr);

        p("We can spot with eas that many of the attributes are " +
                "linearly correlated. As a sample we find from the " +
                "correlation summary that petal-length and petal-width " +
                "have a very strong linear correlation. Let's check " +
                "this intuition with a plot:");

        Plot plot = new Plot();
        Points points = new Points(plot, jitter(df.getCol("petal-length"), 0.01), jitter(df.getCol("sepal-length"), 0.01));
        points.opt().setPchIndex(new OneIndexVector(1));
        plot.add(points);
        draw(plot, 600, 400);


    }
}
