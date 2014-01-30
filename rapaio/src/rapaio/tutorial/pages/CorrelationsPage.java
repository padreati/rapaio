/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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

import rapaio.correlation.PearsonRCorrelation;
import rapaio.correlation.SpearmanRhoCorrelation;
import rapaio.data.Frame;
import rapaio.data.filters.BaseFilters;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Points;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.data.filters.BaseFilters.jitter;
import static rapaio.workspace.Summary.names;
import static rapaio.workspace.Summary.summary;
import static rapaio.workspace.Workspace.*;

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
	public void render() throws IOException, URISyntaxException {

		heading(2, "Introduction");
		p("This tutorial presents you the correlations tools offered by Rapaio library.");

		p("We will use the classical iris data set. The numerical columns of this dataset are:");

		code("Frame df = Datasets.loadIrisDataset();\n"
				+ "df = ColFilters.retainNumeric(df);\n"
				+ "names(df);");
		final Frame df = BaseFilters.retainNumeric(Datasets.loadIrisDataset());
		names(df);

		heading(2, "Pearson product-moment correlation");

		p("Pearson product-moment correlation measures the linear "
				+ "correlation between two random variables. Among other "
				+ "getType of correlation measures, the Pearson product-moment "
				+ "detects only linear correlations. ");

		heading(4, "Definition");

		p("Pearson product-moment coefficient measures the linear correlation "
				+ "between two random variables \\(X\\) and \\(Y\\), "
				+ "giving a getValue between +1 and −1 inclusive, "
				+ "where 1 is total positive correlation, "
				+ "0 is no correlation, and −1 is negative correlation.");

		p("Pearson's correlation coefficient when applied to a population "
				+ "is commonly represented by the Greek letter \\(\rho\\) (rho) "
				+ "and may be referred to as the population correlation "
				+ "coefficient or the population Pearson correlation "
				+ "coefficient. "
				+ "The formula for \\(\\rho\\) is:");

		eqn("\\rho_{X,Y} = \\frac{cov(X,Y)}{\\sigma_X\\sigma_Y} = \\frac{E[(X-\\mu_X)(Y-\\mu_Y)]}{\\sigma_X\\sigma_Y}");

		p("Pearson's correlation coefficient when applied to a "
				+ "sample is commonly represented by the "
				+ "letter \\(r\\) and may be referred to as "
				+ "the sample correlation coefficient or "
				+ "the sample Pearson correlation coefficient. "
				+ "We can obtain a formula for \\(r\\) by substituting estimates "
				+ "of the covariances and variances based on a sample "
				+ "into the formula above. That formula for \\(r\\) is:");

		eqn("r = \\frac{\\sum ^n _{i=1}(X_i-\\bar{X})(Y_i-\\bar{Y})}{\\sqrt{\\sum ^n _{i=1}(X_i - \\bar{X})^2} "
				+ "\\sqrt{\\sum ^n _{i=1}(Y_i - \\bar{Y})^2}}");

		p("The interpretation of a correlation coefficient depends "
				+ "on the context and purposes. A correlation of 0.8 "
				+ "may be very low if one is verifying a physical law "
				+ "using high-quality instruments, but may be regarded "
				+ "as very high in the social sciences where there may be "
				+ "a greater contribution from complicating factors.");

		heading(2, "Usa Rapaio for Pearson correlation");

		p("Rapaio library allows one to compute Pearson \\(r\\) for more then one vector at a time. "
				+ "Thus the result will be a rapaio.data.matrix with computed \\(r\\) values between vectors,"
				+ "using vectors getIndex position as indexes in resulted rapaio.data.matrix. ");

		code("        PearsonRCorrelation corr = new PearsonRCorrelation(df);\n"
				+ "        summary(corr);\n");
		final PearsonRCorrelation r = new PearsonRCorrelation(df);
		summary(r);

		p("We can spot with eas that many of the attributes are "
				+ "linearly correlated. As a sample we find from the "
				+ "correlation summary that petal-length and petal-width "
				+ "have a very strong linear correlation. Let's check "
				+ "this intuition with a plot:");

		draw(new Plot()
				.add(new Points(
						jitter(df.getCol("petal-length"), 0.01),
						jitter(df.getCol("sepal-length"), 0.01)).setPchIndex(1))
				.setTitle("p correlation = " + r.getValues()[df.getColIndex("petal-length")][df.getColIndex("sepal-length")]),
				400, 300);

		p("Another \\(r\\) coefficient which have a getValue close to \\(1\\) is between "
				+ "sepal-length and petal-length. Let's check that with a plot, also: ");

		draw(new Plot()
				.add(new Points(jitter(df.getCol("petal-length"), 0.01), jitter(df.getCol("petal-width"), 0.01))
						.setPchIndex(1))
				.setTitle("p correlation = " + r.getValues()[df.getColIndex("petal-length")][df.getColIndex("petal-width")]),
				400, 300);

		p("Finally, we plot again, but this time using a coefficient which is closer to 0, "
				+ "which could mean that the variables are not linearly correlated. "
				+ "Such a getValue for correlation we have between sepal-length and sepal-width. ");

		draw(new Plot()
				.add(new Points(jitter(df.getCol("sepal-length"), 0.01), jitter(df.getCol("sepal-width"), 0.01))
						.setPchIndex(1))
				.setTitle("p correlation = " + r.getValues()[df.getColIndex("sepal-length")][df.getColIndex("sepal-width")]),
				400, 300);

		heading(2, "Spearman's rank correlation coefficient");

		p("often denoted by the Greek letter \\(\\rho\\) (rho) "
				+ "or as \\(r_s\\), is a nonparametric measure of "
				+ "statistical dependence between two variables. "
				+ "It assesses how well the relationship between two variables "
				+ "can be described using a monotonic function. If there are "
				+ "no repeated data values, a perfect Spearman correlation of "
				+ "\\(+1\\) or \\(−1\\) occurs when each of the variables is a "
				+ "perfect monotone function of the other.");

		heading(4, "Definition");
		p("The Spearman correlation coefficient is defined as the Pearson correlation "
				+ "coefficient between the ranked variables. For a sample of size \\(n\\), "
				+ "the \\(n\\) raw scores \\(X_i\\), \\(Y_i\\) are converted to ranks "
				+ "\\(x_i\\), \\(y_i\\), and \\(\\rho\\) is computed from these:");
		eqn("\\rho = \\frac{\\sum_i(x_i-\\bar{x})(y_i-\\bar{y})}{\\sqrt{\\sum_i (x_i-\\bar{x})^2 \\sum_i(y_i-\\bar{y})^2}}");
		p("Identical values (rank ties or getValue duplicates) are assigned a "
				+ "rank equal to the average of their positions in the ascending "
				+ "order of the values.");

		heading(2, "Use Rapaio to compute Spearman's rank correlation");

		p("Rapaio library allows one to compute Spearman \\(\\rho\\) for more then one vector at a time. "
				+ "Thus the result will be a rapaio.data.matrix with computed \\(\\rho\\) values between vectors,"
				+ "using vectors getIndex position as indexes in resulted rapaio.data.matrix. ");

		SpearmanRhoCorrelation rho = new SpearmanRhoCorrelation(df);
		summary(rho);
		summary(r);

		p(">>>This tutorial is generated with Rapaio document printer facilities.<<<");
	}
}
