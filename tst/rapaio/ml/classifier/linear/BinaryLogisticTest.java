package rapaio.ml.classifier.linear;

import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VType;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.ml.common.Capabilities;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/22/20.
 */
public class BinaryLogisticTest {

    @Test
    void testBuilders() {
        BinaryLogistic defaultModel = BinaryLogistic.newModel();

        assertEquals(1e-10, defaultModel.getEps());
        assertEquals(1.0, defaultModel.getIntercept());
        assertEquals(BinaryLogistic.Initialize.EXPECTED_LOG_VAR, defaultModel.getInitialize());
        assertNull(defaultModel.getNominalLevel());
        assertEquals(0.0, defaultModel.getL1Factor());
        assertEquals(0.0, defaultModel.getL2Factor());
        assertEquals(BinaryLogistic.Method.IRLS, defaultModel.getMethod());

        BinaryLogistic customModel = BinaryLogistic.newModel()
                .withEps(1e-5)
                .withInitialize(BinaryLogistic.Initialize.ZERO)
                .withIntercept(0.0)
                .withNominalLevel("test")
                .withL1Factor(2.0)
                .withL2Factor(3.0)
                .withMethod(BinaryLogistic.Method.NEWTON);

        assertEquals(1e-5, customModel.getEps());
        assertEquals(0.0, customModel.getIntercept());
        assertEquals(BinaryLogistic.Initialize.ZERO, customModel.getInitialize());
        assertEquals("test", customModel.getNominalLevel());
        assertEquals(2.0, customModel.getL1Factor());
        assertEquals(3.0, customModel.getL2Factor());
        assertEquals(BinaryLogistic.Method.NEWTON, customModel.getMethod());

        BinaryLogistic customCopyModel = customModel.newInstance();

        assertEquals(1e-5, customCopyModel.getEps());
        assertEquals(0.0, customCopyModel.getIntercept());
        assertEquals(BinaryLogistic.Initialize.ZERO, customCopyModel.getInitialize());
        assertEquals("test", customCopyModel.getNominalLevel());
        assertEquals(2.0, customCopyModel.getL1Factor());
        assertEquals(3.0, customCopyModel.getL2Factor());
        assertEquals(BinaryLogistic.Method.NEWTON, customCopyModel.getMethod());

        assertEquals("BinaryLogistic", customCopyModel.name());
        assertEquals("BinaryLogistic{intercept=0, initialize=Zero, nominalLevel=test, method=NEWTON, l1factor=2, l2factor=3, eps=1.0E-5, runs=1}", customCopyModel.fullName());
    }

    @Test
    void testCapabilities() {
        Capabilities capabilities = BinaryLogistic.newModel().capabilities();
        assertEquals(Arrays.asList(VType.BINARY, VType.INT, VType.DOUBLE), capabilities.getInputTypes());
        assertEquals(1, capabilities.getMinInputCount());
        assertEquals(10000, capabilities.getMaxInputCount());
        assertFalse(capabilities.getAllowMissingInputValues());

        assertEquals(Arrays.asList(VType.NOMINAL, VType.BINARY), capabilities.getTargetTypes());
        assertEquals(1, capabilities.getMinTargetCount());
        assertEquals(1, capabilities.getMaxTargetCount());
        assertFalse(capabilities.getAllowMissingTargetValues());
    }

    @Test
    void testSymmetricAroundZeroSeparable() {

        var model = BinaryLogistic.newModel()
                .withRuns(100)
                .withInitialize(BinaryLogistic.Initialize.ZERO)
                .withEps(0.000001);

        VarDouble x = VarDouble.copy(-5, -4, -3, -2, -1, 1, 2, 3, 4, 5).withName("x");
        VarNominal y = VarNominal.copy("1", "1", "1", "1", "1", "0", "0", "0", "0", "0").withName("y");
        Frame df = SolidFrame.byVars(x, y);

        var result = model.fit(df, "y").predict(df);
        assertTrue(model.isConverged());

        assertTrue(result.firstClasses().deepEquals(y));
    }

}
