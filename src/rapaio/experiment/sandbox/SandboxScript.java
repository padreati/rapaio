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
 */

package rapaio.experiment.sandbox;

import rapaio.WS;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Histogram;
import rapaio.stream.SCollectors;
import rapaio.ws.Summary;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SandboxScript {
    public static void main(String[] args) throws IOException, URISyntaxException {
        Frame df = Datasets.loadCarMpgDataset();
        Summary.summary(df);

        Stream.of("Ion", "Ion", "Ana", "Vasile", "Ana", "Ion", "Andrei")
                .collect(SCollectors.countingTop(Collectors.toSet()))
                .entrySet()
                .forEach(System.out::println);

        WS.draw(new Plot().add(new Histogram(df.var("mpg"))));
    }
}
