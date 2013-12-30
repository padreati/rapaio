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
import static rapaio.workspace.Workspace.*;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class AdaBoostSAMME extends AbstractClassifier {

    private static final Logger logger = Logger.getLogger(AdaBoostSAMME.class.getName());

    private final Classifier weak;
    private final int t;

    private final List<Double> a;
    private final List<Classifier> h;

    private String[] dict;
    private NominalVector pred;
    private Frame dist;

    public AdaBoostSAMME(Classifier weak, int t) {
        this.weak = weak;
        this.t = t;
        this.a = new ArrayList<>();
        this.h = new ArrayList<>();
    }

    @Override
    public Classifier newInstance() {
        return new AdaBoostSAMME(weak.newInstance(), t);
    }

    @Override
    public void learn(Frame df, List<Double> weights, String classColName) {
        logger.fine(String.format("Start learn on AdaBoostM1 (weak=%s, t=%d)", weak.getClass().getName(), t));
        dict = df.getCol(classColName).getDictionary();
        double k = dict.length - 1;

        List<Double> w = new ArrayList<>(weights);

        double total = 0;
        for (int j = 0; j < w.size(); j++) {
            total += w.get(j);
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
            double alpha = log((1. - err) / err) + log(k - 1);
            if (err == 0 || err > (1 - 1 / k)) {
                if (h.isEmpty()) {
                    h.add(hh);
                    a.add(alpha);
                }
                break;
            }
            h.add(hh);
            a.add(alpha);

            // update
            for (int j = 0; j < w.size(); j++) {
                if (hpred.getIndex(j) != df.getCol(classColName).getIndex(j)) {
                    w.set(j, w.get(j) * (k - 1) / (k * err));
                } else {
                    w.set(j, w.get(j) / (k * (1. - err)));
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
        StringBuilder sb = new StringBuilder();
        // title
        sb.append("AdaBoostSAMME [t=").append(t).append("]\n");
        sb.append("weak learners built:").append(h.size()).append("\n");
        code(sb.toString());
    }
}
