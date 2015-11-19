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

package rapaio.experiment.sandbox;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Maximum;
import rapaio.core.stat.Minimum;
import rapaio.data.*;
import rapaio.datasets.Datasets;
import rapaio.experiment.grid.MeshGrid1D;
import rapaio.graphics.opt.ColorGradient;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.plotcomp.MeshContour;
import rapaio.graphics.plot.plotcomp.Points;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.ensemble.CForest;
import rapaio.ws.Summary;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.WS.draw;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/23/15.
 */
@Deprecated
public class IrisContour {

    public static void main(String[] args) throws IOException, URISyntaxException {

        RandomSource.setSeed(1);

        final String X = "petal-length";
        final String Y = "sepal-width";

        Frame iris = Datasets.loadIrisDataset().mapVars(X, Y, "class").stream().filter(s -> s.index(2) != 3).toMappedFrame();

        Var trimmedClass = Nominal.from(iris.rowCount(), row -> iris.label(row, "class")).withName("class");

        Frame tr = BoundFrame.newByVars(iris.var(X), iris.var(Y), trimmedClass);

        Normal g1 = new Normal(0, 2);
        Normal g2 = new Normal(0, 5);

        for (int i = 0; i < iris.rowCount(); i++) {
            if (iris.index(i, 2) == 1) {
                iris.setValue(i, 0, g1.sampleNext());
                iris.setValue(i, 1, g2.sampleNext());
            } else {
                iris.setValue(i, 0, 4 + g1.sampleNext());
                iris.setValue(i, 1, 9 + g2.sampleNext());
            }
        }

        Summary.printSummary(iris);

        Classifier c = new CForest().withMCols(1).withRuns(1_000);

        c.train(iris, "class");

        Numeric x = Numeric.seq(new Minimum(iris.var(X)).value(), new Maximum(iris.var(X)).value(), 0.1).withName(X);
        Numeric y = Numeric.seq(new Minimum(iris.var(Y)).value(), new Maximum(iris.var(Y)).value(), 0.2).withName(Y);
        MeshGrid1D mg1 = new MeshGrid1D(x, y);

        // build a classification data sets with all required points

        Numeric sl = Numeric.empty().withName(X);
        Numeric sw = Numeric.empty().withName(Y);
        for (int i = 0; i < x.rowCount(); i++) {
            for (int j = 0; j < y.rowCount(); j++) {
                sl.addValue(mg1.getX().value(i));
                sw.addValue(mg1.getY().value(j));
            }
        }
        CFit cr2 = c.fit(SolidFrame.newWrapOf(sl, sw));
        c.fit(iris).printSummary();
        int pos = 0;
        for (int i = 0; i < x.rowCount(); i++) {
            for (int j = 0; j < y.rowCount(); j++) {
                mg1.setValue(i, j, cr2.firstDensity().value(pos, 1));
                pos++;
            }
        }

        Plot p = new Plot();
        double[] qq = Numeric.seq(0, 1, 0.02).stream().mapToDouble().toArray();
        qq[qq.length - 1] = Double.POSITIVE_INFINITY;
        ColorGradient bcg = ColorGradient.newHueGradient(qq);
        for (int i = 0; i < qq.length - 1; i++) {
            p.add(new MeshContour(mg1.compute(qq[i], qq[i + 1]), false, true,
                    lwd(0.1f), color(bcg.getColor(i))));
        }
        p.add(new Points(iris.var(0), iris.var(1), color(iris.var(2)), pch(2)));

        draw(p);
    }
}
