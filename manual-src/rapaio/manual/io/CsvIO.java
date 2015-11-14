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

package rapaio.manual.io;

import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.io.Csv;

import java.io.IOException;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/14/15.
 */
public class CsvIO {

    public static void main(String[] args) throws IOException {

        Frame iris = new Csv().read(Datasets.class, "iris-r.csv");

        // use only few rows
        iris = iris.mapRows(0, 1, 50, 51, 100, 101);
        iris.printLines();

        new Csv().write(iris, "/tmp/iris.csv");
    }
}
