/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import rapaio.core.distributions.Binomial;
import rapaio.core.distributions.Normal;
import rapaio.core.distributions.Poisson;
import rapaio.data.Numeric;
import rapaio.graphics.Plotter;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static rapaio.graphics.Plotter.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/18/16.
 */
public class Sandbox {

    public static void main(String[] args) {

        Numeric x = Numeric.seq(0, 24*5, 0.01);
        Numeric y = logistic(x, 24*2, 24*3, 0.975);

        WS.setPrinter(new IdeaPrinter());
        WS.draw(lines(x, y).vLine(24*2).vLine(24*3).vLine(24*4));

    }

    static Numeric logistic(Numeric x, double safe, double full, double p) {
        double mid = (safe+full)/2;
        double steep = Math.log(p/(1-p))/(mid-safe);
        return Numeric.from(x, v -> 1 - 1/(1+Math.exp(-steep*(v-mid))));
    }
}
