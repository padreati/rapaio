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

package rapaio.ml.model.linear;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarType;
import rapaio.datasets.Datasets;
import rapaio.ml.common.Capabilities;
import rapaio.ml.eval.metric.Confusion;
import rapaio.ml.model.ClassifierResult;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/22/20.
 */
public class BinaryLogisticTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void testBuilders() {
        BinaryLogistic defaultModel = BinaryLogistic.newModel();

        assertEquals(1e-10, defaultModel.eps.get());
        assertEquals(1.0, defaultModel.intercept.get());
        assertEquals(BinaryLogistic.Initialize.ZERO, defaultModel.init.get());
        assertEquals("", defaultModel.nominalLevel.get());
        assertEquals(0.0, defaultModel.l1penalty.get());
        assertEquals(0.0, defaultModel.l2penalty.get());
        assertEquals(BinaryLogistic.Method.IRLS, defaultModel.solver.get());

        BinaryLogistic customModel = BinaryLogistic.newModel()
                .eps.set(1e-5)
                .init.set(BinaryLogistic.Initialize.ZERO)
                .intercept.set(0.0)
                .nominalLevel.set("test")
                .l1penalty.set(2.0)
                .l2penalty.set(3.0)
                .solver.set(BinaryLogistic.Method.NEWTON);

        assertEquals(1e-5, customModel.eps.get());
        assertEquals(0.0, customModel.intercept.get());
        assertEquals(BinaryLogistic.Initialize.ZERO, customModel.init.get());
        assertEquals("test", customModel.nominalLevel.get());
        assertEquals(2.0, customModel.l1penalty.get());
        assertEquals(3.0, customModel.l2penalty.get());
        assertEquals(BinaryLogistic.Method.NEWTON, customModel.solver.get());

        BinaryLogistic customCopyModel = customModel.newInstance();

        assertEquals(1e-5, customCopyModel.eps.get());
        assertEquals(0.0, customCopyModel.intercept.get());
        assertEquals(BinaryLogistic.Initialize.ZERO, customCopyModel.init.get());
        assertEquals("test", customCopyModel.nominalLevel.get());
        assertEquals(2.0, customCopyModel.l1penalty.get());
        assertEquals(3.0, customCopyModel.l2penalty.get());
        assertEquals(BinaryLogistic.Method.NEWTON, customCopyModel.solver.get());

        assertEquals("BinaryLogistic", customCopyModel.name());
        assertEquals("BinaryLogistic{eps=0.00001,intercept=0,l1penalty=2,l2penalty=3," +
                "nominalLevel=test,solver=NEWTON}", customCopyModel.fullName());
    }

    @Test
    void testCapabilities() {
        Capabilities capabilities = BinaryLogistic.newModel().capabilities();
        assertArrayEquals(new VarType[] {VarType.BINARY, VarType.INT, VarType.DOUBLE}, capabilities.inputTypes());
        assertEquals(1, capabilities.minInputCount());
        assertEquals(10000, capabilities.maxInputCount());
        assertFalse(capabilities.allowMissingInputValues());

        assertArrayEquals(new VarType[] {VarType.NOMINAL, VarType.BINARY}, capabilities.targetTypes());
        assertEquals(1, capabilities.minTargetCount());
        assertEquals(1, capabilities.maxTargetCount());
        assertFalse(capabilities.allowMissingTargetValues());
    }

    @Test
    void testSymmetricAroundZeroSeparable() {
        var model1 = BinaryLogistic.newModel()
                .runs.set(100)
                .l2penalty.set(100.0)
                .eps.set(0.000001);

        VarDouble x = VarDouble.copy(-5, -4, -3, -2, -1, 1, 2, 3, 4, 5).name("x");
        VarNominal y = VarNominal.copy("1", "1", "1", "1", "1", "0", "0", "0", "0", "0").name("y");
        Frame df = SolidFrame.byVars(x, y);

        var result1 = model1.fit(df, "y").predict(df);
        assertTrue(model1.isConverged());
        assertTrue(result1.firstClasses().deepEquals(y));

        var model2 = model1.newInstance().solver.set(BinaryLogistic.Method.NEWTON);
        var result2 = model2.fit(df, "y").predict(df);
        assertFalse(model2.isConverged());
        assertTrue(result2.firstClasses().deepEquals(y));
    }

    @Test
    void testIris() {
        Frame iris = Datasets.loadIrisDataset()
                .stream().filter(s -> !s.getLabel("class").equals("virginica")).toMappedFrame();
        VarNominal clazz = VarNominal.from(iris.rowCount(), row -> iris.rvar("class").getLabel(row)).name("clazz");
        Frame df = iris.removeVars("petal-length", "sepal-length", "class").bindVars(clazz).copy();

        Normal normal = Normal.of(0, 0.5);
        df.rvar(0).dv().apply(v -> v + normal.sampleNext());
        df.rvar(1).dv().apply(v -> v + normal.sampleNext());

        var result = BinaryLogistic.newModel()
                .solver.set(BinaryLogistic.Method.IRLS)
                .runs.set(100).fit(df, "clazz").predict(df, true, true);
        assertTrue(Confusion.from(clazz, result.firstClasses()).accuracy() > 0.8);
    }

    @Test
    void singleInputTest() {
        int n = 20;

        VarDouble x = VarDouble.empty(2 * n).name("x");
        VarDouble.sample(Normal.of(0, 0.5), random, n).dv().addTo(x.dv().range(0, n), 0);
        VarDouble.sample(Normal.of(0.75, 0.5), random, n).dv().addTo(x.dv().range(n, 2 * n), 0);

        VarNominal y = VarNominal.from(2 * n, i -> i < n ? "1" : "0").name("y");

        BinaryLogistic irls = BinaryLogistic.newModel()
                .solver.set(BinaryLogistic.Method.IRLS)
                .l2penalty.set(0.0)
                .runs.set(1000);
        irls.fit(SolidFrame.byVars(x, y), "y");
        ClassifierResult result = irls.predict(SolidFrame.byVars(x));

        assertTrue(Confusion.from(y, result.firstClasses()).accuracy() > 0.8);
    }

    @Test
    void testPrinter() {
        Frame iris = Datasets.loadIrisDataset()
                .stream().filter(s -> !s.getLabel("class").equals("virginica")).toMappedFrame();
        VarNominal clazz = VarNominal.from(iris.rowCount(), row -> iris.rvar("class").getLabel(row)).name("clazz");
        Frame df = iris.removeVars("petal-length", "sepal-length", "class").bindVars(clazz).copy();

        Normal normal = Normal.of(0, 0.5);
        df.rvar(0).dv().apply(v -> v + normal.sampleNext(random));
        df.rvar(1).dv().apply(v -> v + normal.sampleNext(random));


        var irls = BinaryLogistic.newModel()
                .solver.set(BinaryLogistic.Method.IRLS)
                .runs.set(100);

        assertEquals("BinaryLogistic{}, hasLearned=false", irls.toString());

        irls.fit(df, "clazz");

        assertEquals("BinaryLogistic{}, hasLearned=true, converged=true, iterations=7", irls.toString());
        assertEquals("""
                BinaryLogistic model
                ================
                                
                Description:
                BinaryLogistic{}
                                
                Capabilities:
                types inputs/targets: BINARY,INT,DOUBLE/NOMINAL,BINARY
                counts inputs/targets: [1,10000] / [1,1]
                missing inputs/targets: false/false
                                
                input vars:\s
                0. sepal-width : DOUBLE  |\s
                1. petal-width : DOUBLE  |\s
                                
                target vars:
                > clazz : NOMINAL [?,setosa,versicolor]
                                
                Learning data:
                > has learned: true
                > has intercept: true
                > intercept factor: 1
                > coefficients:
                  intercept -6.1485327\s
                sepal-width  3.2716831\s
                petal-width -4.8553961\s
                                      \s
                > converged: true
                > iterations: 7
                """, irls.toSummary());

        assertEquals(irls.toSummary(), irls.toContent());
        assertEquals(irls.toSummary(), irls.toFullContent());

        var newton = BinaryLogistic.newModel()
                .solver.set(BinaryLogistic.Method.NEWTON)
                .runs.set(100);

        assertEquals("BinaryLogistic{solver=NEWTON}, hasLearned=false", newton.toString());

        newton.fit(df, "clazz");

        assertEquals("BinaryLogistic{solver=NEWTON}, hasLearned=true, converged=true, iterations=7", newton.toString());
        assertEquals("""
                BinaryLogistic model
                ================
                                
                Description:
                BinaryLogistic{solver=NEWTON}
                                
                Capabilities:
                types inputs/targets: BINARY,INT,DOUBLE/NOMINAL,BINARY
                counts inputs/targets: [1,10000] / [1,1]
                missing inputs/targets: false/false
                                
                input vars:\s
                0. sepal-width : DOUBLE  |\s
                1. petal-width : DOUBLE  |\s
                                
                target vars:
                > clazz : NOMINAL [?,setosa,versicolor]
                                
                Learning data:
                > has learned: true
                > has intercept: true
                > intercept factor: 1
                > coefficients:
                  intercept -6.1485327\s
                sepal-width  3.2716831\s
                petal-width -4.8553961\s
                                      \s
                > converged: true
                > iterations: 7
                """, newton.toSummary());

        assertEquals(newton.toSummary(), newton.toContent());
        assertEquals(newton.toSummary(), newton.toFullContent());
    }

    @Test
    void testDifferentTargetTypes() {

        int n = 100;
        VarDouble x = VarDouble.empty(2 * n).name("x");
        VarDouble.sample(Normal.of(0, 1), n).dv().addTo(x.dv().range(0, n), 0);
        VarDouble.sample(Normal.of(2, 1), n).dv().addTo(x.dv().range(n, 2 * n), 0);

        VarNominal ynom = VarNominal.from(2 * n, i -> i < n ? "1" : "2").name("y");
        VarBinary ybin = VarBinary.from(2 * n, i -> i < n).name("y");

        var resultNom = BinaryLogistic.newModel().fit(SolidFrame.byVars(x, ynom), "y")
                .predict(SolidFrame.byVars(x));
        var resultBin = BinaryLogistic.newModel().fit(SolidFrame.byVars(x, ybin), "y")
                .predict(SolidFrame.byVars(x));

        var predNom = resultNom.firstClasses();
        var predBin = resultBin.firstClasses();

        assertTrue(predNom.dv().apply(v -> v == 2 ? 0 : 1).deepEquals(predBin.dv()));

        assertTrue(resultNom.firstDensity().rvar(0).deepEquals(resultBin.firstDensity().rvar(0)));
        assertTrue(resultNom.firstDensity().rvar(1).dv().deepEquals(resultBin.firstDensity().rvar(1).dv()));
    }

    @Test
    void testLearningArtifacts() {
        int n = 100;
        VarDouble x = VarDouble.empty(2 * n).name("x");
        VarDouble.sample(Normal.of(0, 1), random, n).dv().addTo(x.dv().range(0, n), 0);
        VarDouble.sample(Normal.of(2, 1), random, n).dv().addTo(x.dv().range(n, 2 * n), 0);

        VarNominal y = VarNominal.from(2 * n, i -> i < n ? "1" : "2").name("y");

        var model = BinaryLogistic.newModel().fit(SolidFrame.byVars(x, y), "y");
        assertNotNull(model.iterationLoss());
        assertFalse(model.iterationLoss().isEmpty());
        for (int i = 1; i < model.iterationLoss().size(); i++) {
            assertTrue(model.iterationLoss().get(i - 1) >= model.iterationLoss().get(i));
        }

        assertNotNull(model.iterationWeights());
        assertFalse(model.iterationWeights().isEmpty());
        assertEquals(model.iterationLoss().size(), model.iterationWeights().size());
        for (var w : model.iterationWeights()) {
            assertNotNull(w);
        }
    }
}
