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
 */

package rapaio.tutorial.pages;

import rapaio.core.RandomSource;
import rapaio.core.sample.Sampler;
import rapaio.core.sample.SamplingTool;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Legend;
import rapaio.graphics.plot.ROCCurve;
import rapaio.ml.classifier.ClassifierFit;
import rapaio.ml.classifier.boost.AdaBoostSAMMEClassifier;
import rapaio.ml.classifier.boost.GBTClassifier;
import rapaio.ml.classifier.rule.OneRule;
import rapaio.ml.classifier.tree.CForest;
import rapaio.ml.eval.ConfusionMatrix;
import rapaio.ml.eval.ROC;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static rapaio.WS.*;
import static rapaio.ws.Summary.summary;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class ROCCurvesPage implements TutorialPage {

    @Override
    public String getPageName() {
        return "ROCCurves";
    }

    @Override
    public String getPageTitle() {
        return "ROC Curves";
    }

    @Override
    public void render() throws IOException, URISyntaxException {
        RandomSource.setSeed(1234);

        heading(1, "ROC Curves Tutorial Page");

        p("This tutorial page describes how you can build ROC curves graphs with " +
                "rapaio library.");

        heading(2, "Data set");

        p("Data set used in this tutorial page is the classical UCI spam data set. " +
                "We load the data set and we split randomly in two pieces. " +
                "The first sample will be used for training purposes and it will have ~ 0.66 " +
                "of the data, the second sample will be used for testing our model. ");

        RandomSource.setSeed(2718);
        final Frame spam = Datasets.loadSpamBase().mapVars("0~3,spam");
        List<Frame> samples = SamplingTool.randomSampleSlices(spam, 0.6);
        final Frame train = samples.get(0);
        final Frame test = samples.get(1);

        code("        RandomSource.setSeed(2718);\n" +
                "        final Frame spam = Datasets.loadSpamBase().mapVars(\"0~3,spam\");\n" +
                "        List<Frame> samples = Sampling.randomSampleSlices(spam, 0.6);\n" +
                "        final Frame train = samples.get(0);\n" +
                "        final Frame test = samples.get(1);\n");

        p("If you are not aware how the data for spam data looks like that what you will have to know is " +
                "that it consists of many numerical attributes used to predict a nominal " +
                "attribute called \\(spam\\)");

        p("Thus we know there are 2788 instances classified as \\(ham\\), codified by value 0 (not spam), " +
                "and 1813 instances codified by 1, which denotes spam emails. There are a lot of " +
                "numeric features in this data set. We use " +
                "only the first 4 numerical features for prediction.");

        summary(spam);

        p("Now we can do some predictions.");

        heading(2, "Binary classification");

        p("We will fit 4 models to the data in order to do predictions. " +
                "We will use the train test which consists of 0.66 of our initial data. " +
                "For testing how well the model predicts we use the remaining data.");

        heading(4, "OneRule");

        p("This first model is one of the simplest possible models. It basically learns a decision tree "
                + "with a single level and possible more than two children. " +
                "For documentation about this algorithm you can check the original " +
                "paper <a href=\"http://link.springer.com/article/10.1023/A:1022631118932\">"
                + "Holte, R.C. Very Simple Classification Rules Perform Well on Most Commonly Used Datasets. "
                + "Machine Learning 11, 63-91 (1993).</a>");

        OneRule oneRule = new OneRule();
        oneRule.learn(train, "spam");
        ClassifierFit crOneRule = oneRule.predict(test);

        code("        OneRule oneRule = new OneRule();\n" +
                "        oneRule.learn(train, \"spam\");\n" +
                "        ClassifierFit crOneRule = oneRule.predict(test);\n");

        p("One of the most used ways to check the performance of a classifier is the accuracy. "
                + "Accuracy is the percentage of cases with correct prediction from total number of cases. "
                + "With rapaio library one way to compute the accuracy is " +
                "to summarize the confusion matrix.");

        new ConfusionMatrix(test.getVar("spam"), crOneRule.firstClasses()).summary();

        code("        new ConfusionMatrix(test.getVar(\"spam\"), crOneRule.firstClasses()).summary();\n");

        heading(4, "Random Forest");

        p("The second prediction model is a random forest with 20 random trees. ");

        CForest rf = CForest.newRF(20, 10, new Sampler.Identity());
        rf.learn(train, "spam");
        ClassifierFit crRF = rf.predict(test);

        code("        CForest rf = CForest.newRF(20, 10, 0);\n" +
                "        rf.learn(train, \"spam\");\n" +
                "        ClassifierFit crRF = rf.predict(test);\n");

        new ConfusionMatrix(test.getVar("spam"), crRF.firstClasses()).summary();

        heading(4, "AdaBoost.SAMME");

        p("The third prediction model is a boosting algorithm called AdaBoost.SAMME. " +
                "This model uses decision stumps as a weak learners, " +
                "and builds 50 boosting iterations. " +
                "The following code shows how one can achieve that using rapaio.");

        AdaBoostSAMMEClassifier ab = new AdaBoostSAMMEClassifier().withRuns(20);
        ab.learn(train, "spam");
        ClassifierFit crAB = ab.predict(test);

        code("        AdaBoostSAMMEClassifier ab = new AdaBoostSAMMEClassifier().withRuns(20);\n" +
                "        ab.learn(train, \"spam\");\n" +
                "        ClassifierFit crAB = ab.predict(test);\n");

        new ConfusionMatrix(test.getVar("spam"), crAB.firstClasses()).summary();

        heading(4, "GBTClassifier");

        p("The fourth prediction model is another boosting algorithm called Gradient Boosting Tree. " +
                "This model uses decision trees as weak learners, and builds 20 boosting iterations. " +
                "The following code shows how one can achieve that using rapaio.");

        GBTClassifier gbt = new GBTClassifier().withRuns(20);
        gbt.learn(train, "spam");
        ClassifierFit crGBT = gbt.predict(test);

        code("        GBTClassifier gbt = new GBTClassifier().withRuns(20);\n" +
                "        gbt.learn(train, \"spam\");\n" +
                "        ClassifierFit crGBT = gbt.predict(test);\n");

        new ConfusionMatrix(test.getVar("spam"), crGBT.firstClasses()).summary();

        heading(2, "ROC Curves");

        p("When accuracy is used to compare the performance of some classifiers it is very often " +
                "the case that the comparison is misleading. In fact, it is very hard to find a single " +
                "tools which can be used to compare different models in a proper way. " +
                "This problem is so complicated that it has its own name called model selection");

        p("Among many other tools used for model selection there is a very interesting tool called ROC curves. " +
                "I will not explain what a ROC curve, ROC graph or area under ROC curve are. " +
                "There is enough literature on this topic. " +
                "Among many useful documents, I found one which gives crystal clear details " +
                "and explanations on ROC curves: <a href=\"http://binf.gmu.edu/mmasso/ROC101.pdf\">" +
                "Fawcett, T. (2004). ROC graphs: Notes and practical considerations for researchers. " +
                "Machine Learning.</a>");

        p("In order to draw ROC graphs for the previous models with rapaio you can use the " +
                "ROCCurve plot component which builds and draws a curve according with " +
                "a given computed ROC object. So, the first step is to build a ROC object " +
                "which contains all the necessary computation and information. " +
                "The following code does this.");

        ROC rocOR = new ROC(crOneRule.firstDensity().getVar("1"), test.getVar("spam"), "1");
        ROC rocRF = new ROC(crRF.firstDensity().getVar("1"), test.getVar("spam"), "1");
        ROC rocAB = new ROC(crAB.firstDensity().getVar("1"), test.getVar("spam"), "1");
        ROC rocGBT = new ROC(crGBT.firstDensity().getVar("1"), test.getVar("spam"), "1");

        draw(new Plot()
                        .add(new ROCCurve(rocOR).color(1))
                        .add(new ROCCurve(rocRF).color(2))
                        .add(new ROCCurve(rocAB).color(3))
                        .add(new ROCCurve(rocGBT).color(4))
                        .add(new Legend(0.6, 0.33,
                                new String[]{"onerule", "rf", "adaboost.m1", "gbt"},
                                new int[]{1, 2, 3, 4})),
                600, 400
        );

        code("        ROC rocOR = new ROC(crOneRule.firstDensity().getVar(\"1\"), test.getVar(\"spam\"), \"1\");\n" +
                "        ROC rocRF = new ROC(crRF.firstDensity().getVar(\"1\"), test.getVar(\"spam\"), \"1\");\n" +
                "        ROC rocAB = new ROC(crAB.firstDensity().getVar(\"1\"), test.getVar(\"spam\"), \"1\");\n" +
                "        ROC rocGBT = new ROC(crGBT.firstDensity().getVar(\"1\"), test.getVar(\"spam\"), \"1\");\n" +
                "        \n" +
                "        draw(new Plot()\n" +
                "                        .add(new ROCCurve(rocOR).color(1))\n" +
                "                        .add(new ROCCurve(rocRF).color(2))\n" +
                "                        .add(new ROCCurve(rocAB).color(3))\n" +
                "                        .add(new ROCCurve(rocGBT).color(4))\n" +
                "                        .add(new Legend(0.6, 0.33,\n" +
                "                                new String[]{\"onerule\", \"rf\", \"adaboost.m1\", \"gbt\"},\n" +
                "                                new int[]{1, 2, 3, 4})),\n" +
                "                600, 400\n" +
                "        );\n");

        p("As we can see, ROC objects are used to compute values for ROC curves, and " +
                "ROCCurve plot components are used to add these on a plot graphic. ");

        p("We can see that Random Forst model used exhibits a ROC graph which is " +
                "better than adaboost model most of the times in the conservative " +
                "area of the graph. Boosting algorithms tend to be a little better in the " +
                "liberal area. ");

        p("OneRule behaves sub-optimal as it was expected, being a very simple algorithm. ");

        p("Note however that the tutorial page topic is ROC Curve and the model used " +
                "in this tutorial are built only for exemplification purposes. " +
                "Those models are not at all optimal for the given problem, " +
                "and a comparison on these algorithms based only on this " +
                "tutorial is obviously wrong. ");

        p(">>>This tutorial is generated with Rapaio document printer facilities.<<<");
    }
}
