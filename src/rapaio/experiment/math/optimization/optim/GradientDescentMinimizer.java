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

package rapaio.experiment.math.optimization.optim;

import rapaio.data.VarDouble;
import rapaio.experiment.math.functions.RDerivative;
import rapaio.experiment.math.functions.RFunction;
import rapaio.experiment.math.optimization.optim.linesearch.BacktrackLineSearch;
import rapaio.experiment.math.optimization.optim.linesearch.LineSearch;
import rapaio.math.linear.DV;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/18/17.
 */
public class GradientDescentMinimizer implements Minimizer {

    private final double tol = 1e-10;
    private final int maxIt;

    private final LineSearch lineSearch = BacktrackLineSearch.from();

    private final DV x;
    private final RFunction f;
    private final RDerivative d1f;

    private DV sol;

    private final List<DV> solutions = new ArrayList<>();
    private VarDouble errors;
    private boolean converged = false;

    public GradientDescentMinimizer(DV x, RFunction f, RDerivative d1f, int maxIt) {
        this.x = x;
        this.f = f;
        this.d1f = d1f;
        this.maxIt = maxIt;
    }

    public VarDouble getErrors() {
        return errors;
    }

    @Override
    public void compute() {
        converged = false;
        sol = x.copy();
        for (int i = 0; i < maxIt; i++) {
            solutions.add(sol.copy());
            DV delta_x = d1f.apply(sol).mult(-1);
            if (abs(delta_x.norm(2)) < tol) {
                converged = true;
                break;
            }
            double t = lineSearch.find(f, d1f, x, delta_x);
            sol.add(delta_x.mult(t));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("solution: ").append(sol.toString()).append("\n");
        return sb.toString();
    }

    public List<DV> solutions() {
        return solutions;
    }

    public DV solution() {
        return sol;
    }

    @Override
    public boolean hasConverged() {
        return converged;
    }
}
