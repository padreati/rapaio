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

import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Lines;
import rapaio.graphics.plot.Points;
import rapaio.ml.regressor.RPrediction;
import rapaio.ml.regressor.boost.GBTRegressor;
import rapaio.ml.regressor.linear.OLSRegressor;
import rapaio.printer.LocalPrinter;
import rapaio.ws.Summary;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.WS.draw;
import static rapaio.WS.setPrinter;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class Sand {
    public static void main(String[] args) throws IOException, URISyntaxException {

        setPrinter(new LocalPrinter());

        Frame df = Datasets.loadPearsonHeightDataset();
        Summary.summary(df);

        SolidFrame tr = SolidFrame.newWrapOf(Numeric.newFill(df.rowCount(), 1.0).withName("Intercept"));
        df = tr.bindVars(df);

        Summary.summary(df);

        OLSRegressor r = new OLSRegressor();
        r.learn(df, "Son");

        Var test = Numeric.newSeq(59, 76, 0.1).withName("Father");
        Var inter = Numeric.newFill(test.rowCount(), 1.0).withName("Intercept");
        Frame te = SolidFrame.newWrapOf(inter, test);

        RPrediction pred = r.predict(te);


        GBTRegressor gbt = new GBTRegressor().withBootstrap(true).withBootstrapSize(0.9).withShrinkage(1);
        for (int i = 1; i < 500; i++) {
            gbt.learnFurther(df, i, "Son");
            RPrediction rp = gbt.predict(te);

            draw(new Plot()
                            .add(new Points(df.var("Father"), df.var("Son")).color(Color.LIGHT_GRAY))
                            .add(new Lines(test, pred.firstFit()).color(Color.BLUE))
                            .add(new Lines(test, rp.firstFit()).color(Color.RED))
            );

        }
    }
}
