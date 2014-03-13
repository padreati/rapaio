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

package rapaio.ml.experimental;

import rapaio.core.sample.StatSampling;
import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Nominal;
import rapaio.ml.AbstractClassifier;
import rapaio.ml.Classifier;

import java.util.ArrayList;
import java.util.List;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class Bagging extends AbstractClassifier<Bagging> {

    private final double p;
    private final int bags;
    private final Classifier provider;
    private List<Classifier> classifiers = new ArrayList<>();
    private String[] dict;
    private String classColName;
    private Nominal pred;
    private Frame dist;

    public Bagging(double p, int bags, Classifier provider) {
        this.p = p;
        this.bags = bags;
        this.provider = provider;
    }

    public Bagging newInstance() {
        Bagging bag = new Bagging(p, bags, provider);
        return bag;
    }

    @Override
    public void learn(Frame df, List<Double> weights, String targetColName) {
        this.classColName = targetColName;
        this.dict = df.col(targetColName).dictionary();
        classifiers.clear();
        for (int i = 0; i < bags; i++) {
            Frame bootstrap = StatSampling.randomBootstrap(df, (int) Math.rint(df.rowCount() * p));
            Classifier c = provider.newInstance();
            c.learn(bootstrap, targetColName);
            classifiers.add(c);
        }
    }

    @Override
    public void predict(final Frame df) {
        // voting

        pred = new Nominal(df.rowCount(), dict);
        dist = Frames.newMatrix(df.rowCount(), dict);

        // collect results from each classifier
        for (Classifier c : classifiers) {
            c.predict(df);
            for (int j = 0; j < df.rowCount(); j++) {
                String prediction = c.prediction().label(j);
                double prev = dist.value(j, dist.colIndex(prediction));
                if (prev != prev) {
                    prev = 0;
                }
                dist.setValue(j, dist.colIndex(prediction), prev + 1);
            }
        }
        for (int i = 0; i < dist.rowCount(); i++) {
            int index = -1;
            double max = -1;
            for (int j = 0; j < dist.colCount(); j++) {
                double freq = dist.value(i, j);
                dist.setValue(i, j, freq / (1. * classifiers.size()));
                if (max < freq) {
                    max = freq;
                    index = j;
                }
            }
            pred.setLabel(i, pred.dictionary()[index + 1]);
        }
    }

    @Override
    public Nominal prediction() {
        return pred;
    }

    @Override
    public Frame distribution() {
        return dist;
    }

    @Override
    public void summary() {
    }
}
