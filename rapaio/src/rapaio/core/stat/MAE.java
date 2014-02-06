package rapaio.core.stat;

import rapaio.core.Summarizable;
import rapaio.data.Frame;
import rapaio.data.Vector;
import rapaio.workspace.Workspace;

import java.util.ArrayList;
import java.util.List;

import static rapaio.core.MathBase.abs;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class MAE implements Summarizable {
	private final List<Vector> source;
	private final List<Vector> target;
	private double value;

	public MAE(Frame dfSource, Frame dfTarget) {
		source = new ArrayList<>();
		for (int i = 0; i < dfSource.getColCount(); i++) {
			if (dfSource.getCol(i).getType().isNumeric()) {
				source.add(dfSource.getCol(i));
			}
		}
		target = new ArrayList<>();
		for (int i = 0; i < dfTarget.getColCount(); i++) {
			if (dfTarget.getCol(i).getType().isNumeric()) {
				target.add(dfTarget.getCol(i));
			}
		}
		compute();
	}

	public MAE(Vector source, Vector target) {
		this.source = new ArrayList<>();
		this.source.add(source);
		this.target = new ArrayList<>();
		this.target.add(target);
		compute();
	}

	private void compute() {
		double total = 0;
		double count = 0;

		for (int i = 0; i < source.size(); i++) {
			for (int j = 0; j < source.get(i).getRowCount(); j++) {
				count++;
				total += abs(source.get(i).getValue(j) - target.get(i).getValue(j));
			}
		}
		value = total / count;
	}

	public double getValue() {
		return value;
	}

	@Override
	public void summary() {
		Workspace.code(String.format("MAE: %.6f", getValue()));
	}
}
