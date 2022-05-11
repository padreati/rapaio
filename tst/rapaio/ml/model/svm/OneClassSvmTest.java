package rapaio.ml.model.svm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.color;
import static rapaio.sys.With.fill;
import static rapaio.sys.With.pch;

import java.io.IOException;
import java.util.logging.Level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.distributions.Uniform;
import rapaio.core.tools.GridData;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.experiment.ml.svm.libsvm.svm_model;
import rapaio.experiment.ml.svm.libsvm.svm_predict;
import rapaio.experiment.ml.svm.libsvm.svm_train;
import rapaio.graphics.Plotter;
import rapaio.graphics.opt.Gradient;
import rapaio.graphics.plot.GridLayer;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.DMatrixDenseC;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.sys.WS;
import rapaio.util.collection.DoubleArrays;

public class OneClassSvmTest {

    private final int N = 2000;
    private final int M = N / 10;
    private final int LEN = N + M;
    private Var x1;
    private Var x2;
    private Frame df;

    private DMatrix xs;

    @BeforeEach
    void beforeEach() {
        WS.initLog(Level.SEVERE);
        RandomSource.setSeed(42);

        Normal normal1 = Normal.of(0, 2);
        Normal normal2 = Normal.of(1, 3);
        x1 = VarDouble.from(N, row -> normal1.sampleNext()).name("x1");
        x2 = VarDouble.from(N, row -> x1.getDouble(row) * 2 + normal2.sampleNext()).name("x2");

        // add some random uniform
        Uniform uniform = new Uniform(-10, 10);
        for (int i = 0; i < M; i++) {
            x1.addDouble(uniform.sampleNext());
            x2.addDouble(uniform.sampleNext());
        }
        df = SolidFrame.byVars(x1, x2);
        xs = DMatrix.copy(df);
    }

    @Test
    void testOneClass() throws IOException {

        svm_train t = new svm_train();
        String[] argv = new String[] {"-s", "2",
                "-t", "2",
                "-g", "0.01",
                "-n", "0.1",
                "-b", "0"};
        RandomSource.setSeed(42);
        svm_model model = t.run(xs, DVector.fill(LEN, 0), argv);
        svm_predict.Prediction pred = svm_predict.predict(model, xs, 1);

        OneClassSvm ocs = OneClassSvm
                .newModel()
                .kernel.set(new RBFKernel(0.01))
                .nu.set(0.1);
        var result = ocs.fit(df, null).predict(df);


        assertArrayEquals(DVector.wrap(pred.classes()).apply(v -> v < 0 ? 0 : 1).denseCopy().array(),
                DoubleArrays.newFrom(0, pred.classes().length, i -> result.assignment().getDouble(i)));
    }
}
