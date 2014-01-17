package rapaio.tutorial.pages;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.workspace.Workspace.heading;
import static rapaio.workspace.Workspace.p;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class LinearRegression2Page implements TutorialPage {

	@Override
	public String getPageName() {
		return "LinearRegression2";
	}

	@Override
	public String getPageTitle() {
		return "Linear Regression: Simple linear regression";
	}

	@Override
	public void render() throws IOException, URISyntaxException {

		heading(3, "Linear Regression with vectors and matrices - part 2");

		p("This tutorial aims to present how one can do by hand " +
				"linear regression using only vectors and matrices " +
				"operations. For practical purposes it should be used " +
				"linear regression models. ");

		heading(4, "Simple linear regression");

	}
}