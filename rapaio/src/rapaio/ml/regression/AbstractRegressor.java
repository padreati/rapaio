package rapaio.ml.regression;

import rapaio.data.Frame;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public abstract class AbstractRegressor implements Regressor {

    @Override
    public void learn(Frame df, String targetColName) {
        List<Double> weights = new ArrayList<>();
        for (int i = 0; i < df.getRowCount(); i++) {
            weights.add(1.);
        }
        learn(df, weights, targetColName);
    }
}
