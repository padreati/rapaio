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

package rapaio.core.stat;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.VarDouble;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/18.
 */
public class CovarianceTest {

    private static final double TOL = 1e-12;

    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random(123);
    }

    @Test
    void testDouble() {
        VarDouble x = VarDouble.from(100, row -> row % 7 == 0 ? Double.NaN : random.nextDouble());
        VarDouble y = VarDouble.from(100, row -> row % 7 == 0 ? Double.NaN : random.nextDouble());
        double mu1 = Mean.of(x).value();
        double mu2 = Mean.of(y).value();

        double cs = 0;
        double s1 = 0.0;
        double s2 = 0.0;
        double count = 0.0;
        for (int i = 0; i < x.size(); i++) {
            if (x.isMissing(i)) {
                continue;
            }
            count++;
            s1 += Math.pow(x.getDouble(i) - mu1, 2);
            s2 += Math.pow(y.getDouble(i) - mu2, 2);
            cs += (x.getDouble(i) - mu1) * (y.getDouble(i) - mu2);
        }
        Covariance cov = Covariance.of(x, y);
        assertEquals(cs / (count - 1), cov.value(), TOL);

        assertEquals("> cov[?,?]\n" +
                "total rows: 100 (complete: 85, missing: 15)\n" +
                "covariance: -0.0050385\n", cov.toSummary());
    }

    @Test
    void testEmpty() {
        assertEquals(Double.NaN, Covariance.of(VarDouble.empty(10), VarDouble.empty(10)).value(), TOL);
    }
}
