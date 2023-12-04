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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.optimization;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.MessageFormat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;
import rapaio.datasets.Datasets;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/29/21.
 */
public class IRLSSolverTest {

    private static final double eps = 1e-10;

    private DMatrix A;
    private DVector b;

    @BeforeEach
    public void beforeEach() {
        Frame df = Datasets.loasSAheart().removeVars(0).removeVars("typea,adiposity");
        VarDouble intercept = VarDouble.fill(df.rowCount(), 1).name("(Intercept)");
        Frame dfa = SolidFrame.byVars(intercept).bindVars(df.removeVars("chd"));
        A = DMatrix.copy(dfa);
        b = DMatrix.copy(df.mapVars(VarRange.of("chd"))).mapCol(0);
    }

    @Test
    void testMultipleParamValues() {
        double[] pp = new double[]{1, 1.5, 1.99, 2.5, 5, 10, 100};
        double[] kk = new double[]{0.8, 0.8, 0.8, 1.1, 1.1, 1.1, 1.9};

        for (int i = 0; i < pp.length; i++) {
            double p = pp[i];
            double k = kk[i];

            IRLSSolver m0 = IRLSSolver.newMinimizer().method.set(IRLSSolver.Method.IRLS0M).m.set(A).b.set(b).p.set(p).k.set(k)
                    .maxIt.set(500).eps.set(1e-10).compute();
            IRLSSolver m1 = IRLSSolver.newMinimizer().method.set(IRLSSolver.Method.IRLS1M).m.set(A).b.set(b).p.set(p).k.set(k)
                    .maxIt.set(500).eps.set(1e-10).compute();
//            WS.draw(lines(m0.errors(), color('r')).lines(m1.errors(), color('g')));
            assertTrue(m0.errors().getDouble(m0.errors().size() - 1) >= m1.errors().getDouble(m1.errors().size() - 1),
                    MessageFormat.format("error at p={0}, k={1}, sol.m0={2}", p, k, m0.solution().toString()));
        }
    }

    @Test
    void testLeastSquares() {
        Solver irls = IRLSSolver.newMinimizer().m.set(A).b.set(b).p.set(2.0).maxIt.set(10_000).eps.set(1e-20);
        DVector irlsSolution = irls.compute().solution();

        DMatrix ata = A.t().dot(A);
        DVector ab = A.t().dot(b);
        DVector sol = ata.qr().solve(ab);
        assertTrue(irlsSolution.deepEquals(sol, eps));
    }

}
