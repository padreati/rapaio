/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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
 *
 */

package rapaio.experiment.grid;

import rapaio.core.stat.Maximum;
import rapaio.core.stat.Minimum;
import rapaio.core.stat.Quantiles;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;

import java.io.Serializable;
import java.util.function.BiFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/22/15.
 */
public class GridData implements Serializable {

    public static GridData fromPrediction(ClassifierModel c, Frame df, String xName, String yName, String labelName, int bins) {

        double xMin = Minimum.of(df.rvar(xName)).value();
        double xMax = Maximum.of(df.rvar(xName)).value();
        double yMin = Minimum.of(df.rvar(yName)).value();
        double yMax = Maximum.of(df.rvar(yName)).value();

        VarDouble x = VarDouble.seq(xMin, xMax, (xMax - xMin) / bins).name(xName);
        VarDouble y = VarDouble.seq(yMin, yMax, (yMax - yMin) / bins).name(yName);

        GridData mg = new GridData(x, y);

        VarDouble f1 = VarDouble.empty().name(xName);
        VarDouble f2 = VarDouble.empty().name(yName);

        for (int i = 0; i < x.size(); i++) {
            for (int j = 0; j < y.size(); j++) {
                f1.addDouble(x.getDouble(i));
                f2.addDouble(y.getDouble(j));
            }
        }
        ClassifierResult pred = c.predict(SolidFrame.byVars(f1, f2));
        int pos = 0;
        Var density = pred.firstDensity().rvar(1);
        for (int i = 0; i < x.size(); i++) {
            for (int j = 0; j < y.size(); j++) {
                mg.setValue(i, j, density.getDouble(pos++));
            }
        }
        return mg;
    }

    public static GridData fromFunction(BiFunction<Double, Double, Double> f, double xMin, double xMax, double yMin, double yMax, int bins) {

        VarDouble x = VarDouble.seq(xMin, xMax, (xMax - xMin) / bins);
        VarDouble y = VarDouble.seq(yMin, yMax, (yMax - yMin) / bins);

        GridData mg = new GridData(x, y);
        for (int i = 0; i < x.size(); i++) {
            for (int j = 0; j < y.size(); j++) {
                mg.setValue(i, j, f.apply(x.getDouble(i), y.getDouble(j)));
            }
        }
        return mg;
    }

    public static GridData fromVars(Var x, Var y) {
        return new GridData(x, y);
    }

    private static final long serialVersionUID = 779676910310235832L;
    private final Var x;
    private final Var y;

    private final VarDouble values;

    private GridData(Var x, Var y) {
        this.x = x;
        this.y = y;

        this.values = VarDouble.empty(x.size() * y.size());
    }

    public Var getX() {
        return x;
    }

    public Var getY() {
        return y;
    }

    public double getValue(int i, int j) {
        return values.getDouble(i * y.size() + j);
    }

    public void setValue(int i, int j, double value) {
        values.setDouble(i * y.size() + j, value);
    }

    public double[] quantiles(double... qs) {
        return Quantiles.of(values, qs).values();
    }
}
