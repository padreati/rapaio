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
 *
 */

package rapaio.core.tests;

import rapaio.WS;
import rapaio.printer.Printable;
import rapaio.core.distributions.ChiSquare;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.graphics.Plotter2D;
import rapaio.graphics.plot.Plot;

import static java.util.stream.Collectors.groupingBy;
import static rapaio.graphics.opt.GOpt.color;

/**
 * Chi-Squared tests
 *
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/29/15.
 */
@Deprecated
public class ChiSquareTest implements Printable {


    public static ChiSquareTest newIndependenceTest(Var firstCat, Var secondCat) {
        return new ChiSquareTest(firstCat, secondCat);
    }

    public ChiSquareTest(Var firstCat, Var secondCat) {
//        String[] first = firstCat.stream().complete().collect(groupingBy(VSpot::label, HashMap::new, counting())).keySet().toArray();
//        HashMap<String, Long> second = secondCat.stream().complete().collect(groupingBy(VSpot::label, HashMap::new, counting()));

    }



    public static void main(String[] args) {

        System.out.println(1.0 - new ChiSquare(9).cdf(15));
//        System.out.println(1.0 - new ChiSquare(9).cdf(9.7)/9);

        Numeric x = Numeric.newSeq(0, 20, 0.01);
        Plot plot = Plotter2D.plot();
        for (int i = 1; i < 10; i++) {
            int df = i;
            Numeric y = x.stream().mapToDouble().map(x1 -> new ChiSquare(df+0.1).pdf(x1)).boxed().collect(Numeric.collector());
            plot.lines(x, y, color(df));
        }

        WS.draw(plot.yLim(0, 0.5));
    }

    @Override
    public void buildPrintSummary(StringBuilder sb) {

    }
}
