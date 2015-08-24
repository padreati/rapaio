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

package rapaio.tutorial.pages;

import rapaio.core.correlation.PearsonRCorrelation;
import rapaio.core.correlation.RhoCorr;
import rapaio.data.Frame;
import rapaio.data.VarType;
import rapaio.data.filter.FFAbstractRetainTypes;
import rapaio.data.filter.VFJitter;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plotter;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.sys.WS.*;
import static rapaio.graphics.Plotter.plot;
import static rapaio.graphics.Plotter.pch;
import static rapaio.ws.Summary.printNames;
import static rapaio.ws.Summary.printSummary;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
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
    public void render() throws IOException, URISyntaxException {

        heading(2, "Introduction");
        p("This tutorial presents you the tools built in rapaio library which enables you to compute correlation coefficients.");
        p("We will use the classical iris data set. The numerical columns of this data set are:");

        final Frame df = new FFAbstractRetainTypes(VarType.NUMERIC).filter(Datasets.loadIrisDataset());
        printNames(df);

        code("        final Frame df = new FFAbstractRetainTypes(VarType.NUMERIC).filter(Datasets.loadIrisDataset());\n" +
                "        names(df);\n");

        heading(2, "Pearson product-moment correlation");

        p("Pearson product-moment correlation measures the linear correlation between two random variables. Among other types " +
                "of correlation measures, the Pearson product-moment measures only linear correlations. ");

        heading(4, "Definition");

        p("Pearson product-moment coefficient measures the linear correlation between two random variables \\(X\\) and \\(Y\\), " +
                "giving a value in \\([+1,−1]\\), where \\(1\\) is perfect positive linear correlation, \\(0\\) is no correlation, " +
                "and \\(−1\\) is perfect negative linear correlation.");

        p("Pearson's correlation coefficient when applied to a population is commonly represented by the Greek letter " +
                "\\(\rho\\) (rho) and may be referred to as the population correlation coefficient or the population Pearson " +
                "correlation coefficient. The formula for \\(\\rho\\) is:");

        eqn("\\rho_{X,Y} = \\frac{cov(X,Y)}{\\sigma_X\\sigma_Y} = \\frac{E[(X-\\mu_X)(Y-\\mu_Y)]}{\\sigma_X\\sigma_Y}");

        p("Pearson's correlation coefficient when applied to a sample is commonly represented by the " +
                "letter \\(r\\) and may be referred to as the sample correlation coefficient or " +
                "the sample Pearson correlation coefficient. " +
                "We can obtain a formula for \\(r\\) by substituting estimates of the covariances and variances " +
                "based on a sample into the formula above. That formula for \\(r\\) is:");

        eqn("r = \\frac{\\sum ^n _{i=1}(X_i-\\bar{X})(Y_i-\\bar{Y})}{\\sqrt{\\sum ^n _{i=1}(X_i - \\bar{X})^2} "
                + "\\sqrt{\\sum ^n _{i=1}(Y_i - \\bar{Y})^2}}");

        p("The interpretation of a correlation coefficient depends on the context and purposes. " +
                "A correlation of \\(0.8\\) may be very low if one is verifying a physical law " +
                "using high-quality instruments, but may be regarded as very high in the " +
                "social sciences where there may be a greater contribution from complicating factors.");

        heading(2, "How to compute Pearson correlation coefficient");

        p("Rapaio library allows one to compute Pearson \\(r\\) for more then one variable at a time. " +
                "Thus the result will be a matrix with computed \\(r\\) values between vectors, " +
                "using vectors index position as indexes in resulted matrix. ");

        final PearsonRCorrelation r = new PearsonRCorrelation(df);
        printSummary(r);

        code("        final PearsonRCorrelation r = new PearsonRCorrelation(df);\n" +
                "        printSummary(r);\n");

        p("We can note by visual inspection that many of the attributes are linearly correlated. " +
                "For example we can find from the correlation printSummary that petal-length and " +
                "petal-width variables have a very strong linear correlation. Let's check " +
                "this intuition with a plot:");

        draw(plot().points(
                        new VFJitter(0.01).filter(df.var("petal-length")),
                        new VFJitter(0.01).filter(df.var("sepal-length")),
                        Plotter.pch(1)
                )
                        .title("p correlation = " + r.values()[df.varIndex("petal-length")][df.varIndex("sepal-length")]),
                400, 300
        );

        p("Another \\(r\\) coefficient which has a value close to \\(1\\) is the sample correlation between " +
                "sepal-length and petal-length. Let's check that with a plot, also: ");

        draw(plot()
                        .points(
                                new VFJitter(0.01).filter(df.var("petal-length")),
                                new VFJitter(0.01).filter(df.var("petal-width")),
                                Plotter.pch(1)
                        )
                        .title("p correlation = " + r.values()[df.varIndex("petal-length")][df.varIndex("petal-width")]),
                400, 300
        );

        p("Finally, we plot again, but this time using a coefficient which is closer to 0, " +
                "which could mean that the variables are not linearly correlated. " +
                "We have such kind of value between sepal-length and sepal-width variables. ");

        draw(plot()
                        .points(
                                new VFJitter(0.01).filter(df.var("sepal-length")),
                                new VFJitter(0.01).filter(df.var("sepal-width")),
                                Plotter.pch(1))
                        .title("p correlation = " + r.values()[df.varIndex("sepal-length")][df.varIndex("sepal-width")]),
                400, 300
        );

        heading(2, "Spearman's rank correlation coefficient");

        p("Often denoted by the Greek letter \\(\\rho\\) (rho) or as \\(r_s\\), this correlation " +
                "coefficient is a non-parametric measure of statistical dependence between " +
                "two variables. It assesses how well the relationship between two variables " +
                "can be described using a monotonic function. If there are no repeated data values, " +
                "a perfect Spearman correlation of \\(+1\\) or \\({−1}\\) occurs when each of the " +
                "variables is a perfect monotone function of the other.");

        heading(4, "Definition");

        p("The Spearman correlation coefficient is defined as the Pearson correlation coefficient " +
                "between the ranked variables. For a sample of size \\(n\\), the \\(n\\) sample " +
                "values \\(X_i\\), \\(Y_i\\) are converted to ranks " +
                "\\(x_i\\), \\(y_i\\), and \\(\\rho\\) is computed on the ranked values:");

        eqn("\\rho = \\frac{\\sum_i(x_i-\\bar{x})(y_i-\\bar{y})}{\\sqrt{\\sum_i (x_i-\\bar{x})^2 \\sum_i(y_i-\\bar{y})^2}}");

        p("Identical values (rank ties or value duplicates) are assigned a " +
                "rank equal to the average of their positions in the ascending " +
                "order of the values.");

        heading(2, "Use Rapaio to compute Spearman's rank correlation");

        p("Rapaio library allows one to compute Spearman \\(\\rho\\) for more then \\(1\\) variable at a time. " +
                "Thus the result will be a matrix with computed \\(\\rho\\) values between given variables, " +
                "using variables' index positions as indexes in the resulted matrix. ");

        RhoCorr rho = new RhoCorr(df);
        printSummary(rho);
        printSummary(r);

        p(">>>This tutorial is generated with Rapaio document printer facilities.<<<");
    }
}
