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

import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.GridLayer;

import java.awt.*;
import java.io.IOException;

import static rapaio.graphics.Plotter.points;
import static rapaio.sys.WS.draw;
import static rapaio.graphics.Plotter.color;
import static rapaio.graphics.Plotter.pch;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/30/15.
 */
@Deprecated
public class ISLSandBox {

    public static void main(String[] args) throws IOException {

//        setPrinter(new IdeaPrinter());
        Frame df = Datasets.loadISLAdvertising().removeVars("ID");
        df.printSummary();
        GridLayer gl = new GridLayer(1, 3);
        gl.add(1, 1, points(df.var("TV"), df.var("Sales"), color(Color.RED), pch(2)));
        gl.add(1, 2, points(df.var("Radio"), df.var("Sales"), color(Color.RED), pch(2)));
        gl.add(1, 3, points(df.var("Newspaper"), df.var("Sales"), color(Color.cyan), pch(2)));
        draw(gl);

    }
}
