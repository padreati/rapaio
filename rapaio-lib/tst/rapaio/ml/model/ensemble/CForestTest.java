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

package rapaio.ml.model.ensemble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarType;
import rapaio.datasets.Datasets;
import rapaio.io.Csv;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/14/20.
 */
public class CForestTest {

    private final Frame iris = Datasets.loadIrisDataset();
    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(123);
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

        var trueClass = model.oobTrueClass();
        var predClass = model.oobPredictedClasses();
        var densities = model.oobDensities();

        assertEquals(iris.rowCount(), densities.dim(0));
        assertEquals(model.firstTargetLevels().size(), densities.dim(1));

        for (int i = 0; i < iris.rowCount(); i++) {
            assertEquals(densities.selsq(0, i).argmax(), predClass.getInt(i));
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
                .runs.set(100)
                .seed.set(123L);

        assertEquals("CForest", model.name());
        assertEquals("CForest{freqVI=true,gainVI=true,oob=true,rowSampler=Bootstrap(p=1),seed=123,viPerm=true}", model.fullName());

        assertEquals("CForest{freqVI=true,gainVI=true,oob=true,rowSampler=Bootstrap(p=1),seed=123,viPerm=true}; fitted:false",
                model.toString());
        assertEquals("""
                CForest
                =======

                Description:
                CForest{freqVI=true,gainVI=true,oob=true,rowSampler=Bootstrap(p=1),seed=123,viPerm=true}

                Capabilities:
                types inputs/targets: NOMINAL,INT,DOUBLE,BINARY/NOMINAL,BINARY
                counts inputs/targets: [1,1000000] / [1,1]
                missing inputs/targets: true/false

                Model fitted: false.
                """, model.toSummary());
        assertEquals(model.toSummary(), model.toContent());
        assertEquals(model.toSummary() + "\n", model.toFullContent());

        model.fit(iris, "class");

        assertEquals(
                "CForest{freqVI=true,gainVI=true,oob=true,rowSampler=Bootstrap(p=1),seed=123,viPerm=true}; fitted:true, fitted trees:100",
                model.toString());
        assertEquals("""
                CForest
                =======
                                
                Description:
                CForest{freqVI=true,gainVI=true,oob=true,rowSampler=Bootstrap(p=1),seed=123,viPerm=true}
                                
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
                > class : NOMINAL [setosa,versicolor,virginica]
                                
                                
                Fitted trees:100
                oob enabled:true
                oob error:0.0466667
                """, model.toSummary());
        assertEquals(model.toSummary(), model.toContent());
        assertEquals("""
                CForest
                =======
                                
                Description:
                CForest{freqVI=true,gainVI=true,oob=true,rowSampler=Bootstrap(p=1),seed=123,viPerm=true}
                                
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
                > class : NOMINAL [setosa,versicolor,virginica]
                                
                                
                Fitted trees:100
                oob enabled:true
                oob error:0.0466667
                                
                Frequency Variable Importance:
                        name      mean      sd     scaled score\s
                [0]  petal-width 160.61 77.5364229 100         \s
                [1] petal-length 151.65 76.6744891  94.4212689 \s
                [2] sepal-length  66.9  68.4402711  41.6536953 \s
                [3]  sepal-width  23.89 29.5213995  14.8745408 \s
                                
                Gain Variable Importance:
                        name        mean        sd     scaled score\s
                [0]  petal-width 43.6611601 27.5449726 100         \s
                [1] petal-length 42.5856186 27.7044274  97.5366172 \s
                [2] sepal-length  9.2340389 13.4677241  21.149321  \s
                [3]  sepal-width  2.7973776  4.8615338   6.4070161 \s
                                
                Permutation Variable Importance:
                        name        mean        sd     scaled score\s
                [0]  petal-width 43.6611601 27.5449726 100         \s
                [1] petal-length 42.5856186 27.7044274  97.5366172 \s
                [2] sepal-length  9.2340389 13.4677241  21.149321  \s
                [3]  sepal-width  2.7973776  4.8615338   6.4070161 \s
                                
                """, model.toFullContent());
    }


    @Test
    void testParallelism() {
        var iris = Datasets.loadIrisDataset();
        String target = "class";
        for (int i = 0; i < 10; i++) {
            var rf = CForest.newModel()
                    .runs.set(100)
                    .poolSize.set(3)
                    .seed.set(123L);
            rf.fit(iris, target);
        }
    }

    static final String header = "id,x,y,z,target";
    static final List<String> rows = List.of(
            "0,a,a,a,0",
            "1,b,b,b,1",
            "2,a,b,a,0",
            "3,b,a,a,1",
            "4,c,a,b,0",
            "5,c,c,c,1",
            "6,b,b,a,1",
            "7,c,a,b,0",
            "8,c,a,c,0",
            "9,b,c,a,1",
            "10,b,c,c,0");

    @Test
    void nominalSensitivityTest() throws IOException {

        for (int times = 0; times < 100; times++) {

            Frame df = Csv.instance().varTypes.add(VarType.NOMINAL, "target").read(new ByteArrayInputStream(data(times)));

            var model = CForest.newModel().runs.set(3);
            var result = model.fit(df.removeVars("id"), "target").predict(df).firstClasses();
            var refFrame = SolidFrame.byVars(df.rvar("id"), result.name("class"));

            for (int i = 0; i < 200; i++) {
                Frame test = Csv.instance()
                        .varTypes.add(VarType.NOMINAL, "target")
                        .read(new ByteArrayInputStream(data(times + 1 + i)));
                var testClasses = model.predict(test).firstClasses();
                var testFrame = SolidFrame.byVars(test.rvar("id"), testClasses.name("class"));
                double score = compare(refFrame, testFrame);
                assertEquals(1.0, score, 1e-16);
            }
        }
    }

    private double compare(SolidFrame refFrame, SolidFrame testFrame) {
        Map<Integer, String> refLabels = new HashMap<>();
        for (int i = 0; i < refFrame.rowCount(); i++) {
            refLabels.put(refFrame.getInt(i, "id"), refFrame.getLabel(i, "class"));
        }
        double score = 0;
        for (int i = 0; i < testFrame.rowCount(); i++) {
            String refLabel = refLabels.get(testFrame.getInt(i, "id"));
            if (refLabel.equals(testFrame.getLabel(i, "class"))) {
                score++;
            }
        }
        return score / refFrame.rowCount();
    }

    private byte[] data(int seed) {
        var myrows = new ArrayList<>(rows);
        Collections.shuffle(myrows, new Random(seed));

        StringBuilder sb = new StringBuilder();
        sb.append(header).append("\n");
        for (String line : myrows) {
            sb.append(line).append("\n");
        }
        return sb.toString().getBytes();
    }

    @Test
    void multithreadedConsistency() {
        var df = Datasets.loadIrisDataset();

        CForest rf1 = CForest.newModel()
                .poolSize.set(2)
                .runs.set(10)
                .seed.set(123L);

        var density1 = rf1.fit(df, "class").predict(df).firstDensity();

        CForest rf2 = CForest.newModel()
                .poolSize.set(2)
                .runs.set(10)
                .seed.set(123L);
        var density2 = rf2.fit(df, "class").predict(df).firstDensity();

        assertTrue(density1.deepEquals(density2));
    }
}
