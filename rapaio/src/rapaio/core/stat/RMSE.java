package rapaio.core.stat;

import rapaio.core.MathBase;
import rapaio.core.Summarizable;
import rapaio.data.Frame;
import rapaio.data.Vector;
import rapaio.workspace.Workspace;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class RMSE implements Summarizable {

	private final List<Vector> source;
	private final List<Vector> target;
	private double value;

	public RMSE(Frame dfSource, Frame dfTarget) {
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

	public RMSE(Vector source, Vector target) {
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
				total += MathBase.pow(source.get(i).getValue(j) - target.get(i).getValue(j), 2);
			}
		}
		value = MathBase.sqrt(total / count);
	}

	public double getValue() {
		return value;
	}

	@Override
	public void summary() {
		Workspace.code("not implemented");
	}
}
