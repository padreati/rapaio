/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import rapaio.data.NumericVar;
import rapaio.data.Var;
import rapaio.math.linear.RM;
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.LUDecomposition;
import rapaio.math.linear.dense.SolidRM;
import rapaio.math.linear.dense.SolidRV;
import rapaio.sys.WS;

import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Iteratively Re-weighted Least Squares optimization algorithm.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/3/15.
 */
@Deprecated
public class IRLSOptimizer {

    private static final Logger logger = Logger.getLogger(IRLSOptimizer.class.getName());

    /**
     * The hessian matrix
     */
    private RM hessian;
    /**
     * Contains the values of the coefficients for each data point
     */
    private RM coef;
    private RV derivatives;
    private RV err;
    private RM grad;

    /**
     * Performs optimization on the given inputs to find the minima of the function.
     *
     * @param eps            the desired accuracy of the result.
     * @param iterationLimit the maximum number of iteration steps to allow
     * @param f              the function to optimize
     * @param fd             the derivative of the function to optimize
     * @param vars           contains the initial estimate of the minima. The length should be equal to the number of variables being solved for. This value may be altered.
     * @param inputs         a list of input data point values to learn from
     * @param outputs        a vector containing the true values for each data point in <tt>inputs</tt>
     * @return the compute value for the optimization.
     */
    public NumericVar optimize(double eps, int iterationLimit, Function<Var, Double> f,
                               Function<Var, Double> fd, NumericVar vars, List<Var> inputs, NumericVar outputs) {

        hessian = SolidRM.empty(vars.getRowCount(), vars.getRowCount());
        coef = SolidRM.empty(inputs.size(), vars.getRowCount());
        for (int i = 0; i < inputs.size(); i++) {
            Var x_i = inputs.get(i);
            coef.set(i, 0, 1.0);
            for (int j = 1; j < vars.getRowCount(); j++)
                coef.set(i, j, x_i.getValue(j - 1));
        }

        derivatives = SolidRV.empty(inputs.size());
        err = SolidRV.empty(outputs.getRowCount());
        grad = SolidRM.empty(vars.getRowCount(), 1);

        double maxChange = Double.MAX_VALUE;
        while (!Double.isNaN(maxChange) && maxChange > eps && iterationLimit-- > 0) {
            maxChange = iterationStep(f, fd, vars, inputs, outputs);
            logger.finer("IRLS maxChange: " + WS.formatFlex(maxChange));
        }
        return vars;
    }

    private double iterationStep(Function<Var, Double> f, Function<Var, Double> fd, NumericVar vars, List<Var> inputs, NumericVar outputs) {
        for (int i = 0; i < inputs.size(); i++) {
            Var x_i = inputs.get(i);
            double y = f.apply(x_i);
            double error = y - outputs.getValue(i);
            err.set(i, error);
            derivatives.set(i, fd.apply(x_i));
        }

        for (int j = 0; j < hessian.getRowCount(); j++) {
            double gradTmp = 0;
            for (int k = 0; k < coef.getRowCount(); k++) {
                double coefficient_kj = coef.get(k, j);
                gradTmp += coefficient_kj * err.get(k);

                double factor = derivatives.get(k) * coefficient_kj;

                for (int i = 0; i < hessian.getRowCount(); i++)
                    hessian.increment(j, i, coef.get(k, i) * factor);
            }
            grad.set(j, 0, gradTmp);
        }

        LUDecomposition lu = LUDecomposition.from(hessian);

        //We sent a clone of the hessian b/c we make incremental updates every iteration
        if (Math.abs(lu.det()) < 1e-14) {
            //TODO use a pesudo inverse instead of giving up
            return Double.NaN;//Indicate that we need to stop
        }
        RV delta = lu.solve(grad).mapCol(0);

        for (int i = 0; i < vars.getRowCount(); i++)
            vars.setValue(i, vars.getValue(i) - delta.get(i));

        double max = Math.abs(delta.get(0));
        for (int i = 1; i < delta.count(); i++) {
            max = Math.max(max, Math.abs(delta.get(i)));
        }
        return max;
    }
}
