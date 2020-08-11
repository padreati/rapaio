package rapaio.ml.regression.boost;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;
import rapaio.ml.loss.L2Loss;
import rapaio.ml.regression.tree.RTree;
import rapaio.ml.regression.tree.rtree.RTreeSplitter;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/8/20.
 */
public class GBTRegressionTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(1234);
    }

    @Test
    void smokeTest() throws IOException {

        Var err = VarDouble.empty();
        var loss = new L2Loss();
        var advertise = Datasets.loadISLAdvertising().removeVars("ID");
        var model = GBTRegression.newModel()
                .runs.set(500)
                .shrinkage.set(0.6)
                .eps.set(1e-20)
                .model.set(RTree.newCART().maxDepth.set(3))
                .runningHook.set((m, r) -> {
                    if ((r + 1) % 25 != 0) {
                        err.addMissing();
                        return;
                    }
                    double e = loss.errorScore(advertise.rvar("Sales"),
                            m.predict(advertise).firstPrediction());
                    err.addDouble(e);
                });

        model.fit(advertise, "Sales");

        var err2 = err.stream().complete().toMappedVar();
        for (int i = 1; i < err2.rowCount(); i++) {
            assertTrue(err2.getDouble(i - 1) >= err2.getDouble(i));
        }
        assertTrue(err2.getDouble(err2.rowCount() - 1) < 1e-1);
    }

    @Test
    void printingTest() throws IOException {
        var advertise = Datasets.loadISLAdvertising().removeVars("ID");
        var model = GBTRegression.newModel()
                .runs.set(100)
                .model.set(RTree.newDecisionStump());

        assertEquals("GBTRegression", model.name());

        assertEquals("GBTRegression{nodeModel=RTree{maxDepth=2,splitter=Majority," +
                "testMap={BINARY=NumBin,INT=NumBin,NOMINAL=NomBin,DOUBLE=NumBin,LONG=NumBin,STRING=Ignore}}," +
                "runs=100}", model.fullName());

        assertEquals("GBTRegression{nodeModel=RTree{maxDepth=2,splitter=Majority," +
                "testMap={BINARY=NumBin,INT=NumBin,NOMINAL=NomBin,DOUBLE=NumBin,LONG=NumBin,STRING=Ignore}}," +
                "runs=100}; fitted=false", model.toString());

        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: GBTRegression\n" +
                "Model instance: GBTRegression{nodeModel=RTree{maxDepth=2,splitter=Majority," +
                "testMap={BINARY=NumBin,INT=NumBin,NOMINAL=NomBin,DOUBLE=NumBin,LONG=NumBin,STRING=Ignore}},runs=100}\n" +
                "> model not trained.\n" +
                "\n", model.toSummary());

        assertEquals(model.toSummary(), model.toContent());
        assertEquals(model.toSummary(), model.toFullContent());

        model.fit(advertise, "Sales");

        assertEquals("GBTRegression", model.name());

        assertEquals("GBTRegression{nodeModel=RTree{maxDepth=2,splitter=Majority," +
                "testMap={BINARY=NumBin,INT=NumBin,NOMINAL=NomBin,DOUBLE=NumBin,LONG=NumBin,STRING=Ignore}}," +
                "runs=100}", model.fullName());

        assertEquals("GBTRegression{nodeModel=RTree{maxDepth=2,splitter=Majority," +
                "testMap={BINARY=NumBin,INT=NumBin,NOMINAL=NomBin,DOUBLE=NumBin,LONG=NumBin,STRING=Ignore}}," +
                "runs=100}; fitted=true, fitted trees:100", model.toString());

        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: GBTRegression\n" +
                "Model instance: GBTRegression{nodeModel=RTree{maxDepth=2,splitter=Majority,testMap={BINARY=NumBin,INT=NumBin,NOMINAL=NomBin,DOUBLE=NumBin,LONG=NumBin,STRING=Ignore}},runs=100}\n" +
                "> model is trained.\n" +
                "> input variables: \n" +
                "1. TV        dbl \n" +
                "2. Radio     dbl \n" +
                "3. Newspaper dbl \n" +
                "> target variables: \n" +
                "1. Sales dbl \n" +
                "\n" +
                "Target <<< Sales >>>\n" +
                "\n" +
                "> Number of fitted trees: 100\n", model.toSummary());

        assertEquals(model.toSummary(), model.toContent());
        assertEquals(model.toSummary(), model.toFullContent());
    }

    @Test
    void newInstanceTest() {
        var model = GBTRegression.newModel()
                .runs.set(100)
                .model.set(RTree.newDecisionStump());
        var copy = model.newInstance();

        assertEquals(model.toString(), copy.toString());

        model = GBTRegression.newModel()
                .runs.set(120)
                .model.set(RTree.newCART().splitter.set(RTreeSplitter.IGNORE));
        copy = model.newInstance();

        assertEquals(model.toString(), copy.toString());
    }
}
