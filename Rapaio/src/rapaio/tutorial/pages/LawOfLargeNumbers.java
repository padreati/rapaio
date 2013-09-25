/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

package rapaio.tutorial.pages;

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

import java.io.IOException;

/**
 * @author Aurelian Tutuianu
 */
public class LawOfLargeNumbers implements TutorialPage {

    @Override
    public String getPageName() {
        return "LawOfLargeNumbers";
    }

    @Override
    public String getPageTitle() {
        return "Explore Law of Large Numbers";
    }

    @Override
    public void render() throws IOException {
        RandomSource.setSeed(1);

        heading(2, "Simulation on Law of Large Numbers");

        p("In the probability theory the Law of the Large Numbers states that when " +
                "do you repeat an experiment a large number of times, the average " +
                "of the results of experiment should be close to the expected value. " +
                "The sample average will become closer as more trials are performed.");

        p("To illustrate the intuition behind this law we will consider our experiment " +
                "to be a run of rolls of a dice. A dice has 6 possible outcomes, " +
                "the integer numbers from 1 to 6, with each output having equal " +
                "probability. Therefore teh expected value of a single die roll is \\( (1+2+3+4+5+6)/6=3.5 \\)");

        p("We simulate the event of a single die roll to be a draw of a number from the " +
                "discrete uniform distribution with minimum value equals to 1 and maximum " +
                "value equals to 6. To simulate a large number of independent events we " +
                "simply draw a large sample of generated random numbers from the same distribution.");

        p("Rapaio makes this possible by using the following code:");

        code("        final int N = 1_000;\n" +
                "        Vector dice = new DUniform(1, 6).sample(N);\n");

        final int N = 1_000;

        Vector events = new DUniform(1, 6).sample(N);


        p("Thus we have stored in a vector N (1000) outputs of those events. " +
                "We compute the running mean using OnlineCoreStat:");

        code("        OnlineCoreStat ocs = new OnlineCoreStat();\n" +
                "        Vector mean = new NumericVector(\"mean\", N);\n" +
                "        for (int i = 0; i < events.getRowCount(); i++) {\n" +
                "            ocs.update(events.getValue(i), 1);\n" +
                "            mean.setValue(i, ocs.getMean());\n" +
                "        }\n");
        OnlineCoreStat ocs = new OnlineCoreStat();
        Vector mean = new NumericVector("mean", N);
        for (int i = 0; i < events.getRowCount(); i++) {
            ocs.update(events.getValue(i), 1);
            mean.setValue(i, ocs.getMean());
        }

        p("Now we have the running mean stored in the vector mean and we can plot " +
                "how that running mean evolves as the size of the sample grows.");

        Plot plot = new Plot();

        ABLine abline = new ABLine(plot, 0, 3.5);
        abline.opt().setLwd(1.5f);
        abline.opt().setColorIndex(new OneIndexVector(1));
        plot.add(abline);

        Lines lines = new Lines(plot, new IndexVector("x", 1, N, 1), mean);
        lines.opt().setLwd(1.5f);
        lines.opt().setColorIndex(new OneIndexVector(2));
        plot.add(lines);

        plot.getOp().setYRange(2.5, 4.5);
        draw(plot, 800, 300);

        p("Thus we can clearly notice two fact from the plot above. " +
                "First fact is that the running average gets closer to the " +
                "expected value, as sample size grows. " +
                "Second fact is that deviation from expected value is smaller as " +
                "the sample size grows aka. smaller variation. ");


        p("The code for drawing the plot follows:");

        code("        Plot plot = new Plot();\n" +
                "\n" +
                "        ABLine abline = new ABLine(plot, 0, 3.5);\n" +
                "        abline.opt().setLwd(2);\n" +
                "        abline.opt().setColorIndex(new OneIndexVector(1));\n" +
                "        plot.add(abline);\n" +
                "\n" +
                "        Lines lines = new Lines(plot, new IndexVector(\"x\", 1, N, 1), mean);\n" +
                "        lines.opt().setLwd(2);\n" +
                "        lines.opt().setColorIndex(new OneIndexVector(2));\n" +
                "        plot.add(lines);\n" +
                "\n" +
                "        plot.getOp().setYRange(2.5, 4.5);\n" +
                "        draw(plot, 800, 300);\n");

        p(">>>This tutorial is generated with Rapaio document printer facilities.<<<");
    }
}
