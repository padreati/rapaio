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

package rapaio.experiment.math.optimization;

import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.LUDecomposition;
import rapaio.math.linear.dense.SolidDMatrix;
import rapaio.math.linear.dense.SolidDVector;
import rapaio.printer.format.Format;

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
    private DMatrix hessian;
    /**
     * Contains the values of the coefficients for each data point
     */
    private DMatrix coef;
    private DVector derivatives;
    private DVector err;
    private DMatrix grad;

    /**
     * Performs optimization on the given inputs to find the minima of the function.
     *
     * @param eps            the desired accuracy of the result.
     * @param iterationLimit the maximum number of iteration steps to allow
     * @param f              the function to optimize
     * @param fd             the derivative of the function to optimize
     * @param w              contains the initial estimate of the minima. The length should be equal to the number of variables being solved for. This value may be altered.
     * @param xs             a list of input data point values to learn from
     * @param y              a vector containing the true values for each data point in <tt>inputs</tt>
     * @return the compute value for the optimization.
     */
    public VarDouble optimize(double eps, int iterationLimit,
                              Function<Var, Double> f, Function<Var, Double> fd,
                              VarDouble w, List<Var> xs, VarDouble y) {

        hessian = SolidDMatrix.empty(w.rowCount(), w.rowCount());
        coef = SolidDMatrix.empty(xs.size(), w.rowCount());
        for (int i = 0; i < xs.size(); i++) {
            Var x_i = xs.get(i);
            coef.set(i, 0, 1.0);
            for (int j = 1; j < w.rowCount(); j++)
                coef.set(i, j, x_i.getDouble(j - 1));
        }

        derivatives = SolidDVector.zeros(xs.size());
        err = SolidDVector.zeros(y.rowCount());
        grad = SolidDMatrix.empty(w.rowCount(), 1);

        double maxChange = Double.MAX_VALUE;
        while (!Double.isNaN(maxChange) && maxChange > eps && iterationLimit-- > 0) {
            maxChange = iterationStep(f, fd, w, xs, y);
            logger.finer("IRLS maxChange: " + Format.floatFlex(maxChange));
        }
        return w;
    }

    private double iterationStep(Function<Var, Double> f, Function<Var, Double> fd, VarDouble w, List<Var> xs, VarDouble y) {
        for (int i = 0; i < xs.size(); i++) {
            Var x_i = xs.get(i);
            err.set(i, f.apply(x_i) - y.getDouble(i));
            derivatives.set(i, fd.apply(x_i));
        }

        for (int j = 0; j < hessian.rowCount(); j++) {
            double gradTmp = 0;
            for (int k = 0; k < coef.rowCount(); k++) {
                double coefficient_kj = coef.get(k, j);
                gradTmp += coefficient_kj * err.get(k);

                double factor = derivatives.get(k) * coefficient_kj;

                for (int i = 0; i < hessian.rowCount(); i++)
                    hessian.set(j, i, hessian.get(j, i) + coef.get(k, i) * factor);
            }
            grad.set(j, 0, gradTmp);
        }

        LUDecomposition lu = LUDecomposition.from(hessian.copy());

        //We sent a clone of the hessian b/c we make incremental updates every iteration
        if (Math.abs(lu.det()) < 1e-14) {
            //TODO use a pesudo inverse instead of giving up
            return Double.NaN;//Indicate that we need to stop
        }
        DVector delta = lu.solve(grad).mapCol(0);

        w.op().minus(delta.asVarDouble());

        double max = Math.abs(delta.get(0));
        double min = Math.abs(delta.get(0));
        for (int i = 1; i < delta.size(); i++) {
            max = Math.max(max, Math.abs(delta.get(i)));
            min = Math.min(min, Math.abs(delta.get(i)));
        }
        return max - min;
    }
}
