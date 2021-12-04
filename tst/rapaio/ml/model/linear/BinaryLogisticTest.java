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

package rapaio.ml.model.linear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/22/20.
 */
public class BinaryLogisticTest {

    @Test
    void testBuilders() {
        BinaryLogistic defaultModel = BinaryLogistic.newModel();

        assertEquals(1e-10, defaultModel.eps.get());
        assertEquals(1.0, defaultModel.intercept.get());
        assertEquals(BinaryLogistic.Initialize.EXPECTED_LOG_VAR, defaultModel.init.get());
        assertEquals("", defaultModel.nominalLevel.get());
        assertEquals(0.0, defaultModel.l1Factor.get());
        assertEquals(0.0, defaultModel.l2Factor.get());
        assertEquals(BinaryLogistic.Method.IRLS, defaultModel.solver.get());

        BinaryLogistic customModel = BinaryLogistic.newModel()
                .eps.set(1e-5)
                .init.set(BinaryLogistic.Initialize.ZERO)
                .intercept.set(0.0)
                .nominalLevel.set("test")
                .l1Factor.set(2.0)
                .l2Factor.set(3.0)
                .solver.set(BinaryLogistic.Method.NEWTON);

        assertEquals(1e-5, customModel.eps.get());
        assertEquals(0.0, customModel.intercept.get());
        assertEquals(BinaryLogistic.Initialize.ZERO, customModel.init.get());
        assertEquals("test", customModel.nominalLevel.get());
        assertEquals(2.0, customModel.l1Factor.get());
        assertEquals(3.0, customModel.l2Factor.get());
        assertEquals(BinaryLogistic.Method.NEWTON, customModel.solver.get());

        BinaryLogistic customCopyModel = customModel.newInstance();

        assertEquals(1e-5, customCopyModel.eps.get());
        assertEquals(0.0, customCopyModel.intercept.get());
        assertEquals(BinaryLogistic.Initialize.ZERO, customCopyModel.init.get());
        assertEquals("test", customCopyModel.nominalLevel.get());
        assertEquals(2.0, customCopyModel.l1Factor.get());
        assertEquals(3.0, customCopyModel.l2Factor.get());
        assertEquals(BinaryLogistic.Method.NEWTON, customCopyModel.solver.get());

        assertEquals("BinaryLogistic", customCopyModel.name());
        assertEquals("BinaryLogistic{eps=0.00001,init=ZERO,intercept=0,l1factor=2,l2factor=3," +
                "nominalLevel=test,solver=NEWTON}", customCopyModel.fullName());
    }

    @Test
    void testCapabilities() {
        Capabilities capabilities = BinaryLogistic.newModel().capabilities();
        assertEquals(Arrays.asList(VarType.BINARY, VarType.INT, VarType.DOUBLE), capabilities.inputTypes());
        assertEquals(1, capabilities.minInputCount());
        assertEquals(10000, capabilities.maxInputCount());
        assertFalse(capabilities.allowMissingInputValues());

        assertEquals(Arrays.asList(VarType.NOMINAL, VarType.BINARY), capabilities.targetTypes());
        assertEquals(1, capabilities.minTargetCount());
        assertEquals(1, capabilities.maxTargetCount());
        assertFalse(capabilities.allowMissingTargetValues());
    }

    @Test
    void testSymmetricAroundZeroSeparable() {

        var model1 = BinaryLogistic.newModel()
                .runs.set(100)
                .init.set(BinaryLogistic.Initialize.ZERO)
                .eps.set(0.000001);

        VarDouble x = VarDouble.copy(-5, -4, -3, -2, -1, 1, 2, 3, 4, 5).name("x");
        VarNominal y = VarNominal.copy("1", "1", "1", "1", "1", "0", "0", "0", "0", "0").name("y");
        Frame df = SolidFrame.byVars(x, y);

        var result1 = model1.fit(df, "y").predict(df);
        assertTrue(model1.isConverged());
        assertTrue(result1.firstClasses().deepEquals(y));

        var model2 = model1.newInstance().solver.set(BinaryLogistic.Method.NEWTON);
        var result2 = model2.fit(df, "y").predict(df);
        assertTrue(model2.isConverged());
        assertTrue(result2.firstClasses().deepEquals(y));
    }

}
