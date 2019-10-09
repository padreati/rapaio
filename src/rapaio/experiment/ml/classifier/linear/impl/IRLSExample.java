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

package rapaio.experiment.ml.classifier.linear.impl;

import rapaio.core.distributions.Bernoulli;
import rapaio.core.distributions.Uniform;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.math.linear.RM;
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.QRDecomposition;
import rapaio.math.linear.dense.SolidRM;
import rapaio.math.linear.dense.SolidRV;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/30/19.
 */
public class IRLSExample {

    public static double invlogit(double eta) {
        return (1.0 / (1.0 + Math.exp(-eta)));
    }

    public static void main(String[] args) {

        int n = 50_000;
        Uniform uniform = Uniform.of(-2, 2);
        Var intercept = VarDouble.fill(n, 1).withName("intercept");
        Var x = VarDouble.from(n, uniform::sampleNext).withName("x");

        RM X = SolidRM.copy(intercept, x).copy();

        RV beta_sim = SolidRV.wrap(1, 1);
        RV pi_sim = X.dot(beta_sim).apply(IRLSExample::invlogit);

        RV y = SolidRV.from(VarDouble.from(n, row -> Bernoulli.of(pi_sim.get(row)).sampleNext()));


        IRLS_logit(X, y, 500, 1e-10).printSummary();
    }

    public static RV IRLS_logit(RM x, RV y, int maxIter, double tolerance) {

        int n = x.rowCount();
        int p = x.colCount();
        RV W = SolidRV.fill(n, 0);
        RV beta = QRDecomposition.from(x).solve(y.asMatrix()).mapCol(0);

        beta.printSummary();

        // IRLS
        while (maxIter >= 0) {
            maxIter--;

            // evaluate probabilities
            RV pi = x.dot(beta);
            pi.apply(IRLSExample::invlogit);

            // set diagonal
            for (int i = 0; i < n; i++) {
                W.set(i, pi.get(i)*(1-pi.get(i)));
            }

            // updating beta

            x.dot(W).printSummary();

            System.exit(0);
//        beta_star = beta + inv(X.T * W * X) * X.T * (y - pi)
//        # Check for convergence
//                error = max(abs((beta_star - beta)/beta_star))
//        if error < 1e-10:
//        print("Convergence reached after",iter+1,"iterations")
//        return({'Estimate':beta_star,'Iter':iter})
//        # If the convergence criterium is not satisfied, continue
//                beta = beta_star
//        print("Maximum iteration reached without convergence")
        }
        return SolidRV.empty(2);
    }
}
