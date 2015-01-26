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

package rapaio.sandbox;

import rapaio.core.distributions.empirical.KFunc;
import rapaio.core.distributions.empirical.KFuncBiWeight;
import rapaio.core.distributions.empirical.KFuncGaussian;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.data.grid.MeshGrid;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.MeshContour;
import rapaio.graphics.plot.SandboxComponent;
import rapaio.util.Pair;

import java.awt.*;
import java.util.function.BiFunction;

import static rapaio.WS.draw;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/21/15.
 */
public class ContourSample {

    public static void main(String[] args) {

//        setPrinter(new IdeaPrinter());
        KFunc f1 = new KFuncGaussian();
        KFunc f2 = new KFuncGaussian();
        KFunc f3 = new KFuncBiWeight();

        BiFunction<Double, Double, Double> bi = (x, y) ->
                Math.sqrt(f1.pdf(x, 3, 2) * f1.pdf(y, 3, 2)) +
                        Math.sqrt(f2.pdf(x, -1, 1) * f2.pdf(y, -1, 1)) +
                        Math.sqrt(f3.pdf(x, -2, 2) * f3.pdf(y, 6, 3)) / 3;

        Numeric x = Numeric.newSeq(-3, 10, 0.07);
        Numeric y = Numeric.newSeq(-3, 10, 0.07);

        MeshGrid mg = new MeshGrid(x, y);
        mg.fillWithFunction(bi);


        Plot p = new Plot();
        Pair<Double, Double> range = mg.valueRange();
        Var q = Numeric.newSeq(0.1, 0.99, 0.1);
        for (int i = 0; i < q.rowCount(); i++) {
            p.add(new MeshContour(mg, range.first + q.value(i) * (range.second - range.first))
                    .withFill(true)
                    .color(new Color(0.f, 0.f, 1f, (float) q.value(i) * 0.7f)));
        }
//        p.add(new ContourLine(bi, Math.pow(10, -7)).color(Color.green));
//        draw(p);


        draw(new Plot().add(new SandboxComponent()));
    }
}
