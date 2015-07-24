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

package rapaio.tutorial.pages;

import rapaio.core.distributions.Normal;
import rapaio.core.distributions.StudentT;
import rapaio.graphics.Plotter;

import java.io.IOException;

import static rapaio.sys.WS.*;
import static rapaio.graphics.Plotter.funLine;
import static rapaio.graphics.Plotter.plot;
import static rapaio.graphics.Plotter.color;

/**
 * @author Aurelian Tutuianu
 */
@Deprecated
public class StudentTDistribution implements TutorialPage {

    @Override
    public String getPageName() {
        return "StudentTDistribution";
    }

    @Override
    public String getPageTitle() {
        return "Short story of a densities: Student's T";
    }

    @Override
    public void render() throws IOException {

        heading(1, "Short story of a densities: Student's T");

        heading(3, "Foreword");

        p("I have always believed that knowing the history of scientific achievements " +
                "is a must for anybody. This story is about William Sealy Gosset and " +
                "it's famous t probability distribution. ");

        heading(3, "The context");

        p("When the story begins, the science of mathematical statistics lived its first " +
                "years as a serious scientific discipline, mainly through the efforts made " +
                "by Karl Pearson, who was the founder of the first university statistics " +
                "department in the world. ");

        p("After graduating on chemistry and mathematics at New College, Oxford, Gosset " +
                "joined the brewery of Arthur Guinness and Son, and started to apply his " +
                "statistical knowledge in brewery and on the selection of barley. ");

        p("Note that at that time the methods available involved mainly the practice of " +
                "Karl Pearson and some other eminent statisticians, around the normal " +
                "densities and Central Limit Theorem. ");

        heading(3, "Probability density and central limit theorem");

        p("The probability density function of a standard normal distribution "
                + "looks like this: ");

        draw(funLine(new Normal()::pdf, Plotter.color(1)).xLim(-4, 4).yLim(0, 0.5));

        p("To understand it's mechanics you have to imagine a process, which produces a " +
                "numeric value. If the only thing you would know about the value of the " +
                "number to be produced is it's range, than you don't know much. ");

        p("Statistics adds a new dimension to that range: your expectation, aka. \"which " +
                "values are expected to be produced with a high probability, which values " +
                "might be produced with a low or medium probability and which not\". " +
                "Here the probability has the meaning of frequency. ");

        p("The standard normal distribution from the above graph tells us, for example, " +
                "that we expect that the value produced by our process to be close to 0. " +
                "It will not be exactly 0, but very often the values will be near 0. " +
                "There is a lot o information contained in this density function. " +
                "For example we can say that we expect that produced to be in interval [-2,+2] " +
                "in most of the cases. More precisely we can say that 95% of the values will " +
                "lay in this interval. ");

        p("Central Limit Theorem "
                + "says something like: \"if you repeat the event enough times independently and "
                + "in the same conditions, the mean of the values produced "
                + "will follow a normal densities\". ");

        p("This theory is wonderful due to its generality. However it has a fundamental flow: "
                + "it doesn't work well if you don't repeat the event many times. ");

        p("For scientists like Pearson that was not a problem, since they managed in many cases "
                + "to get enough data. But for Gosset that was a big trouble. It is not easy to see "
                + "that you can't repeat the event of barley selection many times. That "
                + "barley have to be selected, have to grow and be measured after harvest. It was too costly "
                + "to repeat the experiments enough time. ");

        draw(plot()
                .funLine(new Normal()::pdf, Plotter.color(1))
                .funLine(new StudentT(3)::pdf)
                .xLim(-4, 4)
                .yLim(0, 0.5));
        p(">>>This tutorial is generated with Rapaio document printer facilities.<<<");
    }
}
