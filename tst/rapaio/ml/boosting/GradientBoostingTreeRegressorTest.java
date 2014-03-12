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

package rapaio.ml.boosting;

import org.junit.Test;
import rapaio.core.stat.MAE;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.filters.BaseFilters;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Lines;
import rapaio.ml.boost.GradientBoostingTreeRegressor;
import rapaio.ml.boost.gbt.L2BoostingLossFunction;
import rapaio.ml.simple.L2ConstantRegressor;
import rapaio.ml.tree.DecisionStumpRegressor;
import rapaio.printer.LocalPrinter;
import rapaio.workspace.Workspace;

import java.io.IOException;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class GradientBoostingTreeRegressorTest {

	@Test
	public void testProstate() throws IOException {

		Workspace.setPrinter(new LocalPrinter());
		Frame df = Datasets.loadProstateCancer();
		df = BaseFilters.removeCols(df, "train");

		String targetColName = "lpsa";

		GradientBoostingTreeRegressor gbt = new GradientBoostingTreeRegressor()
				.setBootstrap(0.5)
				.setShrinkage(0.2)
				.setLossFunction(new L2BoostingLossFunction())
				.setRegressor(new DecisionStumpRegressor())
				.setInitialRegressor(new L2ConstantRegressor())
				.setRounds(0);

		gbt.learn(df, targetColName);

		Numeric index = new Numeric();
		Numeric mae = new Numeric();
		gbt.predict(df);
		index.addValue(1);
		mae.addValue(new MAE(gbt.getFitValues(), df.getCol(targetColName)).getValue());
		for (int i = 1; i <= 400; i++) {
			gbt.learnFurther(1);
			gbt.predict(df);

			index.addValue(i + 1);
			mae.addValue(new MAE(gbt.getFitValues(), df.getCol(targetColName)).getValue());

			Workspace.draw(new Plot().add(new Lines(index, mae)));
			System.out.println(".");
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
			}
		}
	}
}
