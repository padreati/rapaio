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

package rapaio.ml.clustering;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.experiment.ml.clustering.DistanceMatrix;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.dense.DMatrixStripe;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/13/17.
 */
public class VectorDistanceMatrixTest {

    private static final double TOL = 1e-20;

    @BeforeEach
    void setUp() {
        RandomSource.nextDouble();
    }

    @Test
    void testEmpty() {

        Normal normal = Normal.of(0, 10);

        // generate symmetric matrix
        DMatrix sym = DMatrixStripe.empty(4, 4);
        for (int i = 0; i < 4; i++) {
            for (int j = i; j < 4; j++) {
                double next = normal.sampleNext();
                sym.set(i, j, next);
                sym.set(j, i, next);
            }
        }

        // build a distance matrix and copy values from sym
        String[] names = new String[]{"1", "2", "3", "4"};
        DistanceMatrix d = DistanceMatrix.empty(names);
        for (int i = 0; i < 4; i++) {
            for (int j = i; j < 4; j++) {
                d.set(i, j, sym.get(i, j));
            }
        }

        // now compare
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(sym.get(i, j), d.get(i, j), TOL);
            }
        }
        assertEquals(names.length, d.length());
        for (int i = 0; i < d.length(); i++) {
            assertEquals(names[i], d.name(i));
        }
    }
}
