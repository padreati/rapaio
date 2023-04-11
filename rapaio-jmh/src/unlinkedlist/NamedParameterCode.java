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

package unlinkedlist;

import static rapaio.graphics.Plotter.*;
import static rapaio.graphics.opt.GOptions.bins;
import static rapaio.graphics.opt.GOptions.fill;
import static rapaio.graphics.opt.GOptions.lwd;
import static rapaio.graphics.opt.GOptions.palette;
import static rapaio.graphics.opt.GOptions.pch;
import static rapaio.graphics.opt.GOptions.sz;

import java.awt.Color;
import java.io.IOException;

import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.graphics.opt.Palette;
import rapaio.image.ImageTools;
import rapaio.sys.WS;

public class NamedParameterCode {

    public static void main(String[] args) throws IOException {
        new NamedParameterCode().run();
    }

    private void run() throws IOException {
        sample1();
    }

    private void sample1() throws IOException {
        ImageTools.setBestRenderingHints();

Frame df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
Var x = df.rvar(0).name("x");
Var y = df.rvar(1).name("y");

// 3 x 3 grid cells
// 4 top-left cells will contain a scatter
// last row, 2 cells on left side filled with a histogram
// the others are filled as they come, from up to down, then from left to right

var fig = gridLayer(3, 3, lwd(0.5f))
        .add(0, 0, 2, 2, points(x, y, sz(3), fill(2), palette(Palette.tableau21()), pch(2)))
        .add(2, 1, 2, 1, hist(y, bins(20)))
        .add(lines(x))
        .add(hist(x, bins(20), fill(9), lwd(2f)))
        .add(hist2d(x, y, bins(30), fill(Color.BLUE)));

WS.draw(fig);
        WS.saveImage(fig, 800, 600, "./sample1.png");
    }
}
