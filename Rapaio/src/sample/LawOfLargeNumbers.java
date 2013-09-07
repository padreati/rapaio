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

import rapaio.core.stat.OnlineCoreStat;
import rapaio.core.RandomSource;
import rapaio.data.OneIndexVector;
import rapaio.data.IndexVector;
import rapaio.data.NumericVector;
import rapaio.data.Vector;
import rapaio.distributions.DUniform;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.ABLine;
import rapaio.graphics.plot.Lines;
import rapaio.printer.HTMLPrinter;

import static rapaio.explore.Workspace.*;

/**
 * @author Aurelian Tutuianu
 */
public class LawOfLargeNumbers {

    public static void main(String[] args) {
        setPrinter(new HTMLPrinter("LawOfLargeNumbers.html", "Law of Large Numbers Study"));

        preparePrinter();
        RandomSource.setSeed(1);

        heading(2, "Small study of the Weak Law of Large Numbers");

        p("The weak law of large numbers is part of the fundamental knowledge on "
                + " which statistical inference relies. ");

        heading(3, "Theorem: The Weak Law of Large Numbers");
        p("Let \\(X = {X_1, .. , X_n}\\) a series a independent and identically distributed random variables.");
        p(" Let \\(\\bar{X_n} = \\frac{1}{n}\\sum_{i=1}^{n}X_i \\) denoted as sample mean.");

        final int N = 200;

        Vector x = new IndexVector("x", 1, N, 1);
        Vector dice = new DUniform(1, 6).sample(N);

        OnlineCoreStat ocs = new OnlineCoreStat();
        Vector mean = new NumericVector("mean", N);
        for (int i = 0; i < dice.getRowCount(); i++) {
            ocs.update(dice.getValue(i), 1);
            mean.setValue(i, ocs.getMean());
            if (i < 2) {
                mean.setValue(i, i);
            }
        }

        Plot plot = new Plot();

        ABLine abline = new ABLine(plot, 0, 3.5);
        abline.opt().setLwd(2);
        abline.opt().setColorIndex(new OneIndexVector(1));
        plot.add(abline);

        Lines lines = new Lines(plot, x, mean);
        lines.opt().setLwd(2);
        lines.opt().setColorIndex(new OneIndexVector(2));
        plot.add(lines);

        plot.getOp().setYRange(2.5, 4.5);
        draw(plot, 800, 400);

        closePrinter();
    }
}
