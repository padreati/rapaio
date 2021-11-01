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

package rapaio.ml.supervised.ensemble;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/14/20.
 */
public class CForestTest {

    private final Frame iris = Datasets.loadIrisDataset();

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void nameTest() {
        CForest rf1 = CForest.newModel();
        CForest rf2 = rf1.newInstance();
        assertEquals(rf1.fullName(), rf2.fullName());
    }

    @Test
    void smokeTest() {
        var model = CForest.newModel().runs.set(10);

        model.fit(iris, "class");
        var prediction = model.predict(iris);
        assertEquals(iris.rowCount(), prediction.firstClasses().size());
        assertEquals(iris.rowCount(), prediction.firstDensity().rowCount());
    }

    @Test
    void oobTest() {
        var model = CForest.newModel().runs.set(10).oob.set(true);
        var prediction = model.fit(iris, "class").predict(iris);

        var trueClass = model.getOobTrueClass();
        var predClass = model.getOobPredictedClasses();
        var densities = model.getOobDensities();

        int[] maxRows = densities.argmax(1);

        assertEquals(iris.rowCount(), densities.rowCount());
        assertEquals(model.firstTargetLevels().size() - 1, densities.colCount());

        for (int i = 0; i < iris.rowCount(); i++) {
            assertEquals(maxRows[i], predClass.getInt(i) - 1);
        }
    }

    @Test
    void viInfoTest() {
        var model = CForest.newModel().runs.set(10)
                .viFreq.set(true)
                .viGain.set(true)
                .viPerm.set(true);

        var prediction = model.fit(iris, "class").predict(iris);

        var freqInfo = model.getFreqVIInfo();
        var gainInfo = model.getGainVIInfo();
        var permInfo = model.getPermVIInfo();

        assertEquals(4, freqInfo.rowCount());
        assertEquals(4, gainInfo.rowCount());
        assertEquals(4, permInfo.rowCount());
    }

    @Test
    void printTest() {
        var model = CForest.newModel()
                .oob.set(true)
                .viFreq.set(true)
                .viGain.set(true)
                .viPerm.set(true)
                .runs.set(100);

        assertEquals("CForest", model.name());
        assertEquals("CForest{freqVI=true,gainVI=true,oob=true,rowSampler=Bootstrap(p=1),runs=100,viPerm=true}", model.fullName());

        assertEquals("CForest{freqVI=true,gainVI=true,oob=true,rowSampler=Bootstrap(p=1),runs=100,viPerm=true}; fitted:false", model.toString());
        assertEquals("""
                CForest
                =======

                Description:
                CForest{freqVI=true,gainVI=true,oob=true,rowSampler=Bootstrap(p=1),runs=100,viPerm=true}

                Capabilities:
                types inputs/targets: NOMINAL,INT,DOUBLE,BINARY/NOMINAL,BINARY
                counts inputs/targets: [1,1000000] / [1,1]
                missing inputs/targets: true/false

                Model fitted: false.
                """, model.toSummary());
        assertEquals(model.toSummary(), model.toContent());
        assertEquals(model.toSummary() + "\n", model.toFullContent());

        model.fit(iris, "class");

        assertEquals("CForest{freqVI=true,gainVI=true,oob=true,rowSampler=Bootstrap(p=1),runs=100,viPerm=true}; fitted:true, fitted trees:100", model.toString());
        assertEquals("""
                CForest
                =======

                Description:
                CForest{freqVI=true,gainVI=true,oob=true,rowSampler=Bootstrap(p=1),runs=100,viPerm=true}

                Capabilities:
                types inputs/targets: NOMINAL,INT,DOUBLE,BINARY/NOMINAL,BINARY
                counts inputs/targets: [1,1000000] / [1,1]
                missing inputs/targets: true/false

                Model fitted: true.
                Learned model:
                input vars:\s
                0. sepal-length : DOUBLE  |\s
                1.  sepal-width : DOUBLE  |\s
                2. petal-length : DOUBLE  |\s
                3.  petal-width : DOUBLE  |\s

                target vars:
                > class : NOMINAL [?,setosa,versicolor,virginica]


                Fitted trees:100
                oob enabled:true
                oob error:0.0533333
                """, model.toSummary());
        assertEquals(model.toSummary(), model.toContent());
        assertEquals("""
                CForest
                =======
                                
                Description:
                CForest{freqVI=true,gainVI=true,oob=true,rowSampler=Bootstrap(p=1),runs=100,viPerm=true}
                                
                Capabilities:
                types inputs/targets: NOMINAL,INT,DOUBLE,BINARY/NOMINAL,BINARY
                counts inputs/targets: [1,1000000] / [1,1]
                missing inputs/targets: true/false
                                
                Model fitted: true.
                Learned model:
                input vars:\s
                0. sepal-length : DOUBLE  |\s
                1.  sepal-width : DOUBLE  |\s
                2. petal-length : DOUBLE  |\s
                3.  petal-width : DOUBLE  |\s
                                
                target vars:
                > class : NOMINAL [?,setosa,versicolor,virginica]
                                
                                
                Fitted trees:100
                oob enabled:true
                oob error:0.0533333
                                
                Frequency Variable Importance:
                        name      mean      sd     scaled score\s
                [0]  petal-width 161.17 78.1405177 100         \s
                [1] petal-length 155.01 83.1465173  96.1779487 \s
                [2] sepal-length  58.05 66.9203318  36.0178693 \s
                [3]  sepal-width  17.58 26.9448295  10.9077372 \s
                                
                Gain Variable Importance:
                        name        mean        sd     scaled score\s
                [0]  petal-width 44.7711325 26.8683991 100         \s
                [1] petal-length 44.4125996 27.6972606  99.1991873 \s
                [2] sepal-length  7.3720466 12.2324536  16.4660714 \s
                [3]  sepal-width  2.0400213  3.6450592   4.556555  \s
                                
                Permutation Variable Importance:
                        name        mean        sd     scaled score\s
                [0]  petal-width 44.7711325 26.8683991 100         \s
                [1] petal-length 44.4125996 27.6972606  99.1991873 \s
                [2] sepal-length  7.3720466 12.2324536  16.4660714 \s
                [3]  sepal-width  2.0400213  3.6450592   4.556555  \s
                                
                """, model.toFullContent());
    }
}
