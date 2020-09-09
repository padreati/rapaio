package rapaio.ml.classifier.boost;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.SamplingTools;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.ClassifierRunHook;
import rapaio.ml.common.VarSelector;
import rapaio.ml.eval.metric.Accuracy;
import rapaio.ml.regression.tree.RTree;
import rapaio.sys.WS;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static rapaio.graphics.Plotter.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/11/20.
 */
public class GBTClassifierModelTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(133);
    }

    //    @Test
    void smokeTest() throws IOException {

        var spam = Datasets.loadSpamBase();
        var split = SamplingTools.trainTestSplit(spam, null, 0.8, true, "spam");
        var hook = new ClassifierRunHook()
                .train.set(split.trainDf)
                .test.set(split.testDf)
                .skipStep.set(50)
                .metrics.add(Accuracy.newMetric());
        var model = GBTClassifierModel.newModel().runs.set(2000).runningHook.set(hook);


        model.fit(spam, "spam");

        var result = model.predict(spam);

        WS.draw(lines(hook.getRuns(), hook.getTrainScores().get(Accuracy.ID), color(1))
                .lines(hook.getRuns(), hook.getTestScores().get(Accuracy.ID), color(2)));
    }

    @Test
    void buildTest() throws IOException {

        var spam = Datasets.loadSpamBase();
        var model = GBTClassifierModel.newModel()
                .model.set(RTree.newC45().minCount.set(4).maxDepth.set(3).varSelector.set(VarSelector.fixed(10)))
                .debug.set(true)
                .runs.set(10);

        String target = "spam";
        model.fit(spam, target);

        assertEquals(2, model.getTrees().size());
        assertEquals(10, model.getTrees().get(0).size());
        assertEquals(10, model.getTrees().get(1).size());

        assertEquals("GBTClassifier{debug=true,model=RTree{maxDepth=3,minCount=4,splitter=Random," +
                "varSelector=VarSelector[10]},runs=10}; fitted=true, fitted trees=10", model.toString());

        var result = model.predict(spam, true, true);

        assertNotNull(result);
        assertEquals(spam.rvar("spam").levels().size(), result.firstDensity().varCount());
    }

    @Test
    void newInstanceTest() {
        var model = GBTClassifierModel.newModel()
                .model.set(RTree.newC45().minCount.set(4).maxDepth.set(5).varSelector.set(VarSelector.fixed(10)))
                .runs.set(10);

        var copy = model.newInstance();

        assertEquals(model.toString(), copy.toString());
        assertEquals(model.toSummary(), copy.toSummary());
        assertEquals(model.toContent(), copy.toContent());
        assertEquals(model.toFullContent(), copy.toFullContent());
    }
}
