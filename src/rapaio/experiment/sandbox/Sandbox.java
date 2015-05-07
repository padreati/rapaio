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
import rapaio.datasets.Datasets;
import rapaio.stream.SCollectors;
import rapaio.ws.Summary;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/19/15.
 */
@Deprecated
public class Sandbox {

    public static void main(String[] args) throws IOException, URISyntaxException {

        Frame df = Datasets.loadIrisDataset();
        Summary.printSummary(df);

        df.stream()
                .collect(SCollectors.countingTop(s -> s.label("class")))
                .entrySet()
                .forEach(System.out::println);

        Stream.of("Ion", "Ion", "Ana", "Vasile", "Ana", "Ion", "Andrei")
                .collect(SCollectors.countingTop(Collectors.toSet()))
                .entrySet()
                .forEach(System.out::println);
    }
}
