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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BiFunction;

import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.datasets.Datasets;
import rapaio.math.tensor.Tensor;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/1/20.
 */
public class KMClusterTest {

    @Test
    void irisTest() {

        var df = Datasets.loadOldFaithful();

        VarInt ks = VarInt.seq(1, 10).name("k");
        VarDouble inertia = VarDouble.empty().name("inertia");

        for (int k : ks) {
            KMCluster model = KMCluster.newKMeans()
                    .k.set(k)
                    .nstart.set(100)
                    .init.set(KMClusterInit.PlusPlus)
                    .runs.set(100)
                    .seed.set(42L);
            inertia.addDouble(model.fit(df).getError());
        }

        // we know about this data set that there are two natural clusters
        // thus we should get the biggest discrete differential for 2 clusters

        double max = inertia.getDouble(0) - inertia.getDouble(1);
        int index = 2;
        for (int i = 2; i < 9; i++) {
            if (max < inertia.getDouble(i - 1) - inertia.getDouble(i)) {
                max = inertia.getDouble(i - 1) - inertia.getDouble(i);
                index = i + 1;
            }
        }

        assertEquals(2, index);
    }

    @Test
    void testKMeansDegenerate() {
        Frame df = SolidFrame.byVars(VarDouble.wrap(1, 1, 1, 1, 1, 0, 0, 0, 0, 0).name("x"));
        KMCluster clustering = KMCluster.newKMeans().k.set(2).nstart.set(100).seed.set(42L)
                .fit(df);

        Frame c = clustering.getCentroids().refSort("x");
        assertTrue(c.deepEquals(SolidFrame.byVars(VarDouble.copy(0, 1).name("x"))));
        Tensor<Double> cc = clustering.getCentroidsMatrix();
        assertEquals(0, cc.takesq(1, 0).prod());
        assertEquals(1, cc.takesq(1, 0).sum());

        assertEquals(2, clustering.getErrors().size());
        assertEquals(0, clustering.getError());
    }

    @Test
    void testKMedoidsDegenerate() {
        Frame df = SolidFrame.byVars(VarDouble.wrap(1, 1, 1, 1, 1, 0, 0, 0, 0, 0).name("x"));
        KMCluster clustering = KMCluster.newKMedians().k.set(2).nstart.set(100).seed.set(42L)
                .fit(df);

        Frame c = clustering.getCentroids().refSort("x");
        assertTrue(c.deepEquals(SolidFrame.byVars(VarDouble.copy(0, 1).name("x"))));
        Tensor<Double> cc = clustering.getCentroidsMatrix();
        assertEquals(0, cc.takesq(1, 0).prod());
        assertEquals(1, cc.takesq(1, 0).sum());

        assertEquals(2, clustering.getErrors().size());
        assertEquals(0, clustering.getError());
    }

    @Test
    void printingTest() {
        var df = Datasets.loadOldFaithful();
        KMCluster model = KMCluster.newKMeans()
                .k.set(2)
                .nstart.set(100)
                .init.set(KMClusterInit.PlusPlus)
                .runs.set(100)
                .seed.set(42L);

        assertEquals("KMCluster{init=PlusPlus,k=2,method=KMeans,nstart=100,runs=100,seed=42}, fitted=false", model.toString());
        assertEquals("KMCluster{init=PlusPlus,k=2,method=KMeans,nstart=100,runs=100,seed=42}", model.fullName());
        assertEquals("""
                KMCluster{init=PlusPlus,k=2,method=KMeans,nstart=100,runs=100,seed=42}
                Model fitted=false
                """, model.toSummary());
        assertEquals(model.toContent(), model.toSummary());
        assertEquals(model.toFullContent(), model.toSummary());

        model.fit(df);

        assertEquals("KMCluster{init=PlusPlus,k=2,method=KMeans,nstart=100,runs=100,seed=42}, fitted=true", model.toString());
        assertEquals("KMCluster{init=PlusPlus,k=2,method=KMeans,nstart=100,runs=100,seed=42}", model.fullName());
        assertEquals("""
                KMCluster{init=PlusPlus,k=2,method=KMeans,nstart=100,runs=100,seed=42}
                Model fitted=true
                Inertia:8901.290870880382
                Iterations:9
                Learned clusters:2
                """, model.toSummary());
        assertEquals(model.toContent(), model.toSummary());
        assertEquals("""
                KMCluster{init=PlusPlus,k=2,method=KMeans,nstart=100,runs=100,seed=42}
                Model fitted=true
                Inertia:8901.290870880382
                Iterations:9
                Learned clusters:2
                Centroids:
                    eruptions  waiting  \s
                [0] 4.2948012 80.2865497\s
                [1] 2.0964646 54.7474747\s
                """, model.toFullContent());

    }

    @Test
    void MethodsTest() {
        var df = Datasets.loadOldFaithful();
        KMCluster kmeans = KMCluster.newKMeans().k.set(2).seed.set(42L);
        KMCluster kmedians = KMCluster.newKMedians().k.set(2).seed.set(42L);

        kmeans.fit(df);
        kmedians.fit(df);

        VarInt kmeansAssignment = kmeans.predict(df).assignment();
        VarInt kmediansAssignment = kmedians.predict(df).assignment();

        var kmeansC = kmeans.getCentroidsMatrix();
        var kmediansC = kmedians.getCentroidsMatrix();

        Tensor<Double> instances = df.tensor();

        BiFunction<Tensor<Double>, Tensor<Double>, Double> dist = (u, v) -> u.copy().sub_(v).apply_(x -> x * x).sum();

        double kmeansErr = 0.0;
        double kmediansErr = 0.0;

        for (int i = 0; i < df.rowCount(); i++) {
            kmeansErr += dist.apply(instances.takesq(0, i), kmeansC.takesq(0, kmeansAssignment.getInt(i)));
            kmediansErr += dist.apply(instances.takesq(0, i), kmediansC.takesq(0, kmediansAssignment.getInt(i)));
        }
        // the errors are a little bit different, under one percent
        assertTrue(Math.abs(kmeansErr - kmediansErr) / kmediansErr < 0.01);


        double intersection = 0;
        for (int i = 0; i < df.rowCount(); i++) {
            if (kmeansAssignment.getInt(i) == kmediansAssignment.getInt(i)) {
                intersection++;
            }
        }
        // for k=2 we have the same assignment
        assertEquals(1.0, intersection / df.rowCount());

        // still centroids are not the same
        assertFalse(kmeansC.deepEquals(kmediansC));

        // on the other hand the centroids are close, small normed distance
        for (int i = 0; i < 2; i++) {
            double cdist = dist.apply(kmeansC.takesq(0, i), kmediansC.takesq(0, i));
            cdist /= Math.sqrt(kmeansC.takesq(0, i).norm(2.));
            cdist /= Math.sqrt(kmediansC.takesq(0, i).norm(2.));
            assertTrue(cdist < 0.02);
        }
    }

    @Test
    void predictTest() {
        var km = KMCluster.newKMeans().k.set(2).seed.set(42L);
        var df = Datasets.loadOldFaithful();

        km.fit(df);

        var assignment1 = km.predict(df).assignment();
        var assignment2 = km.newInstance().fit(df).predict(df).assignment();

        assertTrue(assignment1.deepEquals(assignment2));

        var result = km.predict(df);

        assertEquals("KMClusterResult{}", result.toString());
        assertEquals("""
                Overall errors:\s
                > count: 272
                > mean: 32.7273288
                > var: 1,621.0637918
                > sd: 40.2624365
                > inertia/error:8,890.7628278
                > iterations:10
                                
                Per cluster:\s
                    ID count    mean         var      var/total     sd    \s
                [0]  2   172 31.6607834 1,760.3732065 1.085937  41.9568017\s
                [1]  1   100 34.5617867 1,391.437297  0.8583483 37.3019744\s
                """, result.toSummary());

        assertEquals(result.toSummary(), result.toContent());
        assertEquals(result.toSummary(), result.toFullContent());
    }
}
