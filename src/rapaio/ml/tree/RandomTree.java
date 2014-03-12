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

package rapaio.ml.tree;

import rapaio.core.RandomSource;
import rapaio.core.stat.Mode;
import rapaio.data.*;
import rapaio.data.filters.BaseFilters;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;
import rapaio.ml.AbstractClassifier;
import rapaio.ml.Classifier;
import rapaio.ml.colselect.ColSelector;

import java.util.ArrayList;
import java.util.List;

import static rapaio.core.MathBase.abs;
import static rapaio.core.MathBase.validNumber;

public class RandomTree extends AbstractClassifier<RandomTree> {

	String classColName;
	TreeNode root;
	String[] dict;
	Nominal prediction;
	Frame d;
	double[] sumVI;
	double[] cntVI;
	private int minNodeSize = 1;
	private double numericSelectionProb = 1.;
	private ColSelector colSelector;

	public RandomTree() {
	}

	@Override
	public Classifier newInstance() {
		RandomTree randomTree = new RandomTree();
		randomTree.setMinNodeSize(this.getMinNodeSize());
		randomTree.setNumericSelectionProb(getNumericSelectionProb());
		randomTree.setColSelector(getColSelector());
		return randomTree;
	}

	public int getMinNodeSize() {
		return minNodeSize;
	}

	public void setMinNodeSize(int minNodeSize) {
		this.minNodeSize = minNodeSize;
	}

	public double getNumericSelectionProb() {
		return numericSelectionProb;
	}

	public void setNumericSelectionProb(double numericSelectionProb) {
		this.numericSelectionProb = numericSelectionProb;
	}

	public ColSelector getColSelector() {
		return this.colSelector;
	}

	public void setColSelector(ColSelector colSelector) {
		this.colSelector = colSelector;
	}

	public double[] getVariableImportance() {
		double[] vi = new double[sumVI.length];
		for (int i = 0; i < vi.length; i++) {
			if (cntVI[i] != 0) {
				vi[i] = sumVI[i] / cntVI[i];
			}
		}
		return vi;
	}

	@Override
	public void learn(Frame df, List<Double> weights, String classColName) {

		this.classColName = classColName;
		int[] indexes = new int[df.getColCount() - 1];
		int pos = 0;
		for (int i = 0; i < df.getColCount(); i++) {
			if (i != df.getColIndex(classColName)) {
				indexes[pos++] = i;
			}
		}
		this.dict = df.getCol(classColName).getDictionary();
		this.root = new TreeNode();
		this.sumVI = new double[df.getColCount()];
		this.cntVI = new double[df.getColCount()];
		this.root.learn(df, weights, indexes, this);
	}

	@Override
	public void predict(Frame df) {
		prediction = new Nominal(df.getRowCount(), dict);
		d = Frames.newMatrixFrame(df.getRowCount(), dict);
		for (int i = 0; i < df.getRowCount(); i++) {
			double[] distribution = predict(df, i, root);
			for (int j = 0; j < distribution.length; j++) {
				d.setValue(i, j, distribution[j]);
			}
			int[] indexes = new int[distribution.length];
			int len = 1;
			indexes[0] = 1;
			for (int j = 1; j < distribution.length; j++) {
				if (distribution[j] == distribution[indexes[0]]) {
					indexes[len++] = j;
					continue;
				}
				if (distribution[j] > distribution[indexes[0]]) {
					len = 1;
					indexes[0] = j;
				}
			}
			prediction.setLabel(i, dict[indexes[RandomSource.nextInt(len)]]);
		}
	}

	@Override
	public Nominal getPrediction() {
		return prediction;
	}

	@Override
	public Frame getDistribution() {
		return d;
	}

	private double[] predict(Frame df, int row, TreeNode node) {
		if (node.leaf) {
			return node.pd;
		}
		int col = df.getColIndex(node.splitCol);
		if (df.getCol(col).isMissing(row)) {
			double[] left = predict(df, row, node.leftNode);
			double[] right = predict(df, row, node.rightNode);
			double[] pd = new double[dict.length];

			double pleft = node.leftNode.totalFd;
			double pright = node.leftNode.totalFd;
			for (int i = 0; i < dict.length; i++) {
				pd[i] = (pleft * left[i] + pright * right[i]) / (pleft + pright);
			}
			return pd;
		}
		if (df.getCol(col).getType().isNumeric()) {
			double value = df.getValue(row, col);
			return predict(df, row, value <= node.splitValue ? node.leftNode : node.rightNode);
		} else {
			String label = df.getLabel(row, col);
			return predict(df, row, node.splitLabel.equals(label) ? node.leftNode : node.rightNode);
		}
	}

	@Override
	public void summary() {
		//TODO implement summary of random tree
	}
}

class TreeNode {

	public boolean leaf = false;
	public String splitCol;
	public String splitLabel;
	public double splitValue;
	public double metricValue = Double.NaN;
	public double[] pd;
	public double[] fd;
	public double totalFd;

	public String predicted;
	public TreeNode leftNode;
	public TreeNode rightNode;

	public void learn(final Frame df, List<Double> weights, int[] indexes, RandomTree tree) {

		Vector classCol = df.getCol(tree.classColName);
		int classColIndex = df.getColIndex(tree.classColName);

		// compute distribution of classes
		fd = new double[tree.dict.length];
		for (int i = 0; i < df.getRowCount(); i++) {
			fd[classCol.getIndex(i)] += weights.get(i);
		}
		for (int i = 0; i < fd.length; i++) {
			totalFd += fd[i];
		}
		pd = new double[fd.length];
		for (int i = 0; i < pd.length; i++) {
			pd[i] = fd[i] / totalFd;
		}

		if (df.getRowCount() == 1) {
			predicted = classCol.getLabel(0);
			leaf = true;
			return;
		}

		if (df.getRowCount() <= tree.getMinNodeSize()) {
			String[] modes = new Mode(classCol, false).getModes();
			predicted = modes[RandomSource.nextInt(modes.length)];
			leaf = true;
			return;
		}

		// leaf on all classes of same getValue
		for (int i = 1; i < fd.length; i++) {
			if (abs(fd[i] - totalFd) <= 1e-30) {
				predicted = classCol.getLabel(0);
				leaf = true;
				return;
			}
			if (fd[i] != 0)
				break;
		}

		// find best split
		String[] colSel = tree.getColSelector().nextColNames();
		for (String colName : colSel) {
			int colIndex = df.getColIndex(colName);

			Vector col = df.getCol(colIndex);
			if (col.getType().isNumeric()) {
				evaluateNumericCol(df, weights, classColIndex, classCol, colIndex, col, tree);
			} else {
				evaluateNominalCol(df, weights, classColIndex, classCol, colIndex, col);
			}
		}
		if (leftNode != null && rightNode != null) {
			// learn data for left and right nodes
			Mapping leftMap = new Mapping();
			List<Double> leftWeights = new ArrayList<>();
			Mapping rightMap = new Mapping();
			List<Double> rightWeights = new ArrayList<>();
			double leftWeight = 0;
			double rightWeight = 0;

			Vector col = df.getCol(splitCol);
			double missingWeight = 0;

			for (int i = 0; i < df.getRowCount(); i++) {
				int id = df.getRowId(i);
				if (col.isMissing(i)) {
					missingWeight += weights.get(i);
					continue;
				}
				if ((col.getType().isNominal() && splitLabel.equals(col.getLabel(i)))
						|| col.getType().isNumeric() && col.getValue(i) <= splitValue) {
					leftMap.add(id);
					leftWeights.add(weights.get(i));
					leftWeight += weights.get(i);
				} else {
					rightMap.add(id);
					rightWeights.add(weights.get(i));
					rightWeight += weights.get(i);
				}
			}

			double pleft = leftWeight / (leftWeight + rightWeight);
			for (int i = 0; i < df.getRowCount(); i++) {
				int id = df.getRowId(i);
				if (col.isMissing(i)) {
					if (false) {
						if (RandomSource.nextDouble() > .5) {
							leftMap.add(id);
							leftWeights.add(weights.get(i) * 0.5);
						} else {
							rightMap.add(id);
							rightWeights.add(weights.get(i) * .5);
						}
					} else {
						leftMap.add(id);
						leftWeights.add(weights.get(i) * pleft);
						rightMap.add(id);
						rightWeights.add(weights.get(i) * (1. - pleft));
					}
				}
			}
			// sum to variable importance
			tree.sumVI[df.getColIndex(splitCol)] += metricValue * (1 - missingWeight / totalFd);
			tree.cntVI[df.getColIndex(splitCol)]++;

			Frame leftFrame = new MappedFrame(df.getSourceFrame(), leftMap);
			leftNode.learn(leftFrame, leftWeights, indexes, tree);

			Frame rightFrame = new MappedFrame(df.getSourceFrame(), rightMap);
			rightNode.learn(rightFrame, rightWeights, indexes, tree);
			return;
		}

		String[] modes = new Mode(classCol, false).getModes();
		predicted = modes[RandomSource.nextInt(modes.length)];
		leaf = true;
	}

	private void evaluateNumericCol(Frame df, List<Double> weights, int classColIndex, Vector classCol, int colIndex, Vector col, RandomTree tree) {
		double[][] p = new double[2][fd.length];
		int[] rowCounts = new int[2];

		Vector sort = BaseFilters.sort(Vectors.newSeq(0, df.getRowCount() - 1, 1), RowComparators.numericComparator(col, true));
		for (int i = 0; i < df.getRowCount() - 1; i++) {
			int row = df.getCol(colIndex).isMissing(sort.getIndex(i)) ? 0 : 1;
			int index = df.getIndex(sort.getIndex(i), classColIndex);
			p[row][index] += weights.get(sort.getIndex(i));
			rowCounts[row]++;
			if (row == 0) {
				continue;
			}
			if (rowCounts[1] == 0)
				continue;
			if (df.getRowCount() - rowCounts[1] - rowCounts[0] == 0)
				continue;
			if (RandomSource.nextDouble() > tree.getNumericSelectionProb())
				continue;
			if (df.getValue(sort.getIndex(i), colIndex) + 1e-30 < df.getValue(sort.getIndex(i + 1), colIndex)) {
				double metric = computeGini(p[0], p[1]);
				if (!validNumber(metric))
					continue;

				if ((metricValue != metricValue) || metric > metricValue) {
					metricValue = metric;
					splitCol = df.getColNames()[colIndex];
					splitLabel = "";
					splitValue = df.getCol(colIndex).getValue(sort.getIndex(i));
					leftNode = new TreeNode();
					rightNode = new TreeNode();
				}
			}
		}
	}

	private void evaluateNominalCol(Frame df, List<Double> weights, int classColIndex, Vector classCol, int selColIndex, Vector selCol) {
		if (selCol.getDictionary().length == 2) {
			return;
		}
		double[][] p = new double[selCol.getDictionary().length][classCol.getDictionary().length];
		int[] rowCounts = new int[selCol.getDictionary().length];
		for (int i = 0; i < df.getRowCount(); i++) {
			p[selCol.getIndex(i)][classCol.getIndex(i)] += weights.get(i);
			rowCounts[selCol.getIndex(i)]++;
		}

		for (int j = 1; j < selCol.getDictionary().length; j++) {
			if (rowCounts[j] == 0)
				continue;
			if (df.getRowCount() - rowCounts[j] - rowCounts[0] == 0)
				continue;
			if (selCol.getDictionary().length == 3 && j == 2) {
				continue;
			}
			double metric = computeGini(p[0], p[j]);
			if (!validNumber(metric))
				continue;
			if ((metricValue != metricValue) || metric - metricValue > 0) {
				metricValue = metric;
				splitCol = df.getColNames()[selColIndex];
				splitLabel = selCol.getDictionary()[j];
				splitValue = Double.NaN;
				leftNode = new TreeNode();
				rightNode = new TreeNode();
			}
		}
	}

	private double computeGini(double[] missing, double[] pa) {
		double totalOrig = 0;
		double totalLeft = 0;
		double totalRight = 0;
		for (int i = 1; i < fd.length; i++) {
			double left = pa[i];
			double right = fd[i] - pa[i] - missing[i];
			double orig = fd[i] - missing[i];
			totalLeft += left;
			totalRight += right;
			totalOrig += orig;
		}
		if (totalLeft == 0 || totalRight == 0)
			return Double.NaN;
		if (!validNumber(totalLeft) || !validNumber(totalRight))
			return Double.NaN;
		double giniOrig = 1;
		double giniLeft = 1;
		double giniRight = 1;
		for (int i = 1; i < fd.length; i++) {
			double pleft = pa[i] / totalLeft;
			double pright = (fd[i] - pa[i] - missing[i]) / totalRight;
			double porig = (fd[i] - missing[i]) / totalOrig;
			giniOrig -= porig * porig;
			giniLeft -= pleft * pleft;
			giniRight -= pright * pright;
		}
		return giniOrig - (totalLeft * giniLeft + totalRight * giniRight) / (totalLeft + totalRight);
	}
}
