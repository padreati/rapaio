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
package rapaio.ml.classification.boost;

import static rapaio.core.BaseMath.*;

import rapaio.data.*;
import rapaio.ml.classification.AbstractClassifier;
import rapaio.ml.classification.Classifier;

import java.util.ArrayList;
import java.util.List;


import java.util.logging.Logger;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class AdaBoostM1 extends AbstractClassifier {

    private static final Logger logger = Logger.getLogger(AdaBoostM1.class.getName());

    private final Classifier weak;
    private final int t;

    private final List<Double> a;
    private final List<Classifier> h;

    private String[] dict;
    private NominalVector pred;
    private Frame dist;

    public AdaBoostM1(Classifier weak, int t) {
        this.weak = weak;
        this.t = t;
        this.a = new ArrayList<>();
        this.h = new ArrayList<>();
    }

    @Override
    public Classifier newInstance() {
        return new AdaBoostM1(weak.newInstance(), t);
    }

    @Override
    public void learn(Frame df, List<Double> weights, String classColName) {
        logger.fine(String.format("Start learn on AdaBoostM1 (weak=%s, t=%d)", weak.getClass().getName(), t));
        dict = df.getCol(classColName).getDictionary();

        List<Double> w = new ArrayList<>(weights);

        double total = 0;
        for (double ww : w) {
            total += ww;
        }
        for (int j = 0; j < w.size(); j++) {
            w.set(j, w.get(j) / total);
        }

        for (int i = 0; i < t; i++) {
            Classifier hh = weak.newInstance();
            hh.learn(df, new ArrayList<>(w), classColName);
            hh.predict(df);
            NominalVector hpred = hh.getPrediction();
            double err = 0;
            for (int j = 0; j < df.getRowCount(); j++) {
                if (hpred.getIndex(j) != df.getCol(classColName).getIndex(j)) {
                    err += w.get(j);
                }
            }
            double alpha = (1. - err) / err;

            if (err == 0 || err > 0.5) {
                if (h.isEmpty()) {
                    h.add(hh);
                    a.add(log(alpha));
                }
                break;
            }
            h.add(hh);
            a.add(log(alpha));

            // update
            total = 0;
            for (double ww : w) {
                total += ww;
            }
            for (int j = 0; j < w.size(); j++) {
                if (hpred.getIndex(j) != df.getCol(classColName).getIndex(j)) {
                    w.set(j, w.get(j) / (2 * err));
                } else {
                    w.set(j, w.get(j) / (2 * (1. - err)));
                }
            }
        }
    }

    @Override
    public void predict(Frame df) {
        predict(df, h.size());
    }

    public void predict(Frame df, int t) {

        pred = new NominalVector(df.getRowCount(), dict);
        dist = Frames.newMatrixFrame(df.getRowCount(), dict);

        for (int i = 0; i < min(t, h.size()); i++) {
            h.get(i).predict(df);
            for (int j = 0; j < df.getRowCount(); j++) {
                int index = h.get(i).getPrediction().getIndex(j);
                dist.setValue(j, index, dist.getValue(j, index) + a.get(i));
            }
        }

        // simply predict
        for (int i = 0; i < dist.getRowCount(); i++) {
            double max = 0;
            int prediction = 0;
            for (int j = 1; j < dist.getColCount(); j++) {
                if (dist.getValue(i, j) > max) {
                    prediction = j;
                    max = dist.getValue(i, j);
                }
            }
            pred.setIndex(i, prediction);
        }
    }

    @Override
    public NominalVector getPrediction() {
        return pred;
    }

    @Override
    public Frame getDistribution() {
        return dist;
    }

    @Override
    public void summary() {
    }
}
