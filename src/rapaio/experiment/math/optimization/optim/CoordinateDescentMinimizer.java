/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.experiment.math.optimization.optim;

import rapaio.data.VarDouble;
import rapaio.experiment.math.functions.RDerivative;
import rapaio.experiment.math.functions.RFunction;
import rapaio.experiment.math.optimization.optim.linesearch.BacktrackLineSearch;
import rapaio.experiment.math.optimization.optim.linesearch.LineSearch;
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.SolidRV;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

/**
 * Steepest descent for L1 norm
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/19/17.
 */
public class CoordinateDescentMinimizer implements Minimizer {

    private double tol = 1e-10;
    private int maxIt;

    private final LineSearch lineSearch = BacktrackLineSearch.from();

    private final RV x;
    private final RFunction f;
    private final RDerivative d1f;

    private RV sol;

    private List<RV> solutions = new ArrayList<>();
    private VarDouble errors;
    private boolean converged = false;

    public VarDouble getErrors() {
        return errors;
    }

    public CoordinateDescentMinimizer(RV x, RFunction f, RDerivative d1f, int maxInt) {
        this.x = x;
        this.f = f;
        this.d1f = d1f;
        this.maxIt = maxInt;
    }

    @Override
    public void compute() {

        converged = false;
        sol = x.copy();
        for (int i = 0; i < maxIt; i++) {
            solutions.add(sol.copy());
            RV d1fx = d1f.apply(sol);
            double max = abs(d1fx.get(0));
            int index = 0;
            for (int j = 1; j < d1fx.size(); j++) {
                if (abs(d1fx.get(j)) > max) {
                    max = abs(d1fx.get(j));
                    index = j;
                }
            }
            RV deltaX = SolidRV.fill(d1fx.size(), 0);
            deltaX.set(index, -signum(d1fx.get(index)));

            if (abs(deltaX.norm(2)) < tol) {
                converged = true;
                break;
            }
            double t = lineSearch.find(f, d1f, x, deltaX);
            sol.plus(deltaX.dot(t));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("solution: ").append(sol.toSummary()).append("\n");
        return sb.toString();
    }

    public List<RV> solutions() {
        return solutions;
    }

    public RV solution() {
        return sol;
    }

    @Override
    public boolean hasConverged() {
        return converged;
    }
}
