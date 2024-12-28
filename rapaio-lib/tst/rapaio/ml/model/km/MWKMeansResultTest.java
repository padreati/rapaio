/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.ml.model.km;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.data.transform.Jitter;
import rapaio.datasets.Datasets;

public class MWKMeansResultTest {

    @Test
    void printTest() {
        Frame iris = Datasets.loadIrisDataset().mapVars(VarRange.onlyTypes(VarType.DOUBLE));
        iris = iris.fapply(Jitter.on(new Random(42), 1, VarRange.all()));

        int p = 3;
        MWKMeans model = MWKMeans.newMWKMeans()
                .subspace.set(true)
                .k.set(3)
                .p.set(p)
                .nstart.set(3)
                .seed.set(42L);
        var result = model.fit(iris).predict(iris);

        assertEquals("MWKMeansResult{}", result.toString());
        assertEquals("""
                Overall errors:\s
                > count: 150
                > mean: 0.1786952
                > var: 0.0169075
                > sd: 0.130029
                > inertia/error: 13.4395791
                > iterations: 10
                                
                Per cluster:\s
                    ID count   mean       var    var/total    sd    \s
                [0]  2    69 0.2152954 0.0212316 1.2557452 0.1457106\s
                [1]  1    42 0.1520472 0.0132631 0.7844516 0.1151657\s
                [2]  3    39 0.1426389 0.0094403 0.558347  0.0971611\s
                """, result.toSummary());
        assertEquals("""
                Overall errors:\s
                > count: 150
                > mean: 0.1786952
                > var: 0.0169075
                > sd: 0.130029
                > inertia/error: 13.4395791
                > iterations: 10
                                
                Per cluster:\s
                    ID count   mean       var    var/total    sd    \s
                [0]  2    69 0.2152954 0.0212316 1.2557452 0.1457106\s
                [1]  1    42 0.1520472 0.0132631 0.7844516 0.1151657\s
                [2]  3    39 0.1426389 0.0094403 0.558347  0.0971611\s
                """, result.toContent());
        assertEquals("""
                Overall errors:\s
                > count: 150
                > mean: 0.1786952
                > var: 0.0169075
                > sd: 0.130029
                > inertia/error: 13.4395791
                > iterations: 10
                                
                Per cluster:\s
                    ID count   mean       var    var/total    sd    \s
                [0]  2    69 0.2152954 0.0212316 1.2557452 0.1457106\s
                [1]  1    42 0.1520472 0.0132631 0.7844516 0.1151657\s
                [2]  3    39 0.1426389 0.0094403 0.558347  0.0971611\s
                """, result.toFullContent());
    }
}
