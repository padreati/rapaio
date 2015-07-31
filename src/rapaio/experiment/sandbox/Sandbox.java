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

package rapaio.experiment.sandbox;

import rapaio.data.Frame;
import rapaio.data.filter.Filters;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plotter;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.data.filter.Filters.jitter;
import static rapaio.graphics.Plotter.color;
import static rapaio.graphics.Plotter.hist;
import static rapaio.graphics.Plotter.points;
import static rapaio.sys.WS.draw;
import static rapaio.sys.WS.setPrinter;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/19/15.
 */
@Deprecated
public class Sandbox {

    public static void main(String[] args) throws IOException, URISyntaxException {

        System.out.println(3.0 / 2);
    }
}
