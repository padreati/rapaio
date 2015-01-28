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
import rapaio.core.RandomSource;
import rapaio.core.stat.Maximum;
import rapaio.core.stat.Minimum;
import rapaio.data.Frame;
import rapaio.data.*;
import rapaio.data.grid.MeshGrid1D;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.MeshContour;
import rapaio.graphics.plot.Points;
import rapaio.ml.classifier.CResult;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.svm.BinarySMO;
import rapaio.ml.classifier.svm.kernel.RBFKernel;
import rapaio.ml.classifier.svm.kernel.WaveletKernel;
import rapaio.ml.classifier.tree.CForest;
import rapaio.ml.eval.ConfusionMatrix;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/23/15.
 */
public class IrisContour {

    public static void main(String[] args) throws IOException, URISyntaxException {

        RandomSource.setSeed(1);

        String xlab = "x";
        String ylab = "y";


        Var v1 = Numeric.newEmpty().withName(xlab);
        Var v2 = Numeric.newEmpty().withName(ylab);
        Var v3 = Nominal.newEmpty().withName("class");

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 100; k++) {
                    v1.addValue(RandomSource.nextDouble() + i);
                    v2.addValue(RandomSource.nextDouble() + j);
                    v3.addLabel((i + j) % 2 == 0 ? "True" : "False");
                }
            }
        }

        Frame df = SolidFrame.newWrapOf(v1, v2, v3);


        Classifier c = CForest.buildRandomForest(400, 2, 0.9);
//        c = new NaiveBayesClassifier();
//        c = new GBTClassifier()
//                .withTree(RTree.buildCART().withMaxDepth(6))
//                .withRuns(1000);

//        c = new BinarySMO().withKernel(new PolyKernel(1, 1)).withMaxRuns(100);
//        c = new BinarySMO().withKernel(new PolyKernel(2, 1));
        c = new BinarySMO().withKernel(new RBFKernel(0.01)).withC(0.2);
//        c = new BinarySMO().withKernel(new WaveletKernel(0.5)).withC(1);
        c = new BinarySMO().withKernel(new WaveletKernel(0.5));
//        c = new BinarySMO().withKernel(new WaveletKernel(2));
//        c = new BinarySMO().withKernel(new ChiSquareKernel());
//        c = new BinarySMO().withKernel(new MinKernel());
//        c = new BinarySMO().withKernel(new SigmoidKernel(0.1, 1)).withMaxRuns(1000);


        c.learn(df, "class");

        new ConfusionMatrix(df.var("class"), c.predict(df).firstClasses()).summary();

        Var x = Numeric.newSeq(new Minimum(df.var(0)).value() - 1, new Maximum(df.var(0)).value() + 1, 0.05).withName(xlab);
        Var y = Numeric.newSeq(new Minimum(df.var(1)).value() - 1, new Maximum(df.var(1)).value() + 1, 0.05).withName(ylab);

        MeshGrid1D mg = new MeshGrid1D(x, y);

        Var x1 = Numeric.newEmpty().withName(xlab);
        Var y1 = Numeric.newEmpty().withName(ylab);

        for (int i = 0; i < x.rowCount(); i++) {
            for (int j = 0; j < y.rowCount(); j++) {
                x1.addValue(x.value(i));
                y1.addValue(y.value(j));
            }
        }

        Frame grid = SolidFrame.newWrapOf(x1, y1);
        CResult cr = c.predict(grid, true, true);

        int pos = 0;
        for (int i = 0; i < x.rowCount(); i++) {
            for (int j = 0; j < y.rowCount(); j++) {
                mg.setValue(i, j, cr.firstDensity().value(pos, 1) - cr.firstDensity().value(pos, 2));
                pos++;
//                mg.setValue(i, j, cr.firstClasses().value(pos++));
            }
        }

        Plot p = new Plot();
        double[] pp = new double[]{0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.8, 1};
        double[] qq = mg.quantiles(pp);

        for (int i = 0; i < qq.length - 1; i++) {
            p.add(new MeshContour(mg.compute(qq[i], qq[i + 1]), false, true).color(new Color(0f, 0f, 1f, (float) pp[i + 1])));
        }
        p.add(new MeshContour(mg.compute(0, Double.POSITIVE_INFINITY), true, false).color(0).lwd(2f));
        p.title("test");

        WS.draw(p.add(new Points(df.var(0), df.var(1)).color(df.var(2))));
    }
}
