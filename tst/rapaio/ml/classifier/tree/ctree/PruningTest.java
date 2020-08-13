package rapaio.ml.classifier.tree.ctree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.tree.CTree;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/13/20.
 */
public class PruningTest {

    private final Frame iris = Datasets.loadIrisDataset();
    private CTree model;

    @BeforeEach
    void beforeEach() {

        // select a model which expands as much as possible
        model = CTree.newCART()
                .maxDepth.set(Integer.MAX_VALUE)
                .minCount.set(1)
                .minGain.set(0.0)
                .pruning.set(Pruning.None)
                .fit(iris, "class");
    }

    @Test
    void noPruningTest() {
        int initCountNodes = model.countNodes(false);
        Pruning.None.prune(model, iris);
        int afterCountNodes = model.countNodes(false);

        assertEquals(initCountNodes, afterCountNodes);
    }

    @Test
    void reducedErrorPruningTest() {
        int initCountNodes = model.countNodes(false);
        Pruning.ReducedError.prune(model, iris);
        int afterCountNodes = model.countNodes(false);

        assertTrue(initCountNodes > afterCountNodes);
    }
}
