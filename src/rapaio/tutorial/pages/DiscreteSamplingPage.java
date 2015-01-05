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
import rapaio.core.distributions.empirical.KDE;
import rapaio.core.sample.Sampling;
import rapaio.data.*;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.*;
import rapaio.ws.Summary;

import java.io.IOException;
import java.util.Arrays;

import static rapaio.WS.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DiscreteSamplingPage implements TutorialPage {

    @Override
    public String getPageName() {
        return "DiscreteSampling";
    }

    @Override
    public String getPageTitle() {
        return "Discrete Sampling Tutorial";
    }

    @Override
    public void render() throws IOException {

        heading(1, "Discrete Sampling with Rapaio");

        p("In statistics, sampling is the process of selecting a subset of individuals " +
                "from within a statistical population. There are many types of sampling " +
                "methods which can be employed, like simple random sampling, " +
                "stratified sampling, systematic sampling and so on. ");

        p("However the purpose of this tutorial is to present four algorithms which " +
                "are used as building blocks by various statistical sampling methods. " +
                "The purpose of this tutorial is to present discrete random sampling methods.");

        p("<b>Definition:</b>");
        p("<i>A statistical probability density whose values can take only values from " + "a countable discrete set is called a discrete density. </i>");

        p("A discrete probability density function is completely described by " +
                "the set of possible values the random variable can take and " +
                "by the probability values assigned to each input value.");

        p("An example of discrete density is the process of throwing a standard dice. " +
                "We have a finite set of outcomes of the process (6 possible values) and " +
                "a probability function value associated with each output (for a fair dice we " +
                "can associate probability \\( p(x_i) = \\frac{1}{6} \\)).");

        p("Drawing a sample from a discrete population is the process of selecting some values " +
                "from the possible values \\( x_i,i=1..n \\) according with their probabilities. " +
                "Sampling is useful for far many purposes that I can describe here. " +
                "Among some very important scenarios are simulations and sample analysis. " +
                "For the latter example we can note that working with the heights and " +
                "weights of the all the people from the world (assuming that this " +
                "information is possible to collect, there are probably over 7 billions " +
                "records) is much harder than working with a much smaller sample. ");

        heading(2, "Uniform random sample with replacement");

        p("An uniform random sample is a sample from a discrete population with " +
                "an uniform discrete density. A discrete uniform density assigns " +
                "equal probability mass function values to each outcome. " +
                "The previous example of throwing a fair dice is an example of " +
                "discrete uniform density, since it assigns equal value " +
                "\\(\\frac{1}{6}\\) to each possible outcome \\( x_i \\). ");

        p("A sample with replacement is a sample where values of the sample can appear multiple " +
                "times. The expression \"with replacement\" can be misleading. The intuition " +
                "behind seems to follow from the following description of the process:");

        p("<i>We have a set of possible elements \\(x_i\\), each with assigned equal probability " +
                "of \\(p(x_i) \\). Take randomly one element from the set, according with " +
                "their probabilities (denote the taken element with \\(x_k\\)). " +
                "Replace the element taken from the set with a new element, which has " +
                "the same value as the element previously removed. At this stage we have again " +
                "a situation identical with the initial situation. Repeat the process of " +
                "taking elements from the original set, followed by replacing that element " +
                "with another similar element unit you collect the desired number of elements " +
                "for the sample." + "</i>");

        p("Of course, we don't have to replace effectively the element. Repeating " +
                "the process of throwing a fair dice multiple times is a sampling with " +
                "replacement (we don't remove and replace something during process). ");

        p("The algorithm for taking a discrete uniform sample with replacement is " +
                "fairly simple since it is based on a basic operation provided by " +
                "Java language (<code>java.util.Random.nextInt(int n);</code>)");

        int[] sample = Sampling.sampleWR(1000, 6);
        Var var = Index.newWrapOf(sample);
        draw(new Plot().add(new Histogram(var).bins(6).prob(false)), 500, 200);

        code("        int[] sample = Sampling.sampleWR(1000, 6);\n" +
                "        Var var = Index.newWrapOf(sample);\n" +
                "        draw(new Plot().add(new Histogram(var).bins(6).prob(false)), 500, 200);\n");

        p("In the presented histogram we see frequencies obtained be taking a sample " +
                "of size 1000 of the fair-dice process outcomes. We note that the " +
                "proportions are somehow equal, which is according with our assumption " +
                "that each element have equal probability. However the values from sample " +
                "looks random:");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append(sample[i]).append(", ");
            if ((i + 1) % 30 == 0) sb.append("\n");
        }
        sb.append("...");
        code(sb.toString());

        heading(2, "Uniform random sample without replacement");

        p("Sampling without replacement implies that during the process of selection " +
                "of elements which will be collected in teh desired sample, the " +
                "chosen elements are not available again for further selection. " +
                "A process like this appears when are drawn numbers for lottery. " +
                "The already selected numbers are not available for selection again. " +
                "Another expression which, at least for me, seems much clearer " +
                "is \"without repetition\". So, with replacement means a sample " +
                "with repetition, and without replacement is a sample without " +
                "repetition of the sampled values. ");

        p("Let's suppose that we have a lottery 6/49. At each draw 6 unique numbers in " +
                "range 1-69 are drawn. We take 100 draws and simulate:");

        final int TRIALS = 100;
        final int SAMPLE_SIZE = 6;
        final int POPULATION_SIZE = 49;
        Var[] vars = new Var[2];
        vars[0] = Numeric.newEmpty(SAMPLE_SIZE * TRIALS).withName("lottery trial");
        vars[1] = Numeric.newEmpty(SAMPLE_SIZE * TRIALS).withName("winning number");

        for (int i = 0; i < TRIALS; i++) {
            int[] numbers = Sampling.sampleWOR(SAMPLE_SIZE, POPULATION_SIZE);
            for (int j = 0; j < numbers.length; j++) {
                vars[0].setValue(i * SAMPLE_SIZE + j, i + 1);
                vars[1].setValue(i * SAMPLE_SIZE + j, numbers[j] + 1);
            }
        }

        final Frame df = SolidFrame.newWrapOf(SAMPLE_SIZE * TRIALS, Arrays.asList(vars));
        draw(new Plot().add(new Points(df.var(0), df.var(1)).pch(1).color(34).sz(2)), 600, 300);

        code("        final int TRIALS = 100;\n" +
                "        final int SAMPLE_SIZE = 6;\n" +
                "        final int POPULATION_SIZE = 49;\n" +
                "        Var[] vars = new Var[2];\n" +
                "        vars[0] = Numeric.newEmpty(SAMPLE_SIZE * TRIALS).withName(\"lottery trial\");\n" +
                "        vars[1] = Numeric.newEmpty(SAMPLE_SIZE * TRIALS).withName(\"winning number\");\n" +
                "\n" +
                "        for (int i = 0; i < TRIALS; i++) {\n" +
                "            int[] numbers = Sampling.sampleWOR(SAMPLE_SIZE, POPULATION_SIZE);\n" +
                "            for (int j = 0; j < numbers.length; j++) {\n" +
                "                vars[0].setValue(i * SAMPLE_SIZE + j, i + 1);\n" +
                "                vars[1].setValue(i * SAMPLE_SIZE + j, numbers[j] + 1);\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        final Frame df = SolidFrame.newWrapOf(SAMPLE_SIZE * TRIALS, Arrays.asList(vars));\n" +
                "        draw(new Plot().add(new Points(df.var(0), df.var(1)).pch(1).color(34).sz(2)), 600, 300);\n");

        p("There is random in that plot. Everywhere. A summary on the data, however, " +
                "can give us enough clues to understand that the densities " +
                "of those numbers are still symmetric and somehow uniform. ");

        Summary.summary(df);

        heading(2, "Weighted random sample with replacement");

        p("A weighted sample is a discrete random sample which does not have an " +
                "uniform density. A well-know example used in introductory " +
                "probability classes is the biased coin. A biased coin is " +
                "a coin which is not fair. That mean the probability after " +
                "a draw to see a HEAD is different than the probability to see " +
                "a TAIL. ");

        p("Let's suppose we have a biased coin with \\( p(coin=HEAD) = 0.6\\) and " +
                "\\( p(coin=TAIL)=0.4\\). We simulate this experiment make a lot of " +
                "draws. The law of large numbers tells us that after a reasonable " +
                "amount of repetitions the value of the plugged in estimator will be " +
                "close to the value of the estimated population parameter. ");

        p("During the experiment we will throw the coin 300 times and we " +
                "will plot the plugin estimate which is the number of times HEAD is " +
                "drawn divided by the number of experiments. ");

        RandomSource.setSeed(1);
        final Var index = Index.newSeq(1, 1000);
        final Var value = Numeric.newEmpty(1000);
        double count = 0;
        double total = 0;
        for (int i = 0; i < 300; i++) {
            int[] samples = Sampling.sampleWeightedWR(1, new double[]{0.6, 0.4});
            if (samples[0] == 0) count++;
            total++;
            value.setValue(i, count / total);
        }
        draw(new Plot()
                .add(new ABLine(0.6, true))
                        .add(new Lines(index, value).color(2).lwd(1.5f))
                .yLim(0, 1)
                .xLab("experiment no")
                        .yLab("HEAD/TOTAL")
        );

        code("        RandomSource.setSeed(1);\n" +
                "        final Var index = Index.newSeq(1, 1000);\n" +
                "        final Var value = Numeric.newEmpty(1000);\n" +
                "        double count = 0;\n" +
                "        double total = 0;\n" +
                "        for (int i = 0; i < 300; i++) {\n" +
                "            int[] samples = Sampling.sampleWeightedWR(1, new double[]{0.6, 0.4});\n" +
                "            if (samples[0] == 0) count++;\n" +
                "            total++;\n" +
                "            value.setValue(i, count / total);\n" +
                "        }\n" +
                "        draw(new Plot()\n" +
                "                .add(new ABLine(0.6, true))\n" +
                "                .add(new Lines(index, value).color(2).lwd(1.5f))\n" +
                "                .yLim(0, 1)\n" +
                "                .xLab(\"experiment no\")\n" +
                "                .yLab(\"HEAD/TOTAL\")\n" +
                "        );\n");

        p("From the previous function line we see that the plugged in estimated value " +
                "of the probability of HEAD has a large variation at the beginning " +
                "of our experiment. However, as number of trials increases we have " +
                "clear reasoning to confirm that the coin is biased, since " +
                "the variation decrease, the estimator converge to value 0.6 which " +
                "is not what we could expect from a fair coin. ");

        p("The sampling algorithm implemented is one of the family of alias method, " +
                "specifically is called Vose algorithm and is one of the linear " +
                "algorithms used today for discrete weighted random sampling. " +
                "See more about this algorithm here: " +
                "<a href=\"http://en.wikipedia.org/wiki/Alias_method\">http://en.wikipedia.org/wiki/Alias_method</a>.");

        heading(2, "Weighted random sample without replacement");

        p("This is the last type of discrete random sampling covered here. What we are " +
                "interested in is to generate samples without replacement (no repetition), " +
                "from a discrete density different than uniform density. ");

        p("We consider again the lottery experiment. However we want to simulate " +
                "a situation when some winning numbers are preferred over the others. " +
                "Let's suppose that our lottery favors big numbers, >= that 40. " +
                "And some other numbers, ones in interval 8-12 have " +
                "smaller probability than one would expect from a fair lottery. " +
                "We simulate the experiment with a weighted sampling technique. ");

        vars[0] = Numeric.newEmpty(SAMPLE_SIZE * TRIALS).withName("loaded lottery");
        vars[1] = Numeric.newEmpty(SAMPLE_SIZE * TRIALS).withName("winning number");

        double[] prob = new double[49];
        for (int i = 0; i < prob.length; i++) {
            if (i - 1 >= 8 && i - 1 <= 12) {
                prob[i] = 3;
                continue;
            }
            if (i - 1 >= 40) {
                prob[i] = 30;
                continue;
            }
            prob[i] = 10;
        }
        for (int i = 0; i < TRIALS; i++) {
            int[] numbers = Sampling.sampleWeightedWOR(SAMPLE_SIZE, prob);
            for (int j = 0; j < numbers.length; j++) {
                vars[0].setValue(i * SAMPLE_SIZE + j, i + 1);
                vars[1].setValue(i * SAMPLE_SIZE + j, numbers[j] + 1);
            }
        }

        final Frame df2 = SolidFrame.newWrapOf(SAMPLE_SIZE * TRIALS, vars);
        draw(new Plot()
                        .add(new Points(df2.var(0), df2.var(1)))
                        .pch(Index.newScalar(1))
                        .sz(Numeric.newScalar(2))
                        .color(34),
                600, 300
        );

        code("        vars[0] = Numeric.newEmpty(SAMPLE_SIZE * TRIALS).withName(\"loaded lottery\");\n" +
                "        vars[1] = Numeric.newEmpty(SAMPLE_SIZE * TRIALS).withName(\"winning number\");\n" +
                "\n" +
                "        double[] prob = new double[49];\n" +
                "        for (int i = 0; i < prob.length; i++) {\n" +
                "            if (i - 1 >= 8 && i - 1 <= 12) {\n" +
                "                prob[i] = 3;\n" +
                "                continue;\n" +
                "            }\n" +
                "            if (i - 1 >= 40) {\n" +
                "                prob[i] = 30;\n" +
                "                continue;\n" +
                "            }\n" +
                "            prob[i] = 10;\n" +
                "        }\n" +
                "        for (int i = 0; i < TRIALS; i++) {\n" +
                "            int[] numbers = Sampling.sampleWeightedWOR(SAMPLE_SIZE, prob);\n" +
                "            for (int j = 0; j < numbers.length; j++) {\n" +
                "                vars[0].setValue(i * SAMPLE_SIZE + j, i + 1);\n" +
                "                vars[1].setValue(i * SAMPLE_SIZE + j, numbers[j] + 1);\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        final Frame df2 = SolidFrame.newWrapOf(SAMPLE_SIZE * TRIALS, vars);\n" +
                "        draw(new Plot()\n" +
                "                        .add(new Points(df2.var(0), df2.var(1)))\n" +
                "                        .pch(Index.newScalar(1))\n" +
                "                        .sz(Numeric.newScalar(2))\n" +
                "                        .color(34),\n" +
                "                600, 300\n" +
                "        );\n");

        p("This time we see more than random values there. There is a clear more dense " +
                "region in the upper side of the graph. Also, we can note, perhaps " +
                "not as very clear, a stripe with low density somewhere under y=13. ");

        p("To clarify a kernel density plot which approximates the population " +
                "density would help more. ");

        draw(new Plot()
                        .add(new FunctionLine(new KDE(df2.var("winning number"), 3).getPdf())
                                .color(Index.newScalar(1)))
                        .xLab("winning numbers")
                        .yLab("kernel probability density")
                        .xLim(-10, 60).yLim(0, .05),
                600, 300
        );

        p("Rapaio implementation of this last algorithm is based on a " +
                "wonderful algorithm invented by Efraimidis-Spirakis. " +
                "<a href=\"http://link.springer.com/content/pdf/10.1007/978-0-387-30162-4_478.pdf\">" +
                "http://link.springer.com/content/pdf/10.1007/978-0-387-30162-4_478.pdf</a>. ");

        p("Note: the sole purpose of this tutorial is to show what and how can " +
                "be done with rapaio toolbox library. ");

        p(" >>>This tutorial is generated with Rapaio document printer facilities.<<< ");
    }

}
