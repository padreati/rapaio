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
import rapaio.data.Frame;
import rapaio.data.Nominal;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;
import rapaio.ml.AbstractClassifier;
import rapaio.workspace.Workspace;

import java.util.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ID3 extends AbstractClassifier<ID3> {

	private ID3Node root;
	private MetricType metricType = new EntropyMetricType();
	private String[] dict;
	private Nominal prediction;

	/**
	 * Metric getType used as criterion for splitting nodes.
	 */
	public static interface MetricType {

		String getMetricTypeName();

		public double compute(Frame df, List<Double> weights, String classColName, String splitColName);

		int compare(double metricValue1, double metricValue2);
	}

	/**
	 * Entropy metric getType
	 */
	public static final class EntropyMetricType implements MetricType {

		@Override
		public String getMetricTypeName() {
			return "Entropy";
		}

		@Override
		public double compute(Frame df, List<Double> weights, String classColName, String splitColName) {
			return new TreeMetrics().entropy(df, weights, classColName, splitColName);
		}

		@Override
		public int compare(double metricValue1, double metricValue2) {
			if (metricValue1 == metricValue2) {
				return 0;
			}
			return (metricValue1 < metricValue2) ? 1 : -1;
		}
	}

	/**
	 * InfoGain metric getType
	 */
	public static class InfoGainMetricType implements MetricType {

		@Override
		public String getMetricTypeName() {
			return "InfoGain";
		}

		@Override
		public double compute(Frame df, List<Double> weights, String classColName, String splitColName) {
			return new TreeMetrics().infoGain(df, weights, classColName, splitColName);
		}

		@Override
		public int compare(double metricValue1, double metricValue2) {
			if (metricValue1 == metricValue2) {
				return 0;
			}
			return (metricValue1 < metricValue2) ? -1 : 1;
		}
	}

	public MetricType getMetricType() {
		return metricType;
	}

	public void setMetricType(MetricType metricType) {
		this.metricType = metricType;
	}

	public ID3 newInstance() {
		ID3 id3 = new ID3();
		id3.setMetricType(this.getMetricType());
		return id3;
	}

	@Override
	public void learn(Frame df, List<Double> weights, String classColName) {
		validate(df, df.getColIndex(classColName));
		this.dict = df.getCol(classColName).getDictionary();
		this.root = new ID3Node(null, df, weights, classColName, new HashSet<String>(), metricType);
	}

	@Override
	public void summary() {

		StringBuilder sb = new StringBuilder();
		sb.append("\nID3 model\n");
		sb.append("Use ").append(getMetricType().getMetricTypeName()).append(" as split criteria\n");
		summary(root, sb, 0);
		Workspace.code(sb.toString());
	}

	private void summary(ID3Node root, StringBuilder sb, int level) {
		if (root.isLeaf()) {
			for (int i = 0; i < level; i++) {
				sb.append("   .");
			}
			sb.append("-> ");
			sb.append("predict:").append(root.getPredicted());
			sb.append("\n");
			return;
		}
		for (String label : root.getSplitMap().keySet()) {
			for (int i = 0; i < level; i++) {
				sb.append("   .");
			}
			sb.append("-> ");
			sb.append("split on ").append(root.getSplitCol()).append(":").append(label);
			sb.append(" (").append(String.format("%.6f", root.getMetricValue())).append(")");
			sb.append("\n");
			summary(root.getSplitMap().get(label), sb, level + 1);
		}
	}

	@Override
	public void predict(final Frame df) {
		prediction = new Nominal(df.getRowCount(), dict);
		for (int i = 0; i < df.getRowCount(); i++) {
			String label = predict(df, i, root);
			prediction.setLabel(i, label);
		}
	}

	@Override
	public Nominal getPrediction() {
		return prediction;
	}

	@Override
	public Frame getDistribution() {
		return null;
	}

	private String predict(Frame df, int row, ID3Node root) {
		if (root.isLeaf()) {
			return root.getPredicted();
		}
		String label = df.getLabel(row, df.getColIndex(root.getSplitCol()));
		Map<String, ID3Node> map = root.getSplitMap();
		if (!map.containsKey(label)) {
			throw new RuntimeException("Inconsistency");
		}
		return predict(df, row, map.get(label));
	}

	private void validate(Frame df, int classIndex) {
		for (int i = 0; i < df.getColCount(); i++) {
			if (!df.getCol(i).getType().isNominal()) {
				throw new IllegalArgumentException("ID3 can handle only isNominal attributes.");
			}
		}
		if (df.getColCount() <= classIndex) {
			throw new IllegalArgumentException("Class getIndex is not valid");
		}
	}

}

class ID3Node {

	private final ID3Node parent;
	private final Frame df;
	private final List<Double> weights;
	private final String classColName;
	private final ID3.MetricType metricType;
	//
	private boolean leaf = false;
	private String predicted;
	private String splitCol;
	private HashMap<String, ID3Node> splitMap;
	private double metricValue;

	public ID3Node(final ID3Node parent,
				   final Frame df,
				   final List<Double> weights,
				   final String classColName,
				   final HashSet<String> used,
				   final ID3.MetricType metricType) {
		this.parent = parent;
		this.df = df;
		this.weights = weights;
		this.classColName = classColName;
		this.metricType = metricType;

		learn(used);
	}

	public boolean isLeaf() {
		return leaf;
	}

	public String getPredicted() {
		return predicted;
	}

	public String getSplitCol() {
		return splitCol;
	}

	public Map<String, ID3Node> getSplitMap() {
		return splitMap;
	}

	public double getMetricValue() {
		return metricValue;
	}

	public Frame getFrame() {
		return df;
	}

	private void learn(HashSet<String> used) {
		// leaf on empty set
		if (df == null || df.getRowCount() == 0) {
			if (parent == null) {
				throw new IllegalArgumentException("Can't train from an empty frame");
			}
			String[] modes = new Mode(parent.df.getCol(classColName), false).getModes();
			if (modes.length == 0) {
				throw new IllegalArgumentException("Can't train from an empty frame");
			}
			predicted = modes[0];
			if (modes.length > 1) {
				predicted = modes[RandomSource.nextInt(modes.length)];
			}
			leaf = true;
			return;
		}

		// leaf on all classes of same getValue
		boolean same = true;
		for (int i = 1; i < df.getRowCount(); i++) {
			if (df.getIndex(i - 1, df.getColIndex(classColName)) != df.getIndex(i, df.getColIndex(classColName))) {
				same = false;
				break;
			}
		}
		if (same) {
			predicted = df.getLabel(0, df.getColIndex(classColName));
			leaf = true;
			return;
		}

		// find best split
		String colName = "";
		metricValue = Double.NaN;

		for (String col : df.getColNames()) {
			if (col.equals(classColName) || used.contains(col)) {
				continue;
			}
			if (colName.isEmpty()) {
				metricValue = metricType.compute(df, weights, classColName, col);
				colName = col;
				continue;
			}
			double lastMetricValue = metricType.compute(df, weights, classColName, col);
			if (metricType.compare(lastMetricValue, metricValue) > 0) {
				metricValue = lastMetricValue;
				colName = col;
			}
		}

		// if none were selected then there are no columns to select
		if (colName.isEmpty()) {
			String[] modes = new Mode(df.getCol(classColName), false).getModes();
			if (modes.length == 0) {
				throw new IllegalArgumentException("Can't train from an empty frame");
			}
			predicted = modes[RandomSource.nextInt(modes.length)];
			leaf = true;
			return;
		}

		// usual case, a split node
		String[] dict = df.getCol(colName).getDictionary();
		List<Integer>[] splitIds = new List[dict.length];
		List<Double>[] splitWeights = new List[dict.length];

		for (int i = 0; i < dict.length; i++) {
			splitIds[i] = new ArrayList<>();
			splitWeights[i] = new ArrayList<>();
		}

		for (int i = 0; i < df.getRowCount(); i++) {
			int index = df.getIndex(i, df.getColIndex(colName));
			splitIds[index].add(df.getRowId(i));
			splitWeights[index].add(weights.get(i));
		}
		Frame[] frames = new Frame[dict.length];
		for (int i = 0; i < dict.length; i++) {
			frames[i] = new MappedFrame(df.getSourceFrame(), new Mapping(splitIds[i]));
		}

		splitCol = colName;
		splitMap = new HashMap<>();
		HashSet<String> newUsed = new HashSet<>(used);
		newUsed.add(colName);
		for (int i = 0; i < dict.length; i++) {
			splitMap.put(dict[i], new ID3Node(this, frames[i], splitWeights[i], classColName, newUsed, metricType));
		}
	}
}
