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

package titanic;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.distributions.empirical.*;
import rapaio.explore.Summary;
import static rapaio.explore.Workspace.*;
import static rapaio.filters.NominalFilters.consolidate;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.FunctionLine;
import rapaio.graphics.plot.HistogramBars;
import rapaio.io.CsvPersistence;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ConsoleExplore {

    public static void main(String[] args) throws IOException {
        RandomSource.setSeed(1);

        Frame train = Utils.read("train.csv");
        Frame test = Utils.read("test.csv");
        List<Frame> frames = consolidate(Arrays.asList(train, test));
        train = frames.get(0);


        Plot plot = new Plot();
        plot.getOp().setXRange(-10, 100);
        plot.getOp().setYRange(0, 0.04);
        plot.add(new HistogramBars(plot, train.getCol("Age"), 40, true));
//        for (double i = 1; i < 10; i += 2) {
//            KernelDensityEstimator kde = new KernelDensityEstimator(train.getCol("Age"), new KernelFunctionUniform(), 9 / i);
//            plot.add(new FunctionLine(plot, kde.getPdfFunction(), 1024));
//        }

        for (double i = 1; i < 10; i += 2) {
            KernelDensityEstimator kde = new KernelDensityEstimator(train.getCol("Age"), new KernelFunctionEpanechnikov());
            plot.add(new FunctionLine(plot, kde.getPdfFunction(), 256));
        }


        draw(plot, 1024, 1024);
    }
}
