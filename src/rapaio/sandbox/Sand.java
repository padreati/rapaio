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
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Lines;
import rapaio.graphics.plot.Points;
import rapaio.ml.regressor.RPrediction;
import rapaio.ml.regressor.Regressor;
import rapaio.ml.regressor.tree.rtree.RTree;
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

        final String F = "Father";
        final String S = "Son";


        Numeric test = Numeric.newSeq(59, 75, 0.04).withName(F);
        Frame te = SolidFrame.newWrapOf(test);

        Regressor r = new RTree().withMinCount(2);

        r.learn(df, S);

        r.summary();

        RPrediction pred = r.predict(te);

        draw(new Plot()
                        .add(new Points(df.var(F), df.var(S)).color(Color.LIGHT_GRAY))
                        .add(new Lines(te.var(F), pred.firstFit()).color(Color.BLUE))
        );
    }

}
