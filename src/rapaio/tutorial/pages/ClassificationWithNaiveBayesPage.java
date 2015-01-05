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

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.WS.heading;
import static rapaio.WS.p;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class ClassificationWithNaiveBayesPage implements TutorialPage {

    @Override
    public String getPageName() {
        return "NaiveBayesClassification";
    }

    @Override
    public String getPageTitle() {
        return "Classification with NaiveBayes";
    }

    @Override
    public void render() throws IOException, URISyntaxException {

        heading(1, "Classification with NaiveBayesClassifier");

        heading(2, "Probabilistic Bayesian models");

        p("In general, a probability model for a classifier is a conditional model \\( p(t|x_1,x_2,..,x_n) \\), " +
                "where the target variable \\(t\\) has a small number of labels and " +
                "the probability is conditioned on the input variables \\(x_1, x_2,.., x_n\\). " +
                "This means that we try to model which is the probability a a class label given the input data.");

        p("Using Bayes' theorem, we can write the formula for the conditional " +
                "in the following form ");

        p("$$ p(t|x_1,x_2,..,x_n) = \\frac{p(t)p(x_1,x_2,..,x_n|t)}{p(x_1,x_2,..,x_n)} \\propto p(t)p(x_1,x_2,..,x_n|t) $$");

        p(" and because the denominator is the same for all labels we are interested only in the numerator of the fraction. ");

        p("According with the Bayesian terminology we have");

        p("$$ posterior = \\frac{prior \\times likelihood}{evidence} \\propto prior \\times likelihood $$");

        p("Now we should note that the numerator ( \\( prior \\times likelihood \\) ) is the " +
                "joint probability of the target and input features. ");

        p("$$ p(t)p(x_1,x_2,..,x_n|t) = p(t,x_1,x_2,..,x_n) $$");

        p("This can easily verified using the definition of conditional probability. Thus, on can usually say that " +
                "for a Bayesian model one is aimed to find the joint probability of inputs and outputs.");

        p("Using chain rule we can go further and split the joint probability into a product of conditional probabilities. ");

        p("$$ p(t|x_1,x_2,..,x_n) \\propto p(t)p(x_1,x_2,..,x_n|t) = p(t,x_1,x_2,..x_n) = p(t)p(x_1|t)p(x_2|t,x_1)..p(x_n|t,x_1,x_2,..,x_{n-1}) $$");

        heading(2, "Intuition behind NaiveBayesClassifier");

        p("In front of the complex problem of determining all those conditional probabilities, " +
                "the NaiveBayes approach is to simplify formulas using an independence assumption. " +
                "The NaiveBayes assumption is that each feature is independent of one another. " +
                "This is often not true, however the NaiveBayes models offer good accuracy. " +
                "One reason why this happens is that the natural data has a simple structure in general. ");

        p("The independence assumption means that when computing the conditional probabilities " +
                "of the input features the conditional terms of the other features can be eliminated. ");

        p("$$ p(x_i|t,x_1,x_2,..,x_{i-1}) = p(x_i|t) $$");

        p("This looks much simpler. The start model for a NaiveBayes looks like:");

        p("$$ p(t|x_1,x_2,..,x_n) \\propto p(t)\\prod_{i=1}^{n}p(x_i|t) $$");

    }
}
