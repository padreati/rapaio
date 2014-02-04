package rapaio.blog.vectors;

import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Numeric;
import rapaio.data.Vectors;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Points;
import rapaio.ml.linear.LinearModelRegressor;
import rapaio.server.AbstractCmd;
import rapaio.workspace.Summary;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.data.MathNumeric.*;
import static rapaio.workspace.Workspace.draw;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class VectorsExperiment extends AbstractCmd {

	public static void main(String[] args) throws Exception {
		new VectorsExperiment().runLocal();
	}

	@Override
	public void run() throws IOException, URISyntaxException {
		Frame df = Datasets.loadPearsonHeightDataset();
		Numeric son = (Numeric) df.getCol("Son");
		Numeric father = (Numeric) df.getCol("Father");

		Numeric unit = Vectors.newNum(son.getRowCount(), 1.);


		Numeric sonScale = scale(son);
		Numeric fatherScale = scale(father);


		draw(new Plot()
				.add(new Points(fatherScale, sonScale))
		);

		Numeric beta0 = div(dotSum(unit, son), dotSum(unit, unit));
		Numeric zeta0 = unit;
		Numeric gamma0 = div(dotSum(father, zeta0), dotSum(zeta0, zeta0));
		Numeric zeta1 = minus(father, dot(zeta0, gamma0));

		Numeric beta1 = div(dotSum(zeta1, son), dotSum(zeta1, zeta1));


		Summary.lines(beta0);
		Summary.lines(beta1);

		Summary.lines(minus(mean(son), dotSum(beta1, mean(father))));

		Summary.lines(sd(minus(son, plus(dot(unit, beta0), dot(father, beta1)))));
//		Summary.lines(plus(dot(unit, beta0), dot(father, beta1)));

		LinearModelRegressor lm = new LinearModelRegressor();
		df = Frames.addCol(df, unit, "Intercept", 0);
		lm.learn(df, "Son");
		Summary.lines(lm.getCoeff());
	}
}
