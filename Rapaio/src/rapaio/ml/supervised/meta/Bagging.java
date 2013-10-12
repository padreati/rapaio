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

package rapaio.ml.supervised.meta;

import rapaio.data.*;
import rapaio.ml.supervised.Classifier;
import rapaio.ml.supervised.ClassifierModel;
import rapaio.ml.supervised.ClassifierProvider;
import rapaio.sample.Sample;

import java.util.ArrayList;
import java.util.List;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Bagging implements Classifier {

    private final double p;
    private final int bags;
    private final ClassifierProvider provider;
    private List<Classifier> classifiers = new ArrayList<>();
    private String[] dict;
    private String classColName;

    public Bagging(double p, int bags, ClassifierProvider provider) {
        this.p = p;
        this.bags = bags;
        this.provider = provider;
    }

    @Override
    public void learn(Frame df, String classColName) {
        this.classColName = classColName;
        this.dict = df.getCol(classColName).getDictionary();
        classifiers.clear();
        for (int i = 0; i < bags; i++) {
            Frame bootstrap = Sample.randomBootstrap(df, (int) Math.rint(df.getRowCount() * p));
            Classifier c = provider.newInstance();
            c.learn(bootstrap, classColName);
            classifiers.add(c);
        }
    }

    @Override
    public ClassifierModel predict(final Frame df) {
        // voting

        final Vector predict = new NominalVector(classColName, df.getRowCount(), dict);
        final Vector[] probs = new Vector[dict.length - 1];
        for (int i = 0; i < dict.length - 1; i++) {
            probs[i] = new NumericVector(dict[i + 1], new double[df.getRowCount()]);
        }
        final Frame prob = new SolidFrame("probs", df.getRowCount(), probs);

        // collect results from each classifier
        for (int i = 0; i < classifiers.size(); i++) {
            ClassifierModel model = classifiers.get(i).predict(df);
            for (int j = 0; j < df.getRowCount(); j++) {
                String prediction = model.getClassification().getLabel(j);
                double prev = prob.getValue(j, prob.getColIndex(prediction));
                if (prev != prev) {
                    prev = 0;
                }
                prob.setValue(j, prob.getColIndex(prediction), prev + 1);
            }
        }
        for (int i = 0; i < prob.getRowCount(); i++) {
            int index = -1;
            double max = -1;
            for (int j = 0; j < prob.getColCount(); j++) {
                double freq = prob.getValue(i, j);
                prob.setValue(i, j, freq / (1. * classifiers.size()));
                if (max < freq) {
                    max = freq;
                    index = j;
                }
            }
            predict.setLabel(i, predict.getDictionary()[index + 1]);
        }

        return new ClassifierModel() {
            @Override
            public Frame getTestFrame() {
                return df;
            }

            @Override
            public Vector getClassification() {
                return predict;
            }

            @Override
            public Frame getProbabilities() {
                return prob;
            }
        };
    }

    @Override
    public void summary() {
    }
}
