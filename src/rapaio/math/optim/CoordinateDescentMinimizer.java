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

package rapaio.math.optim;

import rapaio.data.NumVar;
import rapaio.math.functions.RDerivative;
import rapaio.math.functions.RFunction;
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.SolidRV;
import rapaio.math.optim.linesearch.BacktrackLineSearch;
import rapaio.math.optim.linesearch.FixedLineSearch;
import rapaio.math.optim.linesearch.LineSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.lang.Math.*;

/**
 * Steepest descent for L1 norm
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/19/17.
 */
public class CoordinateDescentMinimizer implements Minimizer {

    private double tol = 1e-10;
    private int maxIt = 10_000;

    private final LineSearch lineSearch = BacktrackLineSearch.from();

    private final RV x;
    private final RFunction f;
    private final RDerivative d1f;

    private RV sol;

    private List<RV> solutions = new ArrayList<>();
    private NumVar errors;
    private boolean converged = false;

    public CoordinateDescentMinimizer(RV x, RFunction f, RDerivative d1f, int maxInt) {
        this.x = x;
        this.f = f;
        this.d1f = d1f;
        this.maxIt = maxInt;
    }

    @Override
    public void compute() {

        converged = false;
        sol = x.solidCopy();
        for (int i = 0; i < maxIt; i++) {
            solutions.add(sol.solidCopy());
            RV d1f_x = d1f.apply(sol);
            double max = abs(d1f_x.get(0));
            int index = 0;
            for (int j = 1; j < d1f_x.count(); j++) {
                if (abs(d1f_x.get(j)) > max) {
                    max = abs(d1f_x.get(j));
                    index = j;
                }
            }
            RV delta_x = SolidRV.fill(d1f_x.count(), 0);
            delta_x.set(index, -signum(d1f_x.get(index)));

            if (abs(delta_x.norm(2)) < tol) {
                converged = true;
                break;
            }
            double t = lineSearch.find(f, d1f, x, delta_x);
            sol.plus(delta_x.dot(t));
        }
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("solution: ").append(sol.summary()).append("\n");
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
