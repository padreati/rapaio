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

package rapaio.ml.model.km;

import static java.lang.Math.abs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.datasets.Datasets;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.Tensors;
import rapaio.ml.common.distance.Manhattan;
import rapaio.ml.common.distance.MinkowskiDistance;
import rapaio.ml.eval.RandIndex;
import rapaio.ml.model.ClusteringResult;
import rapaio.util.collection.DoubleArrays;

public class KMedoidsTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(2);
    }

    @Test
    void testDynamicDataset() {

        VarDouble x1 = VarDouble.empty().name("x1");
        VarDouble x2 = VarDouble.empty().name("x2");
        Var target = VarInt.empty();

        // 3 clusters around (0,0), (3,3), (0,3)
        double[] m1 = new double[] {0, 5, 0};
        double[] m2 = new double[] {0, 5, 5};
        Normal normal = Normal.std();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 100; j++) {
                x1.addDouble(m1[i] + normal.sampleNext(random));
                x2.addDouble(m2[i] + normal.sampleNext(random));
                target.addInt(i);
            }
        }
        Frame df = SolidFrame.byVars(x1, x2);

        KMedoids alternate = KMedoids.newAlternateModel(3).seed.set(42L);
        ClusteringResult<KMedoids> resultAlternate = alternate.fit(df).predict(df);

        KMedoids pam = KMedoids.newPAMModel(3).seed.set(42L);
        ClusteringResult<KMedoids> resultPAM = pam.fit(df).predict(df);
        assertTrue(RandIndex.from(target, resultAlternate.assignment()).getRandIndex() <
                RandIndex.from(target, resultPAM.assignment()).getRandIndex());
    }

    @Test
    void testIrisDataset() {

        Frame df = Datasets.loadIrisDataset().mapVars(VarRange.onlyTypes(VarType.DOUBLE));
        Var target = Datasets.loadIrisDataset().rvar(4);

        KMedoids alternate = KMedoids.newAlternateModel(3).seed.set(42L);
        ClusteringResult<KMedoids> resultAlternate = alternate.fit(df).predict(df);

        KMedoids pam = KMedoids.newPAMModel(3).seed.set(42L);
        ClusteringResult<KMedoids> resultPAM = pam.fit(df).predict(df);
        assertTrue(RandIndex.from(target, resultAlternate.assignment()).getRandIndex() <
                RandIndex.from(target, resultPAM.assignment()).getRandIndex());
    }

    @Test
    void testNewInstance() {
        KMedoids km = KMedoids.newPAMModel(3).maxIt.set(10).seed.set(42L);
        KMedoids copy = km.newInstance();
        assertEquals(km.toString(), copy.toString());
        KMedoids altered = km.newInstance().distance.set(new MinkowskiDistance(1)).newInstance();
        assertNotEquals(km.toString(), altered.toString());

        assertNotEquals(km, KMedoids.newAlternateModel(3));
    }

    @Test
    void testErrorWithinCluster() {
        Tensor<Double> x = SolidFrame.byVars(VarDouble.seq(10)).dtNew();

        KMedoids km = KMedoids.newAlternateModel(2).seed.set(42L);
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.dim(0), new Manhattan());
        assertEquals(10, km.errorWithinCluster(x, 0, List.of(0, 1, 2, 3, 4), cache));
        assertEquals(0, km.errorWithinCluster(x, 0, null, cache));
    }

    @Test
    void computeAssignmentTest() {
        Tensor<Double> x = SolidFrame.byVars(VarDouble.from(100, () -> Normal.std().sampleNext(random))).dtNew();
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.dim(0), new Manhattan());
        KMedoids km = KMedoids.newAlternateModel(2).seed.set(42L);

        int imin = x.takesq(1, 0).argmin();
        int imax = x.takesq(1, 0).argmax();
        int[] assign = km.computeAssignment(x, new int[] {imin, imax}, cache);

        for (int i = 0; i < x.dim(0); i++) {
            double d1 = cache.get(i, imin, x.takesq(0, i), x.takesq(0, imin));
            double d2 = cache.get(i, imax, x.takesq(0, i), x.takesq(0, imax));
            int iassign = d1 < d2 ? 0 : 1;
            assertEquals(iassign, assign[i]);
        }
    }

    @Test
    void computeErrorTest() {
        Tensor<Double> x = SolidFrame.byVars(VarDouble.seq(10)).dtNew();
        KMedoids km = KMedoids.newAlternateModel(2).seed.set(42L);
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.dim(0), new Manhattan());

        double error = 0;
        for (int i = 0; i < 10; i++) {
            error += i % 2 == 0 ? abs(x.get(i, 0) - x.get(5, 0)) : abs(x.get(i, 0) - x.get(7, 0));
        }
        assertEquals(error, km.computeError(x, new int[] {0, 1, 0, 1, 0, 1, 0, 1, 0, 1}, new int[] {5, 7}, cache));
    }

    @Test
    void updateNewClosestTest() {
        Tensor<Double> x = SolidFrame.byVars(VarDouble.seq(21)).dtNew();
        KMedoids km = KMedoids.newAlternateModel(2).seed.set(42L);
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.dim(0), new Manhattan());

        double[] dv = DoubleArrays.newFill(x.dim(0), Double.NaN);
        double[] ev = DoubleArrays.newFill(x.dim(0), Double.NaN);

        km.updateNewClosest(x, 0, dv, ev, cache);

        Tensor<Double> exp1 = x.takesq(1, 0).copy().sub_(x.get(0, 0)).abs_();
        for (int i = 0; i < x.dim(0); i++) {
            assertEquals(exp1.get(i), dv[i]);
            assertTrue(Double.isNaN(ev[i]));
        }

        km.updateNewClosest(x, 10, dv, ev, cache);
        for (int i = 0; i < x.dim(0); i++) {
            double d = cache.get(i, 0, x.takesq(0, i), x.takesq(0, 0));
            double e = cache.get(i, 10, x.takesq(0, i), x.takesq(0, 10));
            if (d > e) {
                double tmp = d;
                d = e;
                e = tmp;
            }
            assertEquals(d, dv[i]);
            assertEquals(e, ev[i]);
        }

        km.updateNewClosest(x, 20, dv, ev, cache);
        for (int i = 0; i < x.dim(0); i++) {
            double[] v = new double[3];
            v[0] = cache.get(i, 0, x.takesq(0, i), x.takesq(0, 0));
            v[1] = cache.get(i, 10, x.takesq(0, i), x.takesq(0, 10));
            v[2] = cache.get(i, 20, x.takesq(0, i), x.takesq(0, 20));
            DoubleArrays.quickSort(v);
            assertEquals(v[0], dv[i]);
            assertEquals(v[1], ev[i]);
        }

    }

    @Test
    void updateAllClosestTest() {
        Tensor<Double> x = SolidFrame.byVars(VarDouble.seq(21)).dtNew();
        KMedoids km = KMedoids.newAlternateModel(2).seed.set(42L);
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.dim(0), new Manhattan());

        double[] dv = DoubleArrays.newFill(x.dim(0), Double.NaN);
        double[] ev = DoubleArrays.newFill(x.dim(0), Double.NaN);

        km.updateAllClosest(x, new int[] {0, 10, 20}, dv, ev, cache);

        for (int i = 0; i < x.dim(0); i++) {
            double[] v = new double[3];
            v[0] = cache.get(i, 0, x.takesq(0, i), x.takesq(0, 0));
            v[1] = cache.get(i, 10, x.takesq(0, i), x.takesq(0, 10));
            v[2] = cache.get(i, 20, x.takesq(0, i), x.takesq(0, 20));
            DoubleArrays.quickSort(v);
            assertEquals(v[0], dv[i]);
            assertEquals(v[1], ev[i]);
        }
    }

    @Test
    void alternateSwapTest() {

        Tensor<Double> x = Tensors.stride(
                1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,
                9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0
        ).unsqueeze(1);
        KMedoids km = KMedoids.newAlternateModel(2);
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.dim(0), new Manhattan());

        int[] assign = VarInt.from(18, row -> row < 9 ? 0 : 17).elements();
        assertArrayEquals(new int[] {4, 13, 12}, km.alternateSwap(x, new int[] {0, 17, 12}, assign, cache));
    }

    @Test
    void initializePAMTest() {
        Tensor<Double> x = Tensors.stride(
                1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,
                9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0
        ).unsqueeze(1);
        KMedoids km = KMedoids.newAlternateModel(2);
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.dim(0), new Manhattan());

        double[] dv = DoubleArrays.newFill(x.dim(0), Double.NaN);
        double[] ev = DoubleArrays.newFill(x.dim(0), Double.NaN);
        int[] centroids = km.initializePAM(x, dv, ev, cache);
        assertArrayEquals(new int[] {4, 1}, centroids);
    }

    @Test
    void peekFirstCentroidTest() {
        Tensor<Double> x = Tensors.stride(
                1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,
                9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0
        ).unsqueeze(1);
        KMedoids km = KMedoids.newAlternateModel(2);
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.dim(0), new Manhattan());

        assertEquals(4, km.peekFirstCentroid(x, cache));
    }

    @Test
    void peekNextCentroidTest() {
        Tensor<Double> x = Tensors.stride(
                1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,
                9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0
        ).unsqueeze(1);
        KMedoids km = KMedoids.newAlternateModel(2);
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.dim(0), new Manhattan());

        double[] dv = x.takesq(1, 0).copy().sub(x.get(4, 0)).apply(Math::abs).dv().elements();

        assertEquals(1, km.peekNextCentroid(x, Set.of(4), dv, cache));
    }
}
