/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.clustering.km;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.math.MathTools.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.datasets.Datasets;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.clustering.ClusteringResult;
import rapaio.ml.common.distance.Manhattan;
import rapaio.ml.common.distance.MinkowskiDistance;
import rapaio.ml.eval.RandIndex;
import rapaio.util.collection.DoubleArrays;

public class KMedoidsTest {


    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(2);
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
                x1.addDouble(m1[i] + normal.sampleNext());
                x2.addDouble(m2[i] + normal.sampleNext());
                target.addInt(i);
            }
        }
        Frame df = SolidFrame.byVars(x1, x2);

        KMedoids alternate = KMedoids.newAlternateModel(3);
        ClusteringResult<KMedoids> resultAlternate = alternate.fit(df).predict(df);

        KMedoids pam = KMedoids.newPAMModel(3);
        ClusteringResult<KMedoids> resultPAM = pam.fit(df).predict(df);
        assertTrue(RandIndex.from(target, resultAlternate.getAssignment()).getRandIndex() <
                RandIndex.from(target, resultPAM.getAssignment()).getRandIndex());
    }

    @Test
    void testIrisDataset() {

        Frame df = Datasets.loadIrisDataset().mapVars(VarRange.onlyTypes(VarType.DOUBLE));
        Var target = Datasets.loadIrisDataset().rvar(4);

        KMedoids alternate = KMedoids.newAlternateModel(3);
        ClusteringResult<KMedoids> resultAlternate = alternate.fit(df).predict(df);

        KMedoids pam = KMedoids.newPAMModel(3);
        ClusteringResult<KMedoids> resultPAM = pam.fit(df).predict(df);
        assertTrue(RandIndex.from(target, resultAlternate.getAssignment()).getRandIndex() <
                RandIndex.from(target, resultPAM.getAssignment()).getRandIndex());
    }

    @Test
    void testNewInstance() {
        KMedoids km = KMedoids.newPAMModel(3).maxIt.set(10);
        KMedoids copy = km.newInstance();
        assertEquals(km.toString(), copy.toString());
        KMedoids altered = km.newInstance().distance.set(new MinkowskiDistance(1)).newInstance();
        assertNotEquals(km.toString(), altered.toString());

        assertNotEquals(km, KMedoids.newAlternateModel(3));
    }

    @Test
    void testErrorWithinCluster() {
        DMatrix x = DMatrix.copy(VarDouble.seq(10));

        KMedoids km = KMedoids.newAlternateModel(2);
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.rowCount(), new Manhattan());
        assertEquals(10, km.errorWithinCluster(x, 0, List.of(0, 1, 2, 3, 4), cache));
        assertEquals(0, km.errorWithinCluster(x, 0, null, cache));
    }

    @Test
    void computeAssignmentTest() {
        DMatrix x = DMatrix.copy(VarDouble.from(100, () -> Normal.std().sampleNext()));
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.rowCount(), new Manhattan());
        KMedoids km = KMedoids.newAlternateModel(2);

        int imin = x.mapCol(0).argmin();
        int imax = x.mapCol(0).argmax();
        int[] assign = km.computeAssignment(x, new int[] {imin, imax}, cache);

        for (int i = 0; i < x.rowCount(); i++) {
            double d1 = cache.get(i, imin, x.mapRow(i), x.mapRow(imin));
            double d2 = cache.get(i, imax, x.mapRow(i), x.mapRow(imax));
            int iassign = d1 < d2 ? 0 : 1;
            assertEquals(iassign, assign[i]);
        }
    }

    @Test
    void computeErrorTest() {
        DMatrix x = DMatrix.copy(VarDouble.seq(10));
        KMedoids km = KMedoids.newAlternateModel(2);
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.rowCount(), new Manhattan());

        double error = 0;
        for (int i = 0; i < 10; i++) {
            error += i % 2 == 0 ? abs(x.get(i, 0) - x.get(5, 0)) : abs(x.get(i, 0) - x.get(7, 0));
        }
        assertEquals(error, km.computeError(x, new int[] {0, 1, 0, 1, 0, 1, 0, 1, 0, 1}, new int[] {5, 7}, cache));
    }

    @Test
    void updateNewClosestTest() {
        DMatrix x = DMatrix.copy(VarDouble.seq(21));
        KMedoids km = KMedoids.newAlternateModel(2);
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.rowCount(), new Manhattan());

        double[] dv = DoubleArrays.newFill(x.rowCount(), Double.NaN);
        double[] ev = DoubleArrays.newFill(x.rowCount(), Double.NaN);

        km.updateNewClosest(x, 0, dv, ev, cache);

        DVector exp1 = x.mapCol(0).copy().sub(x.get(0, 0)).apply(Math::abs);
        for (int i = 0; i < x.rowCount(); i++) {
            assertEquals(exp1.get(i), dv[i]);
            assertTrue(Double.isNaN(ev[i]));
        }

        km.updateNewClosest(x, 10, dv, ev, cache);
        for (int i = 0; i < x.rowCount(); i++) {
            double d = cache.get(i, 0, x.mapRow(i), x.mapRow(0));
            double e = cache.get(i, 10, x.mapRow(i), x.mapRow(10));
            if (d > e) {
                double tmp = d;
                d = e;
                e = tmp;
            }
            assertEquals(d, dv[i]);
            assertEquals(e, ev[i]);
        }

        km.updateNewClosest(x, 20, dv, ev, cache);
        for (int i = 0; i < x.rowCount(); i++) {
            double[] v = new double[3];
            v[0] = cache.get(i, 0, x.mapRow(i), x.mapRow(0));
            v[1] = cache.get(i, 10, x.mapRow(i), x.mapRow(10));
            v[2] = cache.get(i, 20, x.mapRow(i), x.mapRow(20));
            DoubleArrays.quickSort(v);
            assertEquals(v[0], dv[i]);
            assertEquals(v[1], ev[i]);
        }

    }

    @Test
    void updateAllClosestTest() {
        DMatrix x = DMatrix.copy(VarDouble.seq(21));
        KMedoids km = KMedoids.newAlternateModel(2);
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.rowCount(), new Manhattan());

        double[] dv = DoubleArrays.newFill(x.rowCount(), Double.NaN);
        double[] ev = DoubleArrays.newFill(x.rowCount(), Double.NaN);

        km.updateAllClosest(x, new int[] {0, 10, 20}, dv, ev, cache);

        for (int i = 0; i < x.rowCount(); i++) {
            double[] v = new double[3];
            v[0] = cache.get(i, 0, x.mapRow(i), x.mapRow(0));
            v[1] = cache.get(i, 10, x.mapRow(i), x.mapRow(10));
            v[2] = cache.get(i, 20, x.mapRow(i), x.mapRow(20));
            DoubleArrays.quickSort(v);
            assertEquals(v[0], dv[i]);
            assertEquals(v[1], ev[i]);
        }
    }

    @Test
    void alternateSwapTest() {

        DMatrix x = DVector.wrap(
                1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,
                9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0
        ).asMatrix();
        KMedoids km = KMedoids.newAlternateModel(2);
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.rowCount(), new Manhattan());

        int[] assign = VarInt.from(18, row -> row < 9 ? 0 : 17).elements();
        assertArrayEquals(new int[] {4, 13, 12}, km.alternateSwap(x, new int[] {0, 17, 12}, assign, cache));
    }

    @Test
    void initializePAMTest() {
        DMatrix x = DVector.wrap(
                1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,
                9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0
        ).asMatrix();
        KMedoids km = KMedoids.newAlternateModel(2);
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.rowCount(), new Manhattan());

        double[] dv = DoubleArrays.newFill(x.rowCount(), Double.NaN);
        double[] ev = DoubleArrays.newFill(x.rowCount(), Double.NaN);
        int[] centroids = km.initializePAM(x, dv, ev, cache);
        assertArrayEquals(new int[] {4, 1}, centroids);
    }

    @Test
    void peekFirstCentroidTest() {
        DMatrix x = DVector.wrap(
                1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,
                9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0
        ).asMatrix();
        KMedoids km = KMedoids.newAlternateModel(2);
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.rowCount(), new Manhattan());

        assertEquals(4, km.peekFirstCentroid(x, cache));
    }

    @Test
    void peekNextCentroidTest() {
        DMatrix x = DVector.wrap(
                1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,
                9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0
        ).asMatrix();
        KMedoids km = KMedoids.newAlternateModel(2);
        KMedoids.DistanceCache cache = new KMedoids.DistanceCache(x.rowCount(), new Manhattan());

        double[] dv = x.mapCol(0).copy().sub(x.get(4, 0)).apply(Math::abs).dVar().elements();

        assertEquals(1, km.peekNextCentroid(x, Set.of(4), dv, cache));
    }
}
