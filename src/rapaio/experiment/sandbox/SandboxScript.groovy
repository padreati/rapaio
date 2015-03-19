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

package rapaio.experiment.sandbox

import rapaio.WS
import rapaio.datasets.Datasets
import rapaio.graphics.Plot
import rapaio.graphics.plot.Histogram
import rapaio.ws.Summary

import java.util.stream.Collectors
import java.util.stream.Stream

import static rapaio.stream.SCollectors.countingTop

def df = Datasets.loadCarMpgDataset()
Summary.summary df


Stream.of("Ion", "Ion", "Ana", "Vasile", "Ana", "Ion", "Andrei")
        .collect(countingTop(Collectors.toSet()))
        .entrySet()
        .forEach({ println it })

WS.draw(new Plot().add(new Histogram(df.var("mpg"))))