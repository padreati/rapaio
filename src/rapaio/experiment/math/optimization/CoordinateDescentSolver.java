/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.math.optimization;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import rapaio.data.VarDouble;
import rapaio.math.functions.RDerivative;
import rapaio.math.functions.RFunction;
import rapaio.math.linear.DVector;
import rapaio.math.optimization.Solver;
import rapaio.math.optimization.linesearch.BacktrackLineSearch;
import rapaio.math.optimization.linesearch.LineSearch;
import rapaio.ml.common.param.ParamSet;
import rapaio.ml.common.param.ValueParam;

/**
 * Steepest descent for L1 norm
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/19/17.
 */
public class CoordinateDescentSolver extends ParamSet<CoordinateDescentSolver> implements Solver {

    @Serial
    private static final long serialVersionUID = 6285470727505415422L;

    public final ValueParam<Double, CoordinateDescentSolver> tol = new ValueParam<>(this, 1e-10, "tol");
    public final ValueParam<Integer, CoordinateDescentSolver> maxIt = new ValueParam<>(this, 100, "maxIt");
    public final ValueParam<LineSearch, CoordinateDescentSolver> lineSearch =
            new ValueParam<>(this, BacktrackLineSearch.newSearch(), "lineSearch");
    public final ValueParam<RFunction, CoordinateDescentSolver> f = new ValueParam<>(this, null, "f");
    public final ValueParam<RDerivative, CoordinateDescentSolver> d1f = new ValueParam<>(this, null, "d1f");
    public final ValueParam<DVector, CoordinateDescentSolver> x0 = new ValueParam<>(this, null, "x0");

    private DVector sol;

    private final List<DVector> solutions = new ArrayList<>();
    private VarDouble errors;
    private boolean converged = false;

    @Override
    public VarDouble errors() {
        return errors;
    }

    @Override
    public CoordinateDescentSolver compute() {

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
            sol.add(deltaX.mul(t));
        }
        return this;
    }

    @Override
    public String toString() {
        return "solution: " + sol.toString() + "\n";
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
