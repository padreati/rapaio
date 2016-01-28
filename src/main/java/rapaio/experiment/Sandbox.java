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

package rapaio.experiment;

import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.filter.frame.FFRandomProjection;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plotter;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.graphics.Plotter.color;
import static rapaio.graphics.Plotter.points;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/26/15.
 */
public class Sandbox {
    public static void main(String[] args) throws IOException, URISyntaxException {

        for (int i = 0; i < 100; i++) {

            Frame df = Datasets.loadIrisDataset();

            Frame rp = new FFRandomProjection(2, FFRandomProjection.normal(), VRange.all()).filter(df.mapVars("0~3"));

            WS.setPrinter(new IdeaPrinter());
            WS.draw(points(rp.var(0), rp.var(1), color(df.var("class"))));
            df.printSummary();

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
