package rapaio.core.stat;

import rapaio.core.BaseMath;
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
        for (int i = 0; i < dfSource.colCount(); i++) {
            if (dfSource.col(i).type().isNumeric()) {
                source.add(dfSource.col(i));
            }
        }
        target = new ArrayList<>();
        for (int i = 0; i < dfTarget.colCount(); i++) {
            if (dfTarget.col(i).type().isNumeric()) {
                target.add(dfTarget.col(i));
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
            for (int j = 0; j < source.get(i).rowCount(); j++) {
                count++;
                total += BaseMath.pow(source.get(i).value(j) - target.get(i).value(j), 2);
            }
        }
        value = BaseMath.sqrt(total / count);
    }

    public double getValue() {
        return value;
    }

    @Override
    public void summary() {
        Workspace.code("not implemented");
    }
}
