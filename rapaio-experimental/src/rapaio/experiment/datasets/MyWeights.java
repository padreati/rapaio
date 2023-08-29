/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.experiment.datasets;

import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.GridLayer;
import rapaio.sys.WS;

import static rapaio.graphics.Plotter.hist;
import static rapaio.graphics.Plotter.points;
import static rapaio.graphics.opt.GOptions.bins;
import static rapaio.graphics.opt.GOptions.horizontal;

public class MyWeights {

    public static void main(String[] args) {
        Frame df = Datasets.loadMyWeights();
        df.printFullContent();
        WS.draw(GridLayer.of(1,2)
                .add(points(df.rvar("time"), df.rvar("weight"))
                        .lines(df.rvar("time"), df.rvar("weight"))
                )
                .add(hist(df.rvar("weight"), horizontal(true), bins(15)))
        );
    }
}
