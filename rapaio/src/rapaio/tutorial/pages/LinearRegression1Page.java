package rapaio.tutorial.pages;

import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.datasets.Datasets;
import rapaio.workspace.Summary;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.data.MathNumeric.*;
import static rapaio.workspace.Workspace.*;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class LinearRegression1Page implements TutorialPage {

	@Override
	public String getPageName() {
		return "LinearRegression1";
	}

	@Override
	public String getPageTitle() {
		return "Linear Regression: predict with a constant";
	}

	@Override
	public void render() throws IOException, URISyntaxException {

		heading(3, "Linear Regression with vectors and matrices - part 1");

		p("This tutorial aims to present how one can do by hand " +
				"linear regression using only vectors and matrices " +
				"operations. For practical purposes it should be used " +
				"linear regression models. ");

		heading(4, "Predict with a constant");

		p("We will use the well-known data set Father-Son Heights. This " +
				"data was collected by Karl Pearson and contains the heights " +
				"of the fathers and their full-grown sons, in England, circa 1900. ");

		p("This data set is used to study the relation between the fathers " +
				"and the sons heights. However for the purpose of this tutorial " +
				"we are interested only in the sons heights. ");

		Frame df = Datasets.loadPearsonHeightDataset();
		Summary.summary(df);

		heading(4, "Question: Given collected sons heights, can we find a constant which " +
				"can be used for prediction? ");

		p("First we get the heights of the sons in a numeric vector. ");

		Numeric sons = (Numeric) df.col("Son");

		code("\t\tFrame df = Datasets.loadPearsonHeightDataset();\n" +
				"\t\tNumeric sons = (Numeric) df.col(\"Son\");\n");

		p("We ");

		code("mean: " + mean(sons).value(0));
		code("variance: " + var(sons).value(0));
		code("sd: " + sd(sons).value(0));
	}
}