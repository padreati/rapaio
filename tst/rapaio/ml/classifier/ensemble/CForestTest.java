package rapaio.ml.classifier.ensemble;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(iris.rowCount(), prediction.firstClasses().rowCount());
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
        assertEquals("CForest\n" +
                "=======\n" +
                "\n" +
                "Description:\n" +
                "CForest{freqVI=true,gainVI=true,oob=true,rowSampler=Bootstrap(p=1),runs=100,viPerm=true}\n" +
                "\n" +
                "Capabilities:\n" +
                "types inputs/targets: NOMINAL,INT,DOUBLE,BINARY/NOMINAL\n" +
                "counts inputs/targets: [1,1000000] / [1,1]\n" +
                "missing inputs/targets: true/false\n" +
                "\n" +
                "Model fitted: false.\n", model.toSummary());
        assertEquals(model.toSummary(), model.toContent());
        assertEquals(model.toSummary() + "\n", model.toFullContent());

        model.fit(iris, "class");

        assertEquals("CForest{freqVI=true,gainVI=true,oob=true,rowSampler=Bootstrap(p=1),runs=100,viPerm=true}; fitted:true, fitted trees:100", model.toString());
        assertEquals("CForest\n" +
                "=======\n" +
                "\n" +
                "Description:\n" +
                "CForest{freqVI=true,gainVI=true,oob=true,rowSampler=Bootstrap(p=1),runs=100,viPerm=true}\n" +
                "\n" +
                "Capabilities:\n" +
                "types inputs/targets: NOMINAL,INT,DOUBLE,BINARY/NOMINAL\n" +
                "counts inputs/targets: [1,1000000] / [1,1]\n" +
                "missing inputs/targets: true/false\n" +
                "\n" +
                "Model fitted: true.\n" +
                "Learned model:\n" +
                "input vars: \n" +
                "0. sepal-length : DOUBLE  | \n" +
                "1.  sepal-width : DOUBLE  | \n" +
                "2. petal-length : DOUBLE  | \n" +
                "3.  petal-width : DOUBLE  | \n" +
                "\n" +
                "target vars:\n" +
                "> class : NOMINAL [?,setosa,versicolor,virginica]\n" +
                "\n" +
                "\n" +
                "Fitted trees:100\n" +
                "oob enabled:true\n" +
                "oob error:0.04\n", model.toSummary());
        assertEquals(model.toSummary(), model.toContent());
        assertEquals("CForest\n" +
                "=======\n" +
                "\n" +
                "Description:\n" +
                "CForest{freqVI=true,gainVI=true,oob=true,rowSampler=Bootstrap(p=1),runs=100,viPerm=true}\n" +
                "\n" +
                "Capabilities:\n" +
                "types inputs/targets: NOMINAL,INT,DOUBLE,BINARY/NOMINAL\n" +
                "counts inputs/targets: [1,1000000] / [1,1]\n" +
                "missing inputs/targets: true/false\n" +
                "\n" +
                "Model fitted: true.\n" +
                "Learned model:\n" +
                "input vars: \n" +
                "0. sepal-length : DOUBLE  | \n" +
                "1.  sepal-width : DOUBLE  | \n" +
                "2. petal-length : DOUBLE  | \n" +
                "3.  petal-width : DOUBLE  | \n" +
                "\n" +
                "target vars:\n" +
                "> class : NOMINAL [?,setosa,versicolor,virginica]\n" +
                "\n" +
                "\n" +
                "Fitted trees:100\n" +
                "oob enabled:true\n" +
                "oob error:0.04\n" +
                "\n" +
                "Frequency Variable Importance:\n" +
                "        name      mean      sd     scaled score \n" +
                "[0] petal-length 164.71 82.3588226 100          \n" +
                "[1]  petal-width 139.03 80.1589904  84.4089612  \n" +
                "[2] sepal-length  78.6  85.4258495  47.7202356  \n" +
                "[3]  sepal-width  35.46 37.9205496  21.5287475  \n" +
                "\n" +
                "Gain Variable Importance:\n" +
                "        name        mean        sd     scaled score \n" +
                "[0] petal-length 39.5796441 28.6634617 100          \n" +
                "[1]  petal-width 33.9636502 28.1915658  85.8109035  \n" +
                "[2] sepal-length  9.2588583 14.0932391  23.3929801  \n" +
                "[3]  sepal-width  1.5107091  3.6596976   3.8168839  \n" +
                "\n" +
                "Permutation Variable Importance:\n" +
                "        name        mean        sd     scaled score \n" +
                "[0] petal-length 39.5796441 28.6634617 100          \n" +
                "[1]  petal-width 33.9636502 28.1915658  85.8109035  \n" +
                "[2] sepal-length  9.2588583 14.0932391  23.3929801  \n" +
                "[3]  sepal-width  1.5107091  3.6596976   3.8168839  \n" +
                "\n", model.toFullContent());
    }
}
