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
import rapaio.core.sample.DiscreteSampling;
import rapaio.data.*;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.*;
import rapaio.workspace.Summary;

import java.io.IOException;

import static rapaio.workspace.W.*;

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

        p("In statistics, sampling is the process which selects a subset of observations "
                + "from within a statistical population. There are many types of sampling "
                + "methods which can be employed in statistics, like simple random sampling, "
                + "stratified sampling, systematic sampling and so on. ");
        p("However the purpose of this tutorial is to present four algorithms which "
                + "are used as building blocks by various statistical sampling methods. "
                + "The purpose of this tutorial is to present discrete random sampling methods.");

        p("<b>Definition:</b>");
        p("<i>A statistical distribution whose values can take only discrete values is "
                + "called a discrete distribution. </i>");

        p("A discrete probability distribution function is completely described by "
                + "the set of possible values the random variable can take and "
                + "by the probabilities assigned to each getValue.");

        p("An example of discrete distribution is the process of throwing a standard dice. "
                + "We have a finite set of outcomes of the process (6 possible values) and "
                + "a probability function getValue associated with each output (for a fair dice we "
                + "can associate probability \\( p(x_i) = \\frac{1}{6} \\)).");

        p("Drawing a sample from a distribution is the process of selecting some values "
                + "from the possible values \\( x_i,i=1..n \\) according with their probabilities. "
                + "Sampling is useful for far many purposes that I can describe here. "
                + "Among some very important scenarios are the simulation and the fapt that "
                + "working with a smaller sample than the given population is faster "
                + "and provides enough information for analysis. For the latter "
                + "example we can note that working with the heights and "
                + "weights of the all the people from the world (assuming that this "
                + "information is possible to collect, there are probably over 7 billions "
                + "records) is much harder than working with a sample much smaller. ");

        heading(2, "Uniform random sample with replacement");

        p("An uniform random sample is a sample from a discrete population with "
                + "an uniform distribution. A discrete uniform distribution is a distribution "
                + "which assigns equal probability mass function values to each outcome. "
                + "The previous example of throwing a fair dice is an example of uniform "
                + "distribution, since it assigns equal getValue \\(\\frac{1}{6}\\) to each "
                + "possible outcome \\( x_i \\). ");

        p("A sample with replacement is a sample where values of the sample can appear multiple "
                + "times. The expression \"with replacement\" can be misleading. The intuition "
                + "behind seems to follow from the following description of the process:");

        p("<i>We have a set of possible elements \\(x_i\\), each with assigned equal probability "
                + "of \\(p(x_i) \\). Take randomly one element from the set, according with "
                + "their probabilities (denote the taken element with \\(x_k\\)). "
                + "Replace the element taken from the set with a new element, which has "
                + "the same getValue as the element previously removed. At this stage we have again "
                + "a situation identical with the initial situation. Repeat the process of "
                + "taking elements from the original set, followed by replacing that element "
                + "with another similar element unit you collect the desired number of elements "
                + "for the sample."
                + "</i>");

        p("Of course, we don't have to replace effectively the element. Repeating "
                + "the process of throwing a fair dice multiple times is a sampling with "
                + "replacement (we don't remove and replace something during process). ");

        p("The algorithm for taking a discrete uniform sample with replacement is "
                + "fairly simple since it is based on a basic operation provided by "
                + "Java language (<code>java.util.Random.nextInt(int n);</code>)");

        code("        int[] sample = new DiscreteSamplingWR(6).sample(1000);\n"
                + "        Vector vector = new NumericVector(\"fair-dice\", sample);\n"
                + "        draw(new Histogram(vector, 6, false), 500, 200);\n");

        DiscreteSampling ds = new DiscreteSampling();
        int[] sample = ds.sampleWR(1000, 6);
        Vector vector = Vectors.newIdxFrom(sample);
        draw(new Plot().add(new Histogram(vector, 6, false)), 500, 200);

        p("In the presented histogram we see frequencies obtained be taking a sample "
                + "of size 1000 of the fair-dice process outcomes. We note that the "
                + "proportions are somehow equal, which is according with our assumption "
                + "that each element have equal probability. However the values from sample "
                + "looks random:");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append(sample[i]).append(", ");
            if ((i + 1) % 30 == 0)
                sb.append("\n");
        }
        sb.append("...");
        code(sb.toString());

        heading(2, "Uniform random sample without replacement");

        p("Sampling without replacement implies that during the process of selection "
                + "of elements which will be collected in teh desired sample, the "
                + "chosen elements are not available again for further selection. "
                + "A process like this appears when are drawn numbers for lottery. "
                + "The already selected numbers are not available for selection again. "
                + "Another expression which, at least for me, seems much clearer "
                + "is \"without repetition\". So, with replacement means a sample "
                + "with repetition, and without replacement is a sample without "
                + "repetition. ");

        p("Let's suppose that we have a lottery 6/49. At each draw 6 unique numbers in "
                + "range 1-69 are drawn. We take 100 draws and simulate:");

        code("        final int TRIALS = 100;\n" +
                "        final int SAMPLE_SIZE = 6;\n" +
                "        final int POPULATION_SIZE = 49;\n" +
                "        Vector[] vectors = new Vector[2];\n" +
                "        vectors[0] = new Numeric(SAMPLE_SIZE * TRIALS);\n" +
                "        vectors[1] = new Numeric(SAMPLE_SIZE * TRIALS);\n" +
                "\n" +
                "        for (int i = 0; i < TRIALS; i++) {\n" +
                "            int[] numbers = new DiscreteSamplingWOR(POPULATION_SIZE).sample(SAMPLE_SIZE);\n" +
                "            for (int j = 0; j < numbers.length; j++) {\n" +
                "                vectors[0].setValue(i * SAMPLE_SIZE + j, i + 1);\n" +
                "                vectors[1].setValue(i * SAMPLE_SIZE + j, numbers[j] + 1);\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        final Frame df = new SolidFrame(SAMPLE_SIZE * TRIALS, vectors, new String[]{\"lottery trial\", \"winning number\"});\n" +
                "        draw(new Plot()\n" +
                "                .add(new Points(df.getCol(0), df.getCol(1))\n" +
                "                        .setPchIndex(1)\n" +
                "                        .setColorIndex(34)\n" +
                "                        .setSizeIndex(2)),\n" +
                "                600, 300);\n");

        final int TRIALS = 100;
        final int SAMPLE_SIZE = 6;
        final int POPULATION_SIZE = 49;
        Vector[] vectors = new Vector[2];
        vectors[0] = new Numeric(SAMPLE_SIZE * TRIALS);
        vectors[1] = new Numeric(SAMPLE_SIZE * TRIALS);

        for (int i = 0; i < TRIALS; i++) {
            int[] numbers = ds.sampleWOR(SAMPLE_SIZE, POPULATION_SIZE);
            for (int j = 0; j < numbers.length; j++) {
                vectors[0].setValue(i * SAMPLE_SIZE + j, i + 1);
                vectors[1].setValue(i * SAMPLE_SIZE + j, numbers[j] + 1);
            }
        }

        final Frame df = new SolidFrame(SAMPLE_SIZE * TRIALS, vectors, new String[]{"lottery trial", "winning number"});
        draw(new Plot()
                        .add(new Points(df.col(0), df.col(1))
                                .setPch(1)
                                .setCol(34)
                                .setSize(2)),
                600, 300
        );

        p("There is random in that plot. Everywhere. A summary on the data, however, "
                + "can give us enough clues to understand that the distribution "
                + "of those numbers are still symmetric and somehow uniform.");

        Summary.summary(df);

        heading(2, "Weighted random sample with replacement");

        p("A weighted sample is a discrete random sample which does not have an "
                + "uniform distribution. A well-know example used in almost "
                + "all introductory probability classes is the biased coin. A biased coin "
                + "is a coin which is not fair. That mean the probability after a draw "
                + "to see HEAD is different than the probability to see a TAIL. ");

        p("Let's suppose we have a biased coin with \\( p(coin=HEAD) = 0.6\\) and "
                + "\\( p(coin=TAIL)=0.4\\). We simulate this experiment and we draw "
                + "the coins a lot of times. The law of large numbers tells us "
                + "that after a reasonable amount of repetitions the plugged in estimator "
                + "will tend to go the the population parameter estimated. ");

        p("During the experiment we will throw the coin 300 times and we "
                + "will plot the plugin estimate which is the number of times HEAD is "
                + "drawn divided by the number of experiments. ");

        code("        RandomSource.setSeed(1);\n" +
                "        final Vector getIndex = Vectors.newSeq(1, 1000, 1);\n" +
                "        final Vector getValue = new Numeric(1000);\n" +
                "        double count = 0;\n" +
                "        double total = 0;\n" +
                "        for (int i = 0; i < 300; i++) {\n" +
                "            int[] samples = new DiscreteWeightedSamplingWR(new double[]{0.6, 0.4}).sample(1);\n" +
                "            if (samples[0] == 0)\n" +
                "                count++;\n" +
                "            total++;\n" +
                "            getValue.setValue(i, count / total);\n" +
                "        }\n" +
                "        draw(new Plot()\n" +
                "                .add(new ABLine(0.6, true))\n" +
                "                .add(new Lines(getIndex, getValue)\n" +
                "                        .setColorIndex(2)\n" +
                "                        .setLwd(1.5f)\n" +
                "                )\n" +
                "                .setYLim(0, 1)\n" +
                "                .setBottomLabel(\"experiment no\")\n" +
                "                .setYLab(\"HEAD/TOTAL\"));\n");

        RandomSource.setSeed(1);
        final Vector index = Vectors.newSeq(1, 1000, 1);
        final Vector value = new Numeric(1000);
        double count = 0;
        double total = 0;
        for (int i = 0; i < 300; i++) {
            int[] samples = new DiscreteSampling().sampleWeightedWR(1, new double[]{0.6, 0.4});
            if (samples[0] == 0)
                count++;
            total++;
            value.setValue(i, count / total);
        }
        draw(new Plot()
                .add(new ABLine(0.6, true))
                .add(new Lines(index, value)
                                .setCol(2)
                                .setLwd(1.5f)
                )
                .setYLim(0, 1)
                .setXLab("experiment no")
                .setYLab("HEAD/TOTAL"));

        p("From the previous function line we see that the plugged in estimate "
                + "of the probability of HEAD has a large variation at the beginning "
                + "of our experiment. However, as number of trials increases we have "
                + "clear reasoning to confirm that the coin is biased, since "
                + "the variation decrease, the estimator converge to getValue 0.6 which "
                + "is not what we could expect from a fair coin. ");

        p("The sampling algorithm implemented is one of the family of alias method, "
                + "specifically is called Vose algorithm and is one of the linear "
                + "algorithms used today for discrete weighted random sampling. See more "
                + "about this algorithm here: <a href=\"http://en.wikipedia.org/wiki/Alias_method\">"
                + "http://en.wikipedia.org/wiki/Alias_method</a>.");

        heading(2, "Weighted random sample without replacement");

        p("This is the last getType of discrete random sampling covered here. What we are "
                + "interested in is to generate samples without replacement (no repetition), "
                + "from a discrete distribution different than uniform distribution. ");

        p("We consider again the lottery experiment. However we want to simulate "
                + "a situation when some winning numbers are preferred over the others. "
                + "Let's suppose that our lottery favors big numbers, >= that 40. "
                + "And some other numbers, ones in interval 8-12 have "
                + "smaller probability than usual. We repeat the experiment with a "
                + "weighted sampling technique.");

        code("        vectors[0] = new Numeric(SAMPLE_SIZE * TRIALS);\n" +
                "        vectors[1] = new Numeric(SAMPLE_SIZE * TRIALS);\n" +
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
                "            int[] numbers = new DiscreteWeightedSamplingWOR(prob).sample(SAMPLE_SIZE);\n" +
                "            for (int j = 0; j < numbers.length; j++) {\n" +
                "                vectors[0].setValue(i * SAMPLE_SIZE + j, i + 1);\n" +
                "                vectors[1].setValue(i * SAMPLE_SIZE + j, numbers[j] + 1);\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        final Frame df2 = new SolidFrame(SAMPLE_SIZE * TRIALS, vectors, new String[]{\"loaded lottery\", \"winning number\"});\n" +
                "        draw(new Plot()\n" +
                "                .add(new Points(df2.getCol(0), df2.getCol(1)))\n" +
                "                .setPchIndex(Vectors.newIdxOne(1))\n" +
                "                .setColorIndex(Vectors.newIdxOne(34))\n" +
                "                .setSizeIndex(Vectors.newNumOne(2)),\n" +
                "                600, 300);\n");

        vectors[0] = new Numeric(SAMPLE_SIZE * TRIALS);
        vectors[1] = new Numeric(SAMPLE_SIZE * TRIALS);

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
            int[] numbers = ds.sampleWeightedWOR(SAMPLE_SIZE, prob);
            for (int j = 0; j < numbers.length; j++) {
                vectors[0].setValue(i * SAMPLE_SIZE + j, i + 1);
                vectors[1].setValue(i * SAMPLE_SIZE + j, numbers[j] + 1);
            }
        }

        final Frame df2 = new SolidFrame(SAMPLE_SIZE * TRIALS, vectors, new String[]{"loaded lottery", "winning number"});
        draw(new Plot()
                        .add(new Points(df2.col(0), df2.col(1)))
                        .setPch(Vectors.newIdxOne(1))
                        .setSize(Vectors.newNumOne(2))
                        .setCol(34),
                600, 300
        );

        p("This time we see more than random there. There is a clear more dense "
                + "region in the upper side of the graph. Also, we can note, perhaps "
                + "not as very clear, a stripe with low density somewhere under y=13. ");

        p("To clarify a kernel density plot which approximates the population "
                + "density would help more.");

        draw(new Plot()
                        .add(new FunctionLine(new KDE(df2.col("winning number"), 3).getPdf())
                                .setCol(Vectors.newIdxOne(1)))
                        .setXLab("winning numbers")
                        .setYLab("kernel probability density")
                        .setXLim(-10, 60).setYLim(0, .05),
                600, 300
        );

        p("Rapaio implementation of this last algorithm is based on a wonderful algorithm "
                + "invented by Efraimidis-Spirakis. "
                + "<a href=\"http://link.springer.com/content/pdf/10.1007/978-0-387-30162-4_478.pdf\">"
                + "http://link.springer.com/content/pdf/10.1007/978-0-387-30162-4_478.pdf</a>. ");
        p("Note: the sole purpose of this tutorial is to show what and how it can "
                + "be done with Rapaio toolbox library. ");

        p(" >>>This tutorial is generated with Rapaio document printer facilities.<<< ");
    }

}
