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

package rapaio.sandbox;

import rapaio.WS;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Maximum;
import rapaio.core.stat.Minimum;
import rapaio.data.*;
import rapaio.data.Frame;
import rapaio.data.grid.MeshGrid1D;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.MeshContour;
import rapaio.graphics.plot.Points;
import rapaio.ml.classifier.CResult;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.linear.BinaryLogistic;
import rapaio.printer.IdeaPrinter;
import rapaio.ws.Summary;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.WS.draw;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/23/15.
 */
public class IrisContour {

    public static void main(String[] args) throws IOException, URISyntaxException {

//        RandomSource.setSeed(1);
        WS.setPrinter(new IdeaPrinter());

        final String X = "petal-length";
        final String Y = "sepal-width";

        Frame iris = Datasets.loadIrisDataset();
        iris = iris.mapVars(X, Y, "class");
        iris = iris.stream().filter(s -> s.index(2) != 3).toMappedFrame();

        Var trimmedClass = Nominal.newEmpty().withName("class");
        iris.var("class").stream().forEach(s -> trimmedClass.addLabel(s.label()));

        iris = BoundFrame.newByVars(iris.var(X), iris.var(Y), trimmedClass).solidCopy();

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

        Summary.summary(iris);

        Classifier smo = new BinaryLogistic().withTol(1e-8).withMaxRuns(100_000);
        smo.learn(iris, "class");

        Numeric x = Numeric.newSeq(new Minimum(iris.var(X)).value(), new Maximum(iris.var(X)).value(), 0.1).withName(X);
        Numeric y = Numeric.newSeq(new Minimum(iris.var(Y)).value(), new Maximum(iris.var(Y)).value(), 0.1).withName(Y);
        MeshGrid1D mg1 = new MeshGrid1D(x, y);

        // build a classification data sets with all required points

        Numeric sl = Numeric.newEmpty().withName(X);
        Numeric sw = Numeric.newEmpty().withName(Y);
        for (int i = 0; i < x.rowCount(); i++) {
            for (int j = 0; j < y.rowCount(); j++) {
                sl.addValue(mg1.getX().value(i));
                sw.addValue(mg1.getY().value(j));
            }
        }
        CResult cr2 = smo.predict(SolidFrame.newWrapOf(sl, sw));
        cr2.summary();
        int pos = 0;
        for (int i = 0; i < x.rowCount(); i++) {
            for (int j = 0; j < y.rowCount(); j++) {
                if (pos == 4067) {
                    System.out.println();
                }
                mg1.setValue(i, j, cr2.firstDensity().value(pos++, 1));
            }
        }

        Plot p = new Plot();
        double[] qq = Numeric.newSeq(0, 1, 0.1).stream().mapToDouble().toArray();
        for (int i = 0; i < qq.length - 1; i++) {
            p.add(new MeshContour(mg1.compute(qq[i], qq[i + 1]), false, true).lwd(0.3f).color(new Color(0f, 0f, 1f, 0.7f * (1f - i / 10.f))));
        }
        p.add(new MeshContour(mg1.compute(0.5, Double.POSITIVE_INFINITY), true, false).lwd(1.2f));
        p.add(new Points(iris.var(0), iris.var(1)).color(iris.var(2)));

        draw(p);
    }
}
