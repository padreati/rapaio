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

package rapaio.ml.clustering.kmeans;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.datasets.Datasets;
import rapaio.math.linear.DMatrix;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/1/20.
 */
public class KMeansTest {

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
            KMeans model = KMeans.newModel()
                    .k.set(k)
                    .nstart.set(100)
                    .init.set(KMeansInit.PlusPlus)
                    .runs.set(100);
            inertia.addDouble(model.fit(df).getInertia());
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
    void testDegenerate() {
        Frame df = SolidFrame.byVars(VarDouble.wrap(1, 1, 1, 1, 1, 0, 0, 0, 0, 0).name("x"));
        KMeans clustering = KMeans.newModel().k.set(2).nstart.set(100).fit(df);
        KMeansResult result = clustering.predict(df);

        Frame c = clustering.getCentroids().refSort("x");
        assertTrue(c.deepEquals(SolidFrame.byVars(VarDouble.copy(0, 1).name("x"))));
        DMatrix cc = clustering.getCentroidsMatrix();
        assertEquals(0, cc.mapCol(0).prod());
        assertEquals(1, cc.mapCol(0).sum());

        assertEquals(3, clustering.getErrors().size());
        assertEquals(0, clustering.getInertia());

    }

    @Test
    void printingTest() {
        var df = Datasets.loadOldFaithful();
        KMeans model = KMeans.newModel()
                .k.set(2)
                .nstart.set(100)
                .init.set(KMeansInit.PlusPlus)
                .runs.set(100);

        assertEquals("KMeans{init=PlusPlus,nstart=100,runs=100}, fitted=false", model.toString());
        assertEquals("KMeans{init=PlusPlus,nstart=100,runs=100}", model.fullName());
        assertEquals("""
                KMeans{init=PlusPlus,nstart=100,runs=100}
                Model fitted=false
                """, model.toSummary());
        assertEquals(model.toContent(), model.toSummary());
        assertEquals(model.toFullContent(), model.toSummary());

        model.fit(df);

        assertEquals("KMeans{init=PlusPlus,nstart=100,runs=100}, fitted=true", model.toString());
        assertEquals("KMeans{init=PlusPlus,nstart=100,runs=100}", model.fullName());
        assertEquals("""
                KMeans{init=PlusPlus,nstart=100,runs=100}
                Model fitted=true
                Inertia:8901.768720947213
                Iterations:3
                Learned clusters:2
                """, model.toSummary());
        assertEquals(model.toContent(), model.toSummary());
        assertEquals("""
                KMeans{init=PlusPlus,nstart=100,runs=100}
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
}
