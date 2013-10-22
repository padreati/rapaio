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

import rapaio.data.*;
import rapaio.datasets.Datasets;
import rapaio.explore.Summary;
import static rapaio.explore.Workspace.*;
import rapaio.filters.ColFilters;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Lines;
import rapaio.graphics.plot.Points;
import rapaio.sample.StatSampling;
import rapaio.supervised.Classifier;
import rapaio.supervised.tree.RandomForest;

import java.io.IOException;
import java.util.List;

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
    public void render() throws IOException {
        heading(1, "Classification with Random Forests");

        p("Random forests are an ensemble learning method for " +
                "classification (and regression) that operate by constructing " +
                "a multitude of decision trees at training time and outputting " +
                "the class that is the mode of the classes output by individual " +
                "trees.");
        p("The algorithm for inducing a random forest was developed " +
                "by Leo Breiman and Adele Cutler, and \"Random Forests\" is their trademark. " +
                "More about this topic can be found at " +
                "<a href=\"http://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm\">" +
                "http://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm</a>");

        heading(3, "Implementation");

        p("Rapaio toolbox is and will continue to be under construction for a long " +
                "period. However, there is a Random Forest implementation available and " +
                "ready to be used. Implemented features:");
        p("1. Classification only (regression will follow soon).");
        p("2. It uses Gini impurity function for finding the best split attributes/values, " +
                "according with the original specifications. " +
                "It can be implemented also with InfoGain (as it is implemented in Weka), or" +
                "something else. However there is not a huge difference in results, thus " +
                "for now remains according with the original specifications.");
        p("3. It computes OOB (Out of bag) error, which is an estimation constructed " +
                "in the same way as a cross validation. For faster " +
                "execution one can disable oob computation.");
        p("4. Does not perform yet any of the two ways of feature importance, " +
                "it will be implemented soon.");
        p("5. There is no computation of proximity and I do not know for sure if I want " +
                "that in the immediate future.");

        heading(3, "Why Random Forests?");

        p("Random Forests are very popular due to the fact that the intuition behind " +
                "the algorithm is easy to understand, and, to some extent, " +
                "easy to implement. However, I found a very popular opinion that " +
                "Random Forests is something like a panacea for learning. In my humble " +
                "opinion it is far from that, simply because there is no such " +
                "thing in machine learning. ");

        p("Random Forests learns well in a variety of situations and is usually " +
                "useful when it is very hard or complex to understand the mechanics " +
                "of your data. However finding and exploiting valuable knowledge " +
                "from data is often more successful than random forests.");

        p("I like random forests for some other qualities which I found more " +
                "valuable, but not so popular:");
        p("- ability to capture knowledge about importance of the features");
        p("- possibility to be used as an exploratory tool or for " +
                "unsupervised learning");
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
                "        all = ColFilters.retainCols(all, \"1-20,spam\");\n" +
                "\n" +
                "        Summary.summary(ColFilters.retainCols(all, \"1-5\"));\n" +
                "        Summary.summary(ColFilters.retainCols(all, \"spam\"));\n");

        Frame all = Datasets.loadSpamBase();
        all = ColFilters.retainCols(all, "1-20,spam"); // keep only some columns

        Summary.summary(ColFilters.retainCols(all, "1-5")); // summary of first 5 columsn
        Summary.summary(ColFilters.retainCols(all, "spam"));

        p("Above you see some 5-number information on the data. It is not exhaustive " +
                "since it is not the purpose of this tutorial.");

        p("We will split the data set in two parts, one will be used for " +
                "training the random forest and another one will be used " +
                "for testing its prediction accuracy. ");

        code("        List<Frame> frames = StatSampling.randomSample(all, new int[]{all.getRowCount() * 15 / 100});\n" +
                "        Frame train = frames.get(0);\n" +
                "        Frame test = frames.get(1);\n");
        List<Frame> frames = StatSampling.randomSample(all, new int[]{all.getRowCount() * 15 / 100});
        Frame train = frames.get(0);
        Frame test = frames.get(1);

        heading(3, "Playing with number of trees grown");

        p("Now that we have a train and a test data set we can learn and predict. " +
                "RF grows a number of trees over bootstrap samples and use " +
                "voting for classification. How large this number of trees must be? " +
                "You can check how well you predict as the number of trees grows. ");


        int pos = 0;
        Vector index = new IndexVector("number of trees", 400);
        Vector accuracy = new NumericVector("test error", 400);
        Vector oob = new NumericVector("oob error", 400);
        for (int mtree = 1; mtree < 200; mtree += 10) {
            RandomForest rf = new RandomForest(mtree, 2, true);
            rf.learn(train, "spam");
            rf.predict(test);
            index.setIndex(pos, mtree);
            accuracy.setValue(pos, 1 - computeAccuracy(rf, test));
            oob.setValue(pos, rf.getOobError());
            pos++;
        }
        Plot p = new Plot();
        Lines lines = new Lines(p, index, accuracy);
        lines.opt().setColorIndex(new OneIndexVector(2));
        p.add(lines);
        Points pts = new Points(p, index, accuracy);
        pts.opt().setColorIndex(new OneIndexVector(2));
        p.add(pts);
        p.add(new Lines(p, index, oob));
        p.add(new Points(p, index, oob));

        p.setLeftLabel("test (blue), oob (black)");
        p.setTitle("Accuracy errors (% misclassified)");
        p.getOp().setYRange(0, 0.4);
        draw(p, 600, 400);

        p("Note from the previous plot how both test and oob errors " +
                "goes down as the number of trained trees grown. " +
                "However, the improvement stops at some point and " +
                "become useless to add new trees.");

        code("        int pos = 0;\n" +
                "        Vector index = new IndexVector(\"number of trees\", 1000);\n" +
                "        Vector accuracy = new NumericVector(\"test error\", 1000);\n" +
                "        Vector oob = new NumericVector(\"oob error\", 1000);\n" +
                "        for (int mtree = 1; mtree < 100; mtree += 5) {\n" +
                "            RandomForest rf = new RandomForest(mtree, 3, true);\n" +
                "            rf.learn(train, \"spam\");\n" +
                "            ClassifierModel model = rf.predict(test);\n" +
                "            index.setIndex(pos, mtree);\n" +
                "            accuracy.setValue(pos, 1 - computeAccuracy(model, test));\n" +
                "            oob.setValue(pos, rf.getOobError());\n" +
                "            pos++;\n" +
                "        }\n" +
                "        Plot p = new Plot();\n" +
                "        Lines lines = new Lines(p, index, accuracy);\n" +
                "        lines.opt().setColorIndex(new OneIndexVector(2));\n" +
                "        p.add(lines);\n" +
                "        Points pts = new Points(p, index, accuracy);\n" +
                "        pts.opt().setColorIndex(new OneIndexVector(2));\n" +
                "        p.add(pts);\n" +
                "        p.add(new Lines(p, index, oob));\n" +
                "        p.add(new Points(p, index, oob));\n" +
                "\n" +
                "        p.setLeftLabel(\"test (blue), oob (black)\");\n" +
                "        p.setTitle(\"Accuracy errors (% misclassified)\");\n" +
                "        p.getOp().setYRange(0, 0.4);\n" +
                "        draw(p, 600, 400);\n");

        heading(3, "Playing with number of random features");

        p("The main difference between bagging and random forests is " +
                "that while bagging relies only grows trees on bootstraps, " +
                "the random forests introduces randomization in order " +
                "to uncorrelate those trees. The main effect of this " +
                "is that it will further reduce the variance of the " +
                "prediction and the compensation is better accuracy.");

        pos = 0;
        index = new IndexVector("mtree", 1000);
        accuracy = new NumericVector("test error", 1000);
        oob = new NumericVector("oob error", 1000);
        for (int mcol = 1; mcol < 20; mcol += 1) {

            RandomForest rf = new RandomForest(30, mcol, true);
            rf.learn(train, "spam");
            rf.predict(test);
            index.setIndex(pos, mcol);
            accuracy.setValue(pos, 1 - computeAccuracy(rf, test));
            oob.setValue(pos, rf.getOobError());

            pos++;
        }
        p = new Plot();
        lines = new Lines(p, index, accuracy);
        lines.opt().setColorIndex(new OneIndexVector(2));
        p.add(lines);
        pts = new Points(p, index, accuracy);
        pts.opt().setColorIndex(new OneIndexVector(2));
        p.add(pts);
        p.add(new Lines(p, index, oob));
        p.add(new Points(p, index, oob));
        p.setLeftLabel("test (blue), oob (black");
        p.setBottomLabel("mcols - number of features considered");
        p.setTitle("Accuracy errors (% misclassified)");
        p.getOp().setYRange(0, 0.4);
        draw(p, 600, 400);

        p("It can be seen here that the best prediction according " +
                "with oob and the test used is when the number of " +
                "random features lies in 3 to 6 interval.");

        p("And the code which produced the last plot is listed below.");
        code("        pos = 0;\n" +
                "        index = new IndexVector(\"mtree\", 1000);\n" +
                "        accuracy = new NumericVector(\"test error\", 1000);\n" +
                "        oob = new NumericVector(\"oob error\", 1000);\n" +
                "        for (int mtree = 1; mtree < 20; mtree += 1) {\n" +
                "\n" +
                "            RandomForest rf = new RandomForest(10, mtree, true);\n" +
                "            rf.learn(train, \"spam\");\n" +
                "            ClassifierModel model = rf.predict(test);\n" +
                "\n" +
                "            index.setIndex(pos, mtree);\n" +
                "            accuracy.setValue(pos, 1 - computeAccuracy(model, test));\n" +
                "            oob.setValue(pos, rf.getOobError());\n" +
                "\n" +
                "            pos++;\n" +
                "        }\n" +
                "        p = new Plot();\n" +
                "        lines = new Lines(p, index, accuracy);\n" +
                "        lines.opt().setColorIndex(new OneIndexVector(2));\n" +
                "        p.add(lines);\n" +
                "        pts = new Points(p, index, accuracy);\n" +
                "        pts.opt().setColorIndex(new OneIndexVector(2));\n" +
                "        p.add(pts);\n" +
                "        p.add(new Lines(p, index, oob));\n" +
                "        p.add(new Points(p, index, oob));\n" +
                "        p.setLeftLabel(\"test (blue), oob (black\");\n" +
                "        p.setBottomLabel(\"mcols - number of features considered\");\n" +
                "        p.setTitle(\"Accuracy errors (% misclassified)\");\n" +
                "        p.getOp().setYRange(0, 0.4);\n" +
                "        draw(p, 600, 400);\n");

        p("Note: the sole purpose of this tutorial is to show what and how it can " +
                "be done with Rapaio toolbox library. ");

        p(">>>This tutorial is generated with Rapaio document printer facilities.<<<");
    }

    private double computeAccuracy(Classifier model, Frame test) {
        Vector predict = model.getPrediction();
        double accuracy = 0;
        double total = predict.getRowCount();
        for (int i = 0; i < predict.getRowCount(); i++) {
            if (test.getCol("spam").getIndex(i) == predict.getIndex(i)) {
                accuracy += 1.;
            }
        }
        return accuracy / total;
    }
}
