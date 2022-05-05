package rapaio.ml.model.svm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.logging.Level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.datasets.Datasets;
import rapaio.experiment.ml.svm.libsvm.svm_model;
import rapaio.experiment.ml.svm.libsvm.svm_predict;
import rapaio.experiment.ml.svm.libsvm.svm_train;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.ml.model.ClassifierResult;
import rapaio.sys.WS;

public class SVMClassifierTest {

    private static final double TOL = 1e-16;


    private Frame iris;
    private DMatrix xs;
    private DVector ys;

    @BeforeEach
    void beforeEach() {
        WS.initLog(Level.SEVERE);
        iris = Datasets.loadIrisDataset();

        xs = DMatrix.copy(iris.mapVars(VarRange.onlyTypes(VarType.DOUBLE)));
        ys = DVector.from(xs.rows(), i -> iris.rvar(4).getInt(i) - 1);
    }

    @Test
    void testIrisProbC() throws IOException {
        svm_train t = new svm_train();
        String[] argv = new String[] {"-s", "0",
                "-t", "2",
                "-g", "0.7",
                "-c", "10",
                "-b", "1"};
        svm_model model = t.run(xs, ys, argv);
        svm_predict.Prediction pred = svm_predict.predict(model, xs, 1);

        SVMClassifier c = new SVMClassifier()
                .type.set(SVMClassifier.Penalty.C)
                .c.set(10.0)
                .probability.set(true)
                .kernel.set(new RBFKernel(0.7));
        ClassifierResult cpred = c.fit(iris, "class").predict(iris);
        DMatrix cdensity = DMatrix.copy(cpred.firstDensity()).removeCols(0);

        assertTrue(pred.density().deepEquals(cdensity, TOL));
        for (int i = 0; i < pred.classes().length; i++) {
            int cls = (int) pred.classes()[i];
            assertEquals(cls + 1, cpred.firstClasses().getInt(i));
        }
    }

    @Test
    void testIrisClassC() throws IOException {
        svm_train t = new svm_train();
        String[] argv = new String[] {"-s", "0",
                "-t", "2",
                "-g", "0.7",
                "-c", "10",
                "-b", "0"};
        svm_model model = t.run(xs, ys, argv);
        svm_predict.Prediction pred = svm_predict.predict(model, xs, 1);

        SVMClassifier c = new SVMClassifier()
                .type.set(SVMClassifier.Penalty.C)
                .c.set(10.0)
                .probability.set(false)
                .kernel.set(new RBFKernel(0.7));

        ClassifierResult cpred = c.fit(iris, "class").predict(iris, true, true);
        for (int i = 0; i < pred.classes().length; i++) {
            int cls = (int) pred.classes()[i];
            assertEquals(cls + 1, cpred.firstClasses().getInt(i));
        }
    }

    @Test
    void testIrisProbNU() throws IOException {
        svm_train t = new svm_train();
        String[] argv = new String[] {"-s", "1",
                "-t", "2",
                "-g", "0.7",
                "-n", "0.1",
                "-b", "1"};
        svm_model model = t.run(xs, ys, argv);
        svm_predict.Prediction pred = svm_predict.predict(model, xs, 1);

        SVMClassifier c = new SVMClassifier()
                .type.set(SVMClassifier.Penalty.NU)
                .nu.set(.1)
                .probability.set(true)
                .kernel.set(new RBFKernel(0.7));
        ClassifierResult cpred = c.fit(iris, "class").predict(iris);
        DMatrix cdensity = DMatrix.copy(cpred.firstDensity()).removeCols(0);

        assertTrue(pred.density().deepEquals(cdensity, TOL));
        for (int i = 0; i < pred.classes().length; i++) {
            int cls = (int) pred.classes()[i];
            assertEquals(cls + 1, cpred.firstClasses().getInt(i));
        }
    }

    @Test
    void testIrisClassNu() throws IOException {
        svm_train t = new svm_train();
        String[] argv = new String[] {"-s", "1",
                "-t", "2",
                "-g", "0.7",
                "-n", "0.1",
                "-b", "0"};
        svm_model model = t.run(xs, ys, argv);
        svm_predict.Prediction pred = svm_predict.predict(model, xs, 1);

        SVMClassifier c = new SVMClassifier()
                .type.set(SVMClassifier.Penalty.NU)
                .nu.set(.1)
                .probability.set(false)
                .kernel.set(new RBFKernel(0.7));

        ClassifierResult cpred = c.fit(iris, "class").predict(iris, true, true);
        for (int i = 0; i < pred.classes().length; i++) {
            int cls = (int) pred.classes()[i];
            assertEquals(cls + 1, cpred.firstClasses().getInt(i));
        }
    }
}
