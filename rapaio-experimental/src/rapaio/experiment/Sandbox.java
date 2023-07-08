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

package rapaio.experiment;

import rapaio.core.distributions.Normal;
import rapaio.data.VarDouble;
import rapaio.graphics.plot.Plot;
import rapaio.sys.WS;

import java.awt.*;

import static rapaio.graphics.Plotter.gridLayer;
import static rapaio.graphics.Plotter.plot;
import static rapaio.graphics.opt.GOptions.*;

public class Sandbox {

    public static void main(String[] args) {

        var x = VarDouble.seq(0, 1, 0.004).name("x");

        var green = Normal.of(0.24, 0.08);
        var blue = Normal.of(0.37, 0.15);
        var orange = Normal.of(0.59, 0.13);
        var red = Normal.of(0.80, 0.06);

        Color cgreen = Color.decode("0x2ca02c");
        Color cblue = Color.decode("0x1f77b4");
        Color corange = Color.decode("0xff7f0e");
        Color cred = Color.decode("0xd62728");

        var ygreen = VarDouble.from(x, green::pdf).name("y");
        var yblue = VarDouble.from(x, blue::pdf).name("y");
        var yorange = VarDouble.from(x, orange::pdf).name("y");
        var yred = VarDouble.from(x, red::pdf).name("y");

        float alpha = 0.5f;
        float lwd = 5f;

        Plot up = plot();

        up.polyfill(x, yblue, fill(cblue), alpha(alpha));
        up.polyfill(x, yorange, fill(corange), alpha(alpha));
        up.polyfill(x, ygreen, fill(cgreen), alpha(alpha));
        up.polyfill(x, yred, fill(cred), alpha(alpha));

        up.polyline(false, x, yblue, color(cblue), lwd(lwd));
        up.polyline(false, x, yorange, color(corange), lwd(lwd));
        up.polyline(false, x, ygreen, color(cgreen), lwd(lwd));
        up.polyline(false, x, yred, color(cred), lwd(lwd));

        up.xLim(0, 1);
        up.leftThick(false);
        up.leftMarkers(false);
        up.bottomThick(false);
        up.bottomMarkers(false);

        Plot down = plot();

        down.leftThick(false);
        down.leftMarkers(false);
        down.bottomThick(false);
        down.bottomMarkers(false);

        down.text(0.5, 0.6, "rapaio", font("DejaVu Sans", Font.BOLD, 110),
                halign.center(), valign.center(), color(Color.decode("0x096b87")));
        down.xLim(0, 1);
        down.yLim(0, 1);

        WS.image(gridLayer(2, 1, heights(0.7, 0.3)).add(up).add(down));
    }
}
