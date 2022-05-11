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

package rapaio.ml.model.km;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BiFunction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.datasets.Datasets;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/1/20.
 */
public class KMClusterTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(42);
    }

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
                    .runs.set(100);
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
        KMCluster clustering = KMCluster.newKMeans().k.set(2).nstart.set(100).fit(df);

        Frame c = clustering.getCentroids().refSort("x");
        assertTrue(c.deepEquals(SolidFrame.byVars(VarDouble.copy(0, 1).name("x"))));
        DMatrix cc = clustering.getCentroidsMatrix();
        assertEquals(0, cc.mapCol(0).prod());
        assertEquals(1, cc.mapCol(0).sum());

        assertEquals(2, clustering.getErrors().size());
        assertEquals(0, clustering.getError());
    }

    @Test
    void testKMedoidsDegenerate() {
        Frame df = SolidFrame.byVars(VarDouble.wrap(1, 1, 1, 1, 1, 0, 0, 0, 0, 0).name("x"));
        KMCluster clustering = KMCluster.newKMedians().k.set(2).nstart.set(100).fit(df);

        Frame c = clustering.getCentroids().refSort("x");
        assertTrue(c.deepEquals(SolidFrame.byVars(VarDouble.copy(0, 1).name("x"))));
        DMatrix cc = clustering.getCentroidsMatrix();
        assertEquals(0, cc.mapCol(0).prod());
        assertEquals(1, cc.mapCol(0).sum());

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
                .runs.set(100);

        assertEquals("KMCluster{init=PlusPlus,k=2,method=KMeans,nstart=100,runs=100}, fitted=false", model.toString());
        assertEquals("KMCluster{init=PlusPlus,k=2,method=KMeans,nstart=100,runs=100}", model.fullName());
        assertEquals("""
                KMCluster{init=PlusPlus,k=2,method=KMeans,nstart=100,runs=100}
                Model fitted=false
                """, model.toSummary());
        assertEquals(model.toContent(), model.toSummary());
        assertEquals(model.toFullContent(), model.toSummary());

        model.fit(df);

        assertEquals("KMCluster{init=PlusPlus,k=2,method=KMeans,nstart=100,runs=100}, fitted=true", model.toString());
        assertEquals("KMCluster{init=PlusPlus,k=2,method=KMeans,nstart=100,runs=100}", model.fullName());
        assertEquals("""
                KMCluster{init=PlusPlus,k=2,method=KMeans,nstart=100,runs=100}
                Model fitted=true
                Inertia:8901.768720947213
                Iterations:3
                Learned clusters:2
                """, model.toSummary());
        assertEquals(model.toContent(), model.toSummary());
        assertEquals("""
                KMCluster{init=PlusPlus,k=2,method=KMeans,nstart=100,runs=100}
                Model fitted=true
                Inertia:8901.768720947213
                Iterations:3
                Learned clusters:2
                Centroids:
                    eruptions  waiting  \s
                [0] 4.2979302 80.2848837\s
                [1] 2.09433   54.75     \s
                """, model.toFullContent());

    }

    @Test
    void MethodsTest() {
        var df = Datasets.loadOldFaithful();
        KMCluster kmeans = KMCluster.newKMeans().k.set(2);
        KMCluster kmedians = KMCluster.newKMedians().k.set(2);

        kmeans.fit(df);
        kmedians.fit(df);

        VarInt kmeansAssignment = kmeans.predict(df).assignment();
        VarInt kmediansAssignment = kmedians.predict(df).assignment();

        DMatrix kmeansC = kmeans.getCentroidsMatrix();
        DMatrix kmediansC = kmedians.getCentroidsMatrix();

        DMatrix instances = DMatrix.copy(df);

        BiFunction<DVector, DVector, Double> dist = (u, v) -> u.copy().sub(v).apply(x -> x * x).sum();

        double kmeansErr = 0.0;
        double kmediansErr = 0.0;

        for (int i = 0; i < df.rowCount(); i++) {
            kmeansErr += dist.apply(instances.mapRow(i), kmeansC.mapRow(kmeansAssignment.getInt(i)));
            kmediansErr += dist.apply(instances.mapRow(i), kmediansC.mapRow(kmediansAssignment.getInt(i)));
        }
        // the errors are a little bit different, under one percent
        assertTrue(Math.abs(kmeansErr-kmediansErr)/kmediansErr < 0.01);


        double intersection = 0;
        for (int i = 0; i < df.rowCount(); i++) {
            if(kmeansAssignment.getInt(i)==kmediansAssignment.getInt(i)) {
                intersection++;
            }
        }
        // for k=2 we have the same assignment
        assertEquals(1.0, intersection/df.rowCount());

        // still centroids are not the same
        assertFalse(kmeansC.deepEquals(kmediansC));

        // on the other hand the centroids are close, small normed distance
        for (int i = 0; i < 2; i++) {
            double cdist = dist.apply(kmeansC.mapRow(i), kmediansC.mapRow(i));
            cdist /= Math.sqrt(kmeansC.mapRow(i).norm(2));
            cdist /= Math.sqrt(kmediansC.mapRow(i).norm(2));
            assertTrue(cdist < 0.02);
        }
    }

    @Test
    void predictTest() {
        var km = KMCluster.newKMeans().k.set(2);
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
                > mean: 32.7270909
                > var: 1,622.7494621
                > sd: 40.2833646
                > inertia/error:8,901.7687209
                > iterations:4
                                
                Per cluster:\s
                    ID count    mean         var      var/total     sd    \s
                [0]  2   172 31.6604119 1,763.2348144 1.0865724 41.9908897\s
                [1]  1   100 34.5617787 1,391.1074822 0.8572534 37.2975533\s
                """, result.toSummary());

        assertEquals(result.toSummary(), result.toContent());
        assertEquals(result.toSummary(), result.toFullContent());
    }
}
