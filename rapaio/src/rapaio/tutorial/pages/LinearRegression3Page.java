package rapaio.tutorial.pages;

import rapaio.core.stat.correlation.PearsonRCorrelation;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.filters.BaseFilters;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Points;
import rapaio.workspace.Summary;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.workspace.Workspace.*;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class LinearRegression3Page implements TutorialPage {

	@Override
	public String getPageName() {
		return "LinearRegression";
	}

	@Override
	public String getPageTitle() {
		return "Linear Regression: Multiple linear regression";
	}

	@Override
	public void render() throws IOException, URISyntaxException {

		heading(3, "Linear Regression with vectors and matrices - part 3");

		p("This tutorial aims to present how one can do by hand " +
				"linear regression using only vectors and matrices " +
				"operations. For practical purposes it should be used " +
				"linear regression models. ");

		heading(4, "Multiple Linear Regression");

		Frame cars = BaseFilters.retainNumeric(Datasets.loadCarMpgDataset());
		Summary.summary(cars);
		new PearsonRCorrelation(cars).summary();

		Numeric mpg = (Numeric) cars.getCol("mpg");
		Numeric disp = (Numeric) cars.getCol("displacement");
		Numeric weight = (Numeric) cars.getCol("weight");
		Numeric hp = (Numeric) cars.getCol("horsepower");

		draw(new Plot()
				.add(new Points(mpg, hp).setColorIndex(cars.getCol("origin")).setPchIndex(1))
				.setBottomLabel("mpg")
				.setLeftLabel("horsepower")
		);

		draw(new Plot()
				.add(new Points(mpg, weight))
				.setBottomLabel("mpg")
				.setLeftLabel("weight")
		);

		draw(new Plot()
				.add(new Points(hp, weight))
				.setBottomLabel("horsepower")
				.setLeftLabel("weight")
		);
	}
}