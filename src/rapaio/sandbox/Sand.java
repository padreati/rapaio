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
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.ABLine;
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

        Frame df = Datasets.loadIrisDataset();
        Summary.summary(df);

        Regressor r = RTree.buildDecisionStump();

        r = RTree.buildC45().withMaxDepth(Integer.MAX_VALUE);
        r = RTree.buildC45().withMaxDepth(Integer.MAX_VALUE);

        r.learn(df, "sepal-length");

        r.summary();

        RPrediction pred = r.predict(df, true);

        draw(new Plot()
                        .add(new Points(df.var(1), df.var(0)).color(Color.LIGHT_GRAY))
                        .add(new Points(df.var(1), pred.firstFit()).color(Color.BLUE))
        );

        draw(new Plot()
                        .add(new Points(df.var(1), pred.firstResidual()))
                        .add(new ABLine(0, true).color(Color.LIGHT_GRAY))
        );
    }
}
