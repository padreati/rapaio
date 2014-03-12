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

package rapaio.ml.boost;

import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Nominal;
import rapaio.ml.AbstractClassifier;
import rapaio.ml.Classifier;

import java.util.ArrayList;
import java.util.List;

import static rapaio.core.MathBase.log;
import static rapaio.core.MathBase.min;
import static rapaio.workspace.Workspace.code;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class AdaBoostSAMMEClassifier extends AbstractClassifier<AdaBoostSAMMEClassifier> {

	Classifier weak;
	int t;

	List<Double> a;
	List<Classifier> h;
	List<Double> w;
	double k;

	String[] dict;
	Nominal pred;
	Frame dist;

	public AdaBoostSAMMEClassifier(Classifier weak, int t) {
		this.weak = weak;
		this.t = t;
		this.a = new ArrayList<>();
		this.h = new ArrayList<>();
	}

	@Override
	public Classifier newInstance() {
		return new AdaBoostSAMMEClassifier(weak.newInstance(), t);
	}

    public AdaBoostSAMMEClassifier setT(int t) {
        this.t = t;
        return this;
    }

	@Override
	public void learn(Frame df, List<Double> weights, String classColName) {
		dict = df.getCol(classColName).getDictionary();
		k = dict.length - 1;

		w = new ArrayList<>(weights);

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
			Nominal hpred = hh.getPrediction();

			double err = 0;
			for (int j = 0; j < df.getRowCount(); j++) {
				if (hpred.getIndex(j) != df.getCol(classColName).getIndex(j)) {
					err += w.get(j);
				}
			}
			double alpha = log((1. - err) / err) + log(k - 1);
			if (err == 0) {
				if (h.isEmpty()) {
					h.add(hh);
					a.add(alpha);
				}
				break;
			}
			if (err > (1 - 1 / k)) {
				i--;
				continue;
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
	public void learnFurther(Frame df, List<Double> weights, String classColName, AdaBoostSAMMEClassifier classifier) {
		// TODO validation for further learning

		if (classifier == null) {
			learn(df, weights, classColName);
			return;
		}

		dict = df.getCol(classColName).getDictionary();
		k = dict.length - 1;
		h = new ArrayList<>(classifier.h);
		a = new ArrayList<>(classifier.a);
		if (t == classifier.t) {
			return;
		}
//        w = (weights == null) ? classifier.w : weights;
		w = classifier.w;
		for (int i = h.size(); i < t; i++) {
			Classifier hh = weak.newInstance();
			hh.learn(df, new ArrayList<>(w), classColName);
			hh.predict(df);
			Nominal hpred = hh.getPrediction();

			double err = 0;
			for (int j = 0; j < df.getRowCount(); j++) {
				if (hpred.getIndex(j) != df.getCol(classColName).getIndex(j)) {
					err += w.get(j);
				}
			}
			double alpha = log((1. - err) / err) + log(k - 1);
			if (err == 0) {
				if (h.isEmpty()) {
					h.add(hh);
					a.add(alpha);
				}
				break;
			}
			if (err > (1 - 1 / k)) {
				i--;
				continue;
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
	public void predictFurther(Frame df, AdaBoostSAMMEClassifier classifier) {
		if (classifier == null) {
			predict(df);
			return;
		}

		pred = classifier.pred;
		dist = classifier.dist;

		for (int i = classifier.h.size(); i < min(t, h.size()); i++) {
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
	public void predict(Frame df) {
		pred = new Nominal(df.getRowCount(), dict);
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
	public Nominal getPrediction() {
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
