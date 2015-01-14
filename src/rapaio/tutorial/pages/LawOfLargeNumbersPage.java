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

package rapaio.tutorial.pages;

import rapaio.core.RandomSource;
import rapaio.core.distributions.DUniform;
import rapaio.core.stat.OnlineStat;
import rapaio.data.Index;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.ABLine;
import rapaio.graphics.plot.Lines;

import java.io.IOException;

import static rapaio.WS.*;

/**
 * @author Aurelian Tutuianu
 */
public class LawOfLargeNumbersPage implements TutorialPage {

    @Override
    public String getPageName() {
        return "LawOfLargeNumbersPage";
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
                "of the results of experiment should be close to the expected value " +
                "and the sample average will become closer as more trials are performed.");

        p("To illustrate the intuition behind this law we will consider our experiment " +
                "to be a run of rolls of a dice. A dice has 6 possible outcomes, " +
                "the integer numbers from 1 to 6, with each output having equal " +
                "probability. Therefore the expected value of a single die roll is \\( (1+2+3+4+5+6)/6=3.5 \\)");

        p("We simulate the event of a single die roll to be a draw of a number from the " +
                "discrete uniform distribution with minimum value equals to 1 and maximum " +
                "value equals to 6. To simulate a large number of independent events we " +
                "simply draw a large sample of generated random numbers from the same distribution.");

        p("Rapaio makes this possible by using the following code:");

        final int N = 1_000;
        Var events = new DUniform(1, 6).sample(N);

        code("        final int N = 1_000;\n" +
                "        Var events = new DUniform(1, 6).sample(N);\n");

        p("Thus we have stored in a var N (1000) outputs of those events. We compute the running mean using StatOnline:");

        OnlineStat ocs = new OnlineStat();
        final Var mean = Numeric.newEmpty(N);
        for (int i = 0; i < events.rowCount(); i++) {
            ocs.update(events.value(i));
            mean.setValue(i, ocs.mean());
        }

        code("        StatOnline ocs = new StatOnline();\n" +
                "        final Var mean = Numeric.newEmpty(N);\n" +
                "        for (int i = 0; i < events.rowCount(); i++) {\n" +
                "            ocs.update(events.value(i));\n" +
                "            mean.setValue(i, ocs.mean());\n" +
                "        }\n");

        p("Now we have the running mean stored in the mean variable " +
                "and we can plot how that running mean evolves as " +
                "the size of the sample grows.");

        draw(new Plot()
                        .add(new ABLine(0, 3.5).lwd(1.5f).color(1))
                        .add(new Lines(Index.newSeq(1, N), mean)
                                .lwd(1.5f)
                                .color(2))
                        .yLim(2.5, 4.5),
                800, 300
        );

        p("We notice two fact from the plot above. First fact is that the running " +
                "average gets closer to the expected value, as sample size grows. " +
                "Second fact is that deviation from expected value is smaller as " +
                "the sample size grows aka. smaller variation. ");

        p("The code for drawing the plot follows:");

        code("draw(new Plot()\n" +
                "                        .add(new ABLine(0, 3.5).lwd(1.5f).color(1))\n" +
                "                        .add(new Lines(Index.newSeq(1, N), mean)\n" +
                "                                .lwd(1.5f)\n" +
                "                                .color(2))\n" +
                "                        .yLim(2.5, 4.5),\n" +
                "                800, 300\n" +
                "        );");

        p(">>>This tutorial is generated with Rapaio document printer facilities.<<<");
    }
}
