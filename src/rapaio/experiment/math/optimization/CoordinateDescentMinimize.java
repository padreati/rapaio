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

package rapaio.experiment.math.optimization;

import rapaio.data.VarDouble;
import rapaio.math.functions.RDerivative;
import rapaio.math.functions.RFunction;
import rapaio.math.linear.DVector;
import rapaio.math.optimization.Minimize;
import rapaio.math.optimization.linesearch.BacktrackLineSearch;
import rapaio.math.optimization.linesearch.LineSearch;
import rapaio.ml.common.ParamSet;
import rapaio.ml.common.ValueParam;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

/**
 * Steepest descent for L1 norm
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/19/17.
 */
public class CoordinateDescentMinimize extends ParamSet<CoordinateDescentMinimize> implements Minimize {

    private static final long serialVersionUID = 6285470727505415422L;

    public final ValueParam<Double, CoordinateDescentMinimize> tol = new ValueParam<>(this, 1e-10,
            "tol", "Tolerance on errors for determining convergence");
    public final ValueParam<Integer, CoordinateDescentMinimize> maxIt = new ValueParam<>(this, 100,
            "maxIt", "Maximum number of iterations");
    public final ValueParam<LineSearch, CoordinateDescentMinimize> lineSearch = new ValueParam<>(this,
            BacktrackLineSearch.newSearch(), "lineSearch", "Line search algorithm");

    public final ValueParam<RFunction, CoordinateDescentMinimize> f = new ValueParam<>(this,
            null, "f", "function to be optimized");
    public final ValueParam<RDerivative, CoordinateDescentMinimize> d1f = new ValueParam<>(this,
            null, "d1f", "function's derivative");
    public final ValueParam<DVector, CoordinateDescentMinimize> x0 = new ValueParam<>(this,
            null, "x0", "initial value");

    private DVector sol;

    private final List<DVector> solutions = new ArrayList<>();
    private VarDouble errors;
    private boolean converged = false;

    public VarDouble getErrors() {
        return errors;
    }

    @Override
    public void compute() {

        converged = false;
        sol = x0.get().copy();
        for (int i = 0; i < maxIt.get(); i++) {
            solutions.add(sol.copy());
            DVector d1fx = d1f.get().apply(sol);
            double max = abs(d1fx.get(0));
            int index = 0;
            for (int j = 1; j < d1fx.size(); j++) {
                if (abs(d1fx.get(j)) > max) {
                    max = abs(d1fx.get(j));
                    index = j;
                }
            }
            DVector deltaX = DVector.fill(d1fx.size(), 0);
            deltaX.set(index, -signum(d1fx.get(index)));

            if (abs(deltaX.norm(2)) < tol.get()) {
                converged = true;
                break;
            }
            double t = lineSearch.get().search(f.get(), d1f.get(), x0.get(), deltaX);
            sol.add(deltaX.mult(t));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("solution: ").append(sol.toString()).append("\n");
        return sb.toString();
    }

    public List<DVector> solutions() {
        return solutions;
    }

    public DVector solution() {
        return sol;
    }

    @Override
    public boolean hasConverged() {
        return converged;
    }
}
