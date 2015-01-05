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

import rapaio.core.sample.Sampling;
import rapaio.data.Frame;
import rapaio.data.Index;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Lines;
import rapaio.graphics.plot.Points;
import rapaio.ml.classifier.CResult;
import rapaio.ml.classifier.tree.CForest;
import rapaio.ml.common.VarSelector;
import rapaio.ml.eval.ConfusionMatrix;
import rapaio.ws.Summary;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static rapaio.WS.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ClassificationWithRF implements TutorialPage {

    @Override
    public String getPageName() {
        return "ClassificationWithRandomForest";
    }

    @Override
    public String getPageTitle() {
        return "Classification with Random Forests";
    }

    @Override
    public void render() throws IOException, URISyntaxException {
        heading(1, "Classification with Random Forests");

        p("Random forests are an ensemble learning method for " +
                "classification and regression that operate by constructing " +
                "a multitude of decision trees at training time and outputting " +
                "the class that is the mode of the classes output by individual " +
                "trees.");

        p("The algorithm for building a random forest was developed " +
                "by Leo Breiman and Adele Cutler, and \"Random Forests\" is their trademark. " +
                "More about this topic can be found at " +
                "<a href=\"http://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm\">" +
                "http://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm</a>");

        heading(3, "Implementation");

        p("Rapaio toolbox is and will continue to be under construction for a long period. " +
                "However, there is a Random Forest implementation available and ready to be used. " +
                "Implemented features are:");

        p("1. Classification only (regression will follow soon).");
        p("2. It uses Entropy, Gini index, InfoGain  or GainRatio as impurity functions.");
        p("3. It computes OOB (Out of bag) error, which is an estimation constructed "
                + "in the same way as a cross validation. For faster "
                + "execution one can disable oob computation.");
        p("4. Does not perform yet any of the two ways of feature importance.");
        p("5. There is no computation of proximity.");

        heading(3, "Why Random Forests?");

        p("Random Forests are very popular due to the fact that the intuition behind " +
                "the algorithm is easy to understand, and, to some extent, easy to implement. " +
                "However, I found a very popular opinion that Random Forests is something " +
                "like a panacea for learning. In my humble opinion the reality does not " +
                "support that, if not only because there is no such thing in machine learning. " +
                "However, if one is agnostic about the characteristics of the features or " +
                "has no clue about the real model, random forests looks like a good " +
                "shot.");

        p("Random Forests learns well in a variety of situations and is usually " +
                "useful when it is very hard or complex to understand the mechanics " +
                "of your data. However finding and exploiting valuable knowledge " +
                "from data is often more successful than random forests.");

        p("I like random forests for some other qualities which I found more " +
                "valuable, but not so popular:");
        p("- ability to capture knowledge about importance of the features");
        p("- possibility to be used as an exploratory tool or for unsupervised learning");
        p("- the theory behind the algorithm which explains how some variance " +
                "vanishes, how some \"noisy random salt\" produces stability; " +
                "all the inspiring simple or subtle things from the theory behind it");

        heading(3, "Data setup");

        p("I will use a classical data set called spam-base, data set which was imported " +
                "into Rapaio toolbox from the well-known UCI repository: " +
                "<a href=\"http://archive.ics.uci.edu/ml/datasets/Spambase\">" +
                "http://archive.ics.uci.edu/ml/datasets/Spambase</a> ");

        p("In order to compute faster I will use only some dimensions of this dataset. " +
                "I will use for prediction only the first 20 features and the class called \"spam\"");

        code("        Frame all = Datasets.loadSpamBase();\n" +
                "        all = all.mapVars(\"0~19,spam\"); // keep only some columns\n" +
                "\n" +
                "        Summary.summary(all.mapVars(\"0~4\")); // summary of first 5 columns\n" +
                "        Summary.summary(all.mapVars(\"spam\"));\n" +
                "\n" +
                "        Summary.summary(all);\n");

        Frame all = Datasets.loadSpamBase();
        all = all.mapVars("0~19,spam"); // keep only some columns

        Summary.summary(all.mapVars("0~4")); // summary of first 5 columns
        Summary.summary(all.mapVars("spam"));

        p("Above you see some 5-number summaries of the data. ");

        p("We will split the data set in two parts, one will be used for " +
                "training the random forest and another one will be used " +
                "for testing its prediction accuracy. ");

        code("        List<Frame> frames = Sampling.randomSampleSlices(all, 0.15);\n" +
                "        Frame train = frames.get(0);\n" +
                "        Frame test = frames.get(1);\n");


        List<Frame> frames = Sampling.randomSampleSlices(all, 0.15);
        Frame train = frames.get(0);
        Frame test = frames.get(1);

        heading(3, "Playing with number of trees grown");

        p("Now that we have a train and a test data set we can learn and predict. "
                + "RF grows a number of trees over bootstrap samples and use "
                + "voting for classification. How large this number of trees must be? "
                + "You can check how well you predict as the number of trees grows. ");

        final Var index = Index.newEmpty();
        final Var errors = Numeric.newEmpty();
        final Var oob = Numeric.newEmpty();

        CForest rf = new CForest()
                .withVarSelector(new VarSelector.Random(2))
                .withOobError(true);

        for (int mTrees = 1; mTrees < 20; mTrees += 1) {
            rf.withRuns(mTrees);
            rf.learn(train, "spam");

            CResult cr = rf.predict(test);

            index.addIndex(mTrees);
            errors.addValue(new ConfusionMatrix(
                    test.var("spam"),
                    cr.firstClasses()
            ).error());
            oob.addValue(rf.getOobError());
        }

        draw(new Plot()
                .add(new Lines(index, errors).color(2))
                .add(new Points(index, errors).color(2))
                .add(new Lines(index, oob))
                .add(new Points(index, oob))
                .yLim(0, 0.24)
                .yLab("test (blue), oob (black)")
                .xLab("number of trees")
                .title("Accuracy errors (% misclassified)")
                , 600, 400);

        p("Note from the previous plot how both test and oob errors " +
                "goes down as the number of trained trees grown. " +
                "However, the improvement stops at some point and " +
                "become useless to add new trees.");

        code("        final Var index = Index.newEmpty();\n" +
                "        final Var errors = Numeric.newEmpty();\n" +
                "        final Var oob = Numeric.newEmpty();\n" +
                "\n" +
                "        CForest rf = new CForest()\n" +
                "                .withVarSelector(new VarSelector.Random(2))\n" +
                "                .withOobError(true);\n" +
                "\n" +
                "        for (int mTrees = 1; mTrees < 20; mTrees += 1) {\n" +
                "            rf.withRuns(mTrees);\n" +
                "            rf.learn(train, \"spam\");\n" +
                "\n" +
                "            CResult cr = rf.predict(test);\n" +
                "\n" +
                "            index.addIndex(mTrees);\n" +
                "            errors.addValue(new ConfusionMatrix(\n" +
                "                    test.var(\"spam\"),\n" +
                "                    cr.firstClasses()\n" +
                "            ).error());\n" +
                "            oob.addValue(rf.getOobError());\n" +
                "        }\n" +
                "\n" +
                "        draw(new Plot()\n" +
                "                .add(new Lines(index, errors).color(2))\n" +
                "                .add(new Points(index, errors).color(2))\n" +
                "                .add(new Lines(index, oob))\n" +
                "                .add(new Points(index, oob))\n" +
                "                .yLim(0, 0.24)\n" +
                "                .yLab(\"test (blue), oob (black)\")\n" +
                "                .xLab(\"number of trees\")\n" +
                "                .title(\"Accuracy errors (% misclassified)\")\n" +
                "                , 600, 400);\n");

        heading(3, "Playing with number of random features");

        p("The main difference between bagging and random forests is " +
                "that while bagging relies only on growing trees on bootstraps, " +
                "the random forests introduces randomized selection of the features in order " +
                "to de-correlate the tree models. " +
                "The main effect of this is that it will further reduce the variance of the " +
                "prediction and the compensation is better accuracy.");

        final Var index1 = Index.newEmpty();
        final Var errors1 = Numeric.newEmpty();
        final Var oob1 = Numeric.newEmpty();

        for (int mCol = 1; mCol <= 10; mCol++) {

            rf = new CForest()
                    .withVarSelector(new VarSelector.Random(mCol))
                    .withRuns(30)
                    .withOobError(true);

            rf.learn(train, "spam");
            CResult cr = rf.predict(test);

            index1.addIndex(mCol);
            errors1.addValue(new ConfusionMatrix(test.var("spam"), cr.firstClasses()).error());
            oob1.addValue(rf.getOobError());
        }
        draw(new Plot()
                .add(new Lines(index1, errors1).color(2))
                .add(new Points(index1, errors1).color(2))
                .add(new Lines(index1, oob1))
                .add(new Points(index1, oob1))
                .yLab("test (blue), oob (black")
                .xLab("mcols - number of features considered")
                .title("Accuracy errors (% misclassified)")
                .yLim(0, 0.2), 600, 400);

        p("The code which produced the last plot is listed below.");

        code("        final Var index1 = Index.newEmpty();\n" +
                "        final Var errors1 = Numeric.newEmpty();\n" +
                "        final Var oob1 = Numeric.newEmpty();\n" +
                "        \n" +
                "        for (int mCol = 1; mCol <= 10; mCol++) {\n" +
                "\n" +
                "            rf = new CForest()\n" +
                "                    .withVarSelector(new VarSelector.Random(mCol))\n" +
                "                    .withRuns(30)\n" +
                "                    .withOobError(true);\n" +
                "\n" +
                "            rf.learn(train, \"spam\");\n" +
                "            CResult cr = rf.predict(test);\n" +
                "            \n" +
                "            index1.addIndex(mCol);\n" +
                "            errors1.addValue(new ConfusionMatrix(test.var(\"spam\"), cr.firstClasses()).error());\n" +
                "            oob1.addValue(rf.getOobError());\n" +
                "        }\n" +
                "        draw(new Plot()\n" +
                "                .add(new Lines(index1, errors1).color(2))\n" +
                "                .add(new Points(index1, errors1).color(2))\n" +
                "                .add(new Lines(index1, oob1))\n" +
                "                .add(new Points(index1, oob1))\n" +
                "                .yLab(\"test (blue), oob (black\")\n" +
                "                .xLab(\"mcols - number of features considered\")\n" +
                "                .title(\"Accuracy errors (% misclassified)\")\n" +
                "                .yLim(0, 0.2), 600, 400);\n");

        p("Note: the sole purpose of this tutorial is to show what and how it can "
                + "be done with Rapaio toolbox library. ");

        p(">>>This tutorial is generated with Rapaio document printer facilities.<<<");
    }
}
