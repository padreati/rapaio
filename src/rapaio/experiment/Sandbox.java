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
import rapaio.data.Numeric;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plotter;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static rapaio.graphics.Plotter.lines;
import static rapaio.graphics.Plotter.plot;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/26/15.
 */
public class Sandbox {
    public static void main(String[] args) throws IOException, URISyntaxException {

        WS.setPrinter(new IdeaPrinter());
        Numeric v = Numeric.newWrapOf(DoubleStream.iterate(2, x -> Math.pow(x, 0.99999999)).limit(100_000).toArray());
        WS.draw(lines(v));
    }
}
