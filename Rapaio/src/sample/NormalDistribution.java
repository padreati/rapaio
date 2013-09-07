/*
 * Copyright 2013 Aurelian Tutuianu
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

package sample;

import rapaio.data.IndexOneVector;
import rapaio.distributions.Normal;
import rapaio.distributions.StudentT;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.FunctionLine;
import rapaio.printer.RemotePrinter;

import static rapaio.explore.Workspace.draw;
import static rapaio.explore.Workspace.setPrinter;

/**
 * @author Aurelian Tutuianu
 */
public class NormalDistribution {

    public static void main(String[] args) {

        setPrinter(new RemotePrinter());

        Plot p = new Plot();

        FunctionLine fline = new FunctionLine(p, new StudentT(3).getPdfFunction());
        p.add(fline);
        p.getOp().setXRange(-4, 4);
        p.getOp().setYRange(0, 0.5);

        FunctionLine normalpdf = new FunctionLine(p, new Normal().getPdfFunction());
        normalpdf.opt().setColorIndex(new IndexOneVector(1));
        p.add(normalpdf);

        draw(p);
    }
}
