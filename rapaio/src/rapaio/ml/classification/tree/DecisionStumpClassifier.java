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
package rapaio.ml.classification.tree;

import rapaio.core.RandomSource;
import rapaio.data.*;
import rapaio.data.filters.BaseFilters;
import rapaio.ml.classification.AbstractClassifier;
import rapaio.ml.classification.Classifier;

import java.util.ArrayList;
import java.util.List;

import static rapaio.core.MathBase.log;
import static rapaio.core.MathBase.validNumber;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class DecisionStumpClassifier extends AbstractClassifier<DecisionStumpClassifier> {

	private String[] dict;
	private String splitCol;
	private String splitLabel;
	private double splitValue;
	private double splitGain;
	private String leftLabel;
	private String rightLabel;
	private String defaultLabel;

	private Nominal pred;
	private Frame distr;

	@Override
	public Classifier newInstance() {
		return new DecisionStumpClassifier();
	}

	@Override
	public void learn(Frame df, List<Double> weights, String classColName) {

		splitGain = 0;
		String[] colNames = df.getColNames();
		Vector classCol = df.getCol(classColName);
		dict = classCol.getDictionary();

		double[] total = new double[classCol.getDictionary().length];
		for (int i = 0; i < df.getRowCount(); i++) {
			total[classCol.getIndex(i)] += weights.get(i);
		}
		for (String colName : colNames) {
			if (classColName.equals(colName)) continue;
			Vector col = df.getCol(colName);
			if (col.getType().isNumeric()) {
				evaluateNumeric(df, weights, classCol, col, colName, total);
			} else {
				evaluateNominal(df, weights, classCol, col, colName, total);
			}
		}
		this.defaultLabel = buildDefaultLabel(df, weights, classCol);

	}

	private String buildDefaultLabel(Frame df, List<Double> weights, Vector classCol) {
		double[] freq = new double[classCol.getDictionary().length];
		int total = 0;
		for (int i = 0; i < df.getRowCount(); i++) {
			if (splitCol == null || df.getCol(splitCol).isMissing(i)) {
				freq[classCol.getIndex(i)] += weights.get(i);
				total++;
			}
		}
		if (total == 0) return dict[0];
		double max = 0;
		List<Integer> sel = new ArrayList<>();
		for (int i = 1; i < dict.length; i++) {
			if (freq[i] > max) {
				max = freq[i];
				sel.clear();
				sel.add(i);
				continue;
			}
			if (freq[i] == max) {
				sel.add(i);
			}
		}
		return dict[sel.get(RandomSource.nextInt(sel.size()))];
	}

	private void evaluateNominal(Frame df, List<Double> weights, Vector classCol, Vector col, String colName, double[] total) {
		double[][] dist = new double[col.getDictionary().length][classCol.getDictionary().length];
		for (int i = 0; i < df.getRowCount(); i++) {
			dist[col.getIndex(i)][classCol.getIndex(i)] += weights.get(i);
		}
		for (int i = 1; i < col.getDictionary().length; i++) {

			double metric = computeGini(dist[0], dist[i], total);
			if (validNumber(metric) && metric > splitGain) {
				splitGain = metric;
				splitCol = colName;
				splitLabel = col.getDictionary()[i];
				splitValue = Double.NaN;

				List<Integer> left = new ArrayList<>();
				List<Integer> right = new ArrayList<>();
				double leftMax = 0;
				double rightMax = 0;
				for (int j = 0; j < dict.length; j++) {
					if (dist[i][j] > leftMax) {
						leftMax = dist[i][j];
						left.clear();
						left.add(j);
						continue;
					}
					if (dist[i][j] == leftMax) {
						left.add(j);
					}
				}
				for (int j = 0; j < dict.length; j++) {
					if (total[j] - dist[0][j] - dist[i][j] > rightMax) {
						rightMax = total[j] - dist[0][j] - dist[i][j];
						right.clear();
						right.add(j);
						continue;
					}
					if (total[j] - dist[0][j] - dist[i][j] == rightMax) {
						right.add(j);
					}
				}
				leftLabel = dict[left.get(RandomSource.nextInt(left.size()))];
				rightLabel = dict[right.get(RandomSource.nextInt(right.size()))];
			}
		}
	}

	private void evaluateNumeric(Frame df, List<Double> weights, Vector classCol, Vector col, String colName, double[] total) {
		double[][] p = new double[2][classCol.getDictionary().length];
		int[] rowCounts = new int[2];

		Vector sort = BaseFilters.sort(Vectors.newSeq(0, df.getRowCount() - 1, 1), RowComparators.numericComparator(col, true));
		for (int i = 0; i < df.getRowCount() - 1; i++) {
			int row = col.isMissing(sort.getIndex(i)) ? 0 : 1;
			int index = classCol.getIndex(sort.getIndex(i));
			p[row][index] += weights.get(sort.getIndex(i));
			rowCounts[row]++;
			if (row == 0) {
				continue;
			}
			if (rowCounts[1] == 0) continue;
			if (df.getRowCount() - rowCounts[1] - rowCounts[0] == 0) continue;
			if (col.getValue(sort.getIndex(i)) < col.getValue(sort.getIndex(i + 1))) {
				double metric = compute(p[0], p[1], total);
				if (!validNumber(metric)) continue;

				if (validNumber(metric) && metric > splitGain) {
					splitGain = metric;
					splitCol = colName;
					splitLabel = null;
					splitValue = col.getValue(sort.getIndex(i));

					List<Integer> left = new ArrayList<>();
					List<Integer> right = new ArrayList<>();
					double leftMax = 0;
					double rightMax = 0;
					for (int j = 0; j < dict.length; j++) {
						if (p[1][j] > leftMax) {
							leftMax = p[1][j];
							left.clear();
							left.add(j);
							continue;
						}
						if (p[1][j] == leftMax) {
							left.add(j);
						}
					}
					for (int j = 0; j < dict.length; j++) {
						if (total[j] - p[0][j] - p[1][j] > rightMax) {
							rightMax = total[j] - p[0][j] - p[1][j];
							right.clear();
							right.add(j);
							continue;
						}
						if (total[j] - p[0][j] - p[1][j] == rightMax) {
							right.add(j);
						}
					}
					leftLabel = dict[left.get(RandomSource.nextInt(left.size()))];
					rightLabel = dict[right.get(RandomSource.nextInt(right.size()))];
				}
			}
		}
	}

	@Override
	public void predict(Frame df) {
		pred = new Nominal(df.getRowCount(), dict);
		for (int i = 0; i < df.getRowCount(); i++) {
			if (splitCol == null || splitCol.isEmpty() || df.getCol(splitCol).isMissing(i)) {
				pred.setLabel(i, defaultLabel);
				continue;
			}
			Vector col = df.getCol(splitCol);
			if (col.getType().isNumeric()) {
				if (col.getValue(i) <= splitValue) {
					pred.setLabel(i, leftLabel);
				} else {
					pred.setLabel(i, rightLabel);
				}
			} else {
				if (splitLabel.equals(col.getLabel(i))) {
					pred.setLabel(i, leftLabel);
				} else {
					pred.setLabel(i, rightLabel);
				}
			}
		}
	}

	@Override
	public Nominal getPrediction() {
		return pred;
	}

	@Override
	public Frame getDistribution() {
		return distr;
	}

	@Override
	public void summary() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	private double compute(double[] missing, double[] pa, double[] total) {
		return computeInfoGain(missing, pa, total);
	}

	private double computeInfoGain(double[] missing, double[] pa, double[] total) {
		double to = 0;
		double tl = 0;
		double tr = 0;
		for (int i = 1; i < total.length; i++) {
			double left = pa[i];
			double right = total[i] - pa[i] - missing[i];
			double orig = total[i] - missing[i];
			tl += left;
			tr += right;
			to += orig;
		}
		if (tl == 0 || tr == 0) return Double.NaN;
		if (!validNumber(tl) || !validNumber(tr)) return Double.NaN;
		double po = 0;
		double pl = 0;
		double pr = 0;
		for (int i = 1; i < total.length; i++) {
			double pleft = pa[i] / tl;
			double pright = (total[i] - pa[i] - missing[i]) / tr;
			double porig = (total[i] - missing[i]) / to;
			po -= porig == 0 ? 0 : log(porig) * porig;
			pl -= pleft == 0 ? 0 : log(pleft) * pleft;
			pr -= pright == 0 ? 0 : log(pright) * pright;
		}
		return po - (tl * pl + tr * pr) / (tl + tr);
	}

	private double computeGini(double[] missing, double[] pa, double[] total) {
		double totalOrig = 0;
		double totalLeft = 0;
		double totalRight = 0;
		for (int i = 1; i < total.length; i++) {
			double left = pa[i];
			double right = total[i] - pa[i] - missing[i];
			double orig = total[i] - missing[i];
			totalLeft += left;
			totalRight += right;
			totalOrig += orig;
		}
		if (totalLeft == 0 || totalRight == 0) return Double.NaN;
		if (!validNumber(totalLeft) || !validNumber(totalRight)) return Double.NaN;
		double giniOrig = 1;
		double giniLeft = 1;
		double giniRight = 1;
		for (int i = 1; i < total.length; i++) {
			double pleft = pa[i] / totalLeft;
			double pright = (total[i] - pa[i] - missing[i]) / totalRight;
			double porig = (total[i] - missing[i]) / totalOrig;
			giniOrig -= porig * porig;
			giniLeft -= pleft * pleft;
			giniRight -= pright * pright;
		}
		return giniOrig - (totalLeft * giniLeft + totalRight * giniRight) / (totalLeft + totalRight);
	}
}
