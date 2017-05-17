/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
import rapaio.printer.Summary;

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

        Frame iris = Datasets.loadIrisDataset().mapVars(X, Y, "class").stream().filter(s -> s.getIndex(2) != 3).toMappedFrame();

        Var trimmedClass = NominalVar.from(iris.getRowCount(), row -> iris.getLabel(row, "class")).withName("class");

        Frame tr = BoundFrame.byVars(iris.getVar(X), iris.getVar(Y), trimmedClass);

        Normal g1 = new Normal(0, 2);
        Normal g2 = new Normal(0, 5);

        for (int i = 0; i < iris.getRowCount(); i++) {
            if (iris.getIndex(i, 2) == 1) {
                iris.setValue(i, 0, g1.sampleNext());
                iris.setValue(i, 1, g2.sampleNext());
            } else {
                iris.setValue(i, 0, 4 + g1.sampleNext());
                iris.setValue(i, 1, 9 + g2.sampleNext());
            }
        }

        Summary.printSummary(iris);

        Classifier c = CForest.newRF().withMCols(1).withRuns(1_000);

        c.train(iris, "class");

        NumericVar x = NumericVar.seq(Minimum.from(iris.getVar(X)).getValue(), Maximum.from(iris.getVar(X)).getValue(), 0.1).withName(X);
        NumericVar y = NumericVar.seq(Minimum.from(iris.getVar(Y)).getValue(), Maximum.from(iris.getVar(Y)).getValue(), 0.2).withName(Y);
        MeshGrid1D mg1 = new MeshGrid1D(x, y);

        // build a classification data sets with all required points

        NumericVar sl = NumericVar.empty().withName(X);
        NumericVar sw = NumericVar.empty().withName(Y);
        for (int i = 0; i < x.getRowCount(); i++) {
            for (int j = 0; j < y.getRowCount(); j++) {
                sl.addValue(mg1.getX().getValue(i));
                sw.addValue(mg1.getY().getValue(j));
            }
        }
        CFit cr2 = c.fit(SolidFrame.byVars(sl, sw));
        c.fit(iris).printSummary();
        int pos = 0;
        for (int i = 0; i < x.getRowCount(); i++) {
            for (int j = 0; j < y.getRowCount(); j++) {
                mg1.setValue(i, j, cr2.firstDensity().getValue(pos, 1));
                pos++;
            }
        }

        Plot p = new Plot();
        double[] qq = NumericVar.seq(0, 1, 0.02).stream().mapToDouble().toArray();
        qq[qq.length - 1] = Double.POSITIVE_INFINITY;
        ColorGradient bcg = ColorGradient.newHueGradient(qq);
        for (int i = 0; i < qq.length - 1; i++) {
            p.add(new MeshContour(mg1.compute(qq[i], qq[i + 1]), false, true,
                    lwd(0.1f), color(bcg.getColor(i))));
        }
        p.add(new Points(iris.getVar(0), iris.getVar(1), color(iris.getVar(2)), pch(2)));


        draw(p);
    }
}
