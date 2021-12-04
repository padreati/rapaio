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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.data.filter.FJitter;
import rapaio.datasets.Datasets;

public class MWKMeansResultTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(42);
    }

    @Test
    void printTest() {
        Frame iris = Datasets.loadIrisDataset().mapVars(VarRange.onlyTypes(VarType.DOUBLE));
        iris = iris.fapply(FJitter.on(1, VarRange.all()));

        double p = 3;
        MWKMeans model = MWKMeans.newMWKMeans()
                .subspace.set(true)
                .k.set(3)
                .p.set(p)
                .nstart.set(3);
        var result = model.fit(iris).predict(iris);

        assertEquals("MWKMeansResult{}", result.toString());
        assertEquals("""
                Overall errors:\s
                > count: 150
                > mean: 0.1433928
                > var: 0.0103899
                > sd: 0.1019307
                > inertia/error: 9.6013022
                > iterations: 11
                                
                Per cluster:\s
                    ID count   mean       var    var/total    sd    \s
                [0]  1    61 0.1390899 0.0113295 1.0904405 0.1064402\s
                [1]  3    53 0.1614438 0.0121037 1.164952  0.1100168\s
                [2]  2    36 0.1241089 0.0059183 0.5696208 0.0769304\s
                """, result.toSummary());
        assertEquals("""
                Overall errors:\s
                > count: 150
                > mean: 0.1433928
                > var: 0.0103899
                > sd: 0.1019307
                > inertia/error: 9.6013022
                > iterations: 11
                                
                Per cluster:\s
                    ID count   mean       var    var/total    sd    \s
                [0]  1    61 0.1390899 0.0113295 1.0904405 0.1064402\s
                [1]  3    53 0.1614438 0.0121037 1.164952  0.1100168\s
                [2]  2    36 0.1241089 0.0059183 0.5696208 0.0769304\s
                """, result.toContent());
        assertEquals("""
                Overall errors:\s
                > count: 150
                > mean: 0.1433928
                > var: 0.0103899
                > sd: 0.1019307
                > inertia/error: 9.6013022
                > iterations: 11
                                
                Per cluster:\s
                    ID count   mean       var    var/total    sd    \s
                [0]  1    61 0.1390899 0.0113295 1.0904405 0.1064402\s
                [1]  3    53 0.1614438 0.0121037 1.164952  0.1100168\s
                [2]  2    36 0.1241089 0.0059183 0.5696208 0.0769304\s
                """, result.toFullContent());
    }
}
