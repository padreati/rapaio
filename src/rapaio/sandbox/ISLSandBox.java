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
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.graphics.GridLayer;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Points;
import rapaio.printer.IdeaPrinter;

import java.io.IOException;

import static rapaio.WS.draw;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/5/15.
 */
public class ISLSandBox {

    public static void main(String[] args) throws IOException {

        WS.setPrinter(new IdeaPrinter());

        Frame df = Datasets.loadISLAdvertising().removeVars("ID");

        df.summary();

        GridLayer gl = new GridLayer(1, 3);
        gl.add(1, 1, new Plot().add(new Points(df.var("TV"), df.var("Sales"))));
        gl.add(1, 2, new Plot().add(new Points(df.var("Radio"), df.var("Sales"))));
        gl.add(1, 3, new Plot().add(new Points(df.var("Newspaper"), df.var("Sales"))));

        draw(gl);
    }
}
