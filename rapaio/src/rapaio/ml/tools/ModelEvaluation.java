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
package rapaio.ml.tools;

import rapaio.core.sample.StatSampling;
import rapaio.core.stat.ConfusionMatrix;
import rapaio.data.Frame;
import rapaio.data.Vector;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;
import rapaio.ml.Classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static rapaio.data.filters.BaseFilters.delta;
import static rapaio.data.filters.BaseFilters.shuffle;
import static rapaio.workspace.Workspace.print;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ModelEvaluation {

	public double cv(Frame df, String classColName, Classifier c, int folds) {
		print("\n<pre><code>\n");
		print("CrossValidation with " + folds + " folds\n");

		List<Integer>[] strata = buildStrata(df, folds, classColName);

		double correct = 0;

		for (int i = 0; i < folds; i++) {
			List<Integer> trainMapping = new ArrayList<>();
			List<Integer> testMapping = new ArrayList<>();
			for (int j = 0; j < folds; j++) {
				if (j == i) {
					testMapping.addAll(strata[j]);
				} else {
					trainMapping.addAll(strata[j]);
				}
			}
			Frame train = new MappedFrame(df.getSourceFrame(), new Mapping(trainMapping));
			Frame test = new MappedFrame(df.getSourceFrame(), new Mapping(testMapping));

			c.learn(train, classColName);
			c.predict(test);
			double fcorrect = 0;
			for (int j = 0; j < test.getRowCount(); j++) {
				if (test.getCol(classColName).getIndex(j) == c.getPrediction().getIndex(j)) {
					fcorrect++;
				}
			}
			print(String.format("CV %d, accuracy:%.6f\n", i + 1, fcorrect / (1. * test.getRowCount())));
			correct += fcorrect;
		}
		correct /= (1. * df.getRowCount());
		print(String.format("Mean accuracy:%.6f\n", correct));

		print("</code></pre>\n");
		return correct;
	}

	private List<Integer>[] buildStrata(Frame df, int folds, String classColName) {
		String[] dict = df.getCol(classColName).getDictionary();
		List<Integer>[] rowIds = new List[dict.length];
		for (int i = 0; i < dict.length; i++) {
			rowIds[i] = new ArrayList<>();
		}
		for (int i = 0; i < df.getRowCount(); i++) {
			rowIds[df.getIndex(i, df.getColIndex(classColName))].add(df.getRowId(i));
		}
		List<Integer> shuffle = new ArrayList<>();
		for (int i = 0; i < dict.length; i++) {
			Collections.shuffle(rowIds[i]);
			shuffle.addAll(rowIds[i]);
		}
		List<Integer>[] strata = new List[folds];
		for (int i = 0; i < strata.length; i++) {
			strata[i] = new ArrayList<>();
		}
		int fold = 0;
		for (int next : shuffle) {
			strata[fold].add(next);
			fold++;
			if (fold == folds) {
				fold = 0;
			}
		}
		return strata;
	}

	public void multiCv(Frame df, String classColName, List<Classifier> classifiers, int folds) {
		print("\n<pre><code>\n");
		print("CrossValidation with " + folds + " folds\n");
		df = shuffle(df);
		double[] tacc = new double[classifiers.size()];

		for (int i = 0; i < folds; i++) {
			List<Integer> trainMapping = new ArrayList<>();
			List<Integer> testMapping = new ArrayList<>();
			if (folds >= df.getRowCount() - 1) {
				testMapping.add(i);
				for (int j = 0; j < df.getRowCount(); j++) {
					if (j != i) {
						trainMapping.add(df.getRowId(j));
					}
				}

			} else {
				for (int j = 0; j < df.getRowCount(); j++) {
					if (j % folds == i) {
						testMapping.add(df.getRowId(j));
					} else {
						trainMapping.add(df.getRowId(j));
					}
				}
			}
			Frame train = new MappedFrame(df.getSourceFrame(), new Mapping(trainMapping));
			Frame test = new MappedFrame(df.getSourceFrame(), new Mapping(testMapping));

			for (int k = 0; k < classifiers.size(); k++) {
				Classifier c = classifiers.get(k);
//                c = c.newInstance();
				c.learn(train, classColName);
				c.predict(test);
				double acc = 0;
				for (int j = 0; j < c.getPrediction().getRowCount(); j++) {
					if (c.getPrediction().getIndex(j) == test.getCol(classColName).getIndex(j)) {
						acc++;
					}
				}
				acc /= (1. * c.getPrediction().getRowCount());
				tacc[k] += acc;
				print(String.format("CV %d, classifier[%d] - accuracy:%.6f\n", i + 1, k + 1, acc));
			}
			print("-----------\n");

		}

		for (int k = 0; k < classifiers.size(); k++) {
			tacc[k] /= (1. * folds);
			print(String.format("Mean accuracy for classifier[%d] :%.6f\n", k + 1, tacc[k]));
		}


		print("</code></pre>\n");
	}

	public void bootstrapValidation(Frame df, String classColName, Classifier c, int bootstraps) {
		print(bootstraps + " bootstrap evaluation\n");
		double total = 0;
		double count = 0;
		for (int i = 0; i < bootstraps; i++) {
			Frame train = StatSampling.randomBootstrap(df);
			Frame test = delta(df, train);

			c.learn(train, classColName);
			c.predict(test);
			Vector pred = c.getPrediction();
			double acc = new ConfusionMatrix(test.getCol(classColName), pred).getAccuracy();
//            System.out.println(String.format("bootstrap(%d) : %.6f", i+1, acc));
			total += acc;
			count++;
		}
		System.out.println(String.format("Average accuracy: %.6f", total / count));
	}
}
