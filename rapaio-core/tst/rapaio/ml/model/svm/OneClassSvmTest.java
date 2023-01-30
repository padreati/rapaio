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

package rapaio.ml.model.svm;

import java.io.IOException;
import java.util.logging.Level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.core.distributions.Uniform;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.math.linear.DMatrix;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.sys.WS;

public class OneClassSvmTest {

    private final int N = 2000;
    private final int M = N / 10;
    private final int LEN = N + M;
    private Var x1;
    private Var x2;
    private Frame df;

    private DMatrix xs;

    @BeforeEach
    void beforeEach() {
        WS.initLog(Level.SEVERE);

        Normal normal1 = Normal.of(0, 2);
        Normal normal2 = Normal.of(1, 3);
        x1 = VarDouble.from(N, row -> normal1.sampleNext()).name("x1");
        x2 = VarDouble.from(N, row -> x1.getDouble(row) * 2 + normal2.sampleNext()).name("x2");

        // add some random uniform
        Uniform uniform = new Uniform(-10, 10);
        for (int i = 0; i < M; i++) {
            x1.addDouble(uniform.sampleNext());
            x2.addDouble(uniform.sampleNext());
        }
        df = SolidFrame.byVars(x1, x2);
        xs = DMatrix.copy(df);
    }

    @Test
    void testOneClass() throws IOException {

        OneClassSvm ocs = OneClassSvm
                .newModel()
                .kernel.set(new RBFKernel(0.01))
                .nu.set(0.1)
                .seed.set(42L);
        var result = ocs.fit(df, null).predict(df);


//        assertArrayEquals(DVector.wrap(pred.classes()).apply(v -> v < 0 ? 0 : 1).denseCopy().array(),
//                DoubleArrays.newFrom(0, pred.classes().length, i -> result.assignment().getDouble(i)));
    }
}
