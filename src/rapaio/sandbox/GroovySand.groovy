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

package rapaio.sandbox

import rapaio.data.Frame
import rapaio.datasets.CBenchmark
import rapaio.datasets.CTask
import rapaio.graphics.Plot
import rapaio.graphics.plot.Points
import rapaio.printer.LocalPrinter
import rapaio.ws.Summary

import static rapaio.WS.draw
import static rapaio.WS.setPrinter

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
class GroovySand {

    public static void main(String[] args) throws IOException {

        setPrinter(new LocalPrinter())
        CTask bench = new CBenchmark().getTask("iris")
        Frame tr = bench.getTrain()
        Summary.summary(tr)

//        draw(new Plot().add(new Histogram(tr.col("sepallength"))))
        draw(new Plot().add(new Points(tr.col("sepallength"), tr.col("petallength"))
                .sz(3).color(tr.col("class")).pch(1))
        )
    }
}
