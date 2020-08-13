package rapaio.ml.classifier.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;
import rapaio.experiment.ml.common.predicate.RowPredicate;
import rapaio.ml.classifier.tree.ctree.Candidate;
import rapaio.ml.classifier.tree.ctree.Node;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/11/20.
 */
public class CTreeTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void candidateTest() {
        Candidate c = new Candidate(0.1, "t1");
        c.addGroup(RowPredicate.numLessEqual("x", 1));
        c.addGroup(RowPredicate.numGreater("x", 1));

        Frame df = SolidFrame.byVars(VarDouble.wrap(0).withName("x"));

        assertTrue(c.groupPredicates.get(0).test(0, df));
        assertFalse(c.groupPredicates.get(1).test(0, df));

        assertEquals(0.1, c.score, 1e-10);
        assertEquals("t1", c.testName);

        Candidate b = new Candidate(0.2, "t2");

        assertTrue(c.score < b.score);
    }

    @Test
    void testBuilderDecisionStump() {
        Frame df = Datasets.loadIrisDataset();

        CTree tree = CTree.newDecisionStump();
        assertEquals(1, tree.maxDepth.get());

        tree.fit(df, "class");

        Node root = tree.getRoot();
        assertEquals("root", root.groupName);

        String testName = root.bestCandidate.testName;
        if ("petal-width".equals(testName)) {
            assertEquals("petal-width", root.bestCandidate.testName);
            assertEquals("petal-width <= 0.8", root.bestCandidate.groupPredicates.get(0).toString());
            assertEquals("petal-width > 0.8", root.bestCandidate.groupPredicates.get(1).toString());
        } else {
            assertEquals("petal-length", root.bestCandidate.testName);
            assertEquals("petal-length <= 2.45", root.bestCandidate.groupPredicates.get(0).toString());
            assertEquals("petal-length > 2.45", root.bestCandidate.groupPredicates.get(1).toString());
        }
    }

    @Test
    void testPredictorStandard() {
        Frame df = Datasets.loadIrisDataset();
        CTree tree = CTree.newCART().maxDepth.set(10000).minCount.set(1);
        tree.fit(df, "class");

        var pred = tree.predict(df, true, true);
        df = df.bindVars(pred.firstClasses().copy().withName("predict"));

        Frame match = df.stream().filter(spot -> spot.getInt("class") == spot.getInt("predict")).toMappedFrame();
        assertEquals(150, match.rowCount());

        df.setMissing(0, 0);
        df.setMissing(0, 1);
        df.setMissing(0, 2);
        df.setMissing(0, 3);

        tree.predict(df, true, false);
        match = df.stream().filter(spot -> spot.getInt("class") == spot.getInt("predict")).toMappedFrame();
        assertEquals(150, match.rowCount());
    }

    @Test
    void printingTest() {

        var iris = Datasets.loadIrisDataset();
        var model = CTree.newCART().fit(iris, "class");

        assertEquals("CTree model\n" +
                "================\n" +
                "\n" +
                "Description:\n" +
                "CTree{purity=GiniGain,splitter=Weighted,varSelector=VarSelector[ALL]}\n" +
                "\n" +
                "Capabilities:\n" +
                "types inputs/targets: NOMINAL,INT,DOUBLE,BINARY/NOMINAL\n" +
                "counts inputs/targets: [1,1000000] / [1,1]\n" +
                "missing inputs/targets: true/false\n" +
                "\n" +
                "Learned model:\n" +
                "\n" +
                "total number of nodes: 19\n" +
                "total number of leaves: 10\n" +
                "description:\n" +
                "split, n/err, classes (densities) [* if is leaf / purity if not]\n" +
                "\n" +
                "|- 0. root    150/100 virginica (0.333 0.333 0.333 ) [0.3234323]\n" +
                "|   |- 1. petal-width <= 0.8    50/0 setosa (1 0 0 ) *\n" +
                "|   |- 2. petal-width > 0.8    100/50 versicolor (0 0.5 0.5 ) [0.3878205]\n" +
                "|   |   |- 3. petal-width <= 1.75    54/5 versicolor (0 0.907 0.093 ) [0.0577325]\n" +
                "|   |   |   |- 5. petal-length <= 4.95    48/1 versicolor (0 0.979 0.021 ) [0.0130208]\n" +
                "|   |   |   |   |- 9. petal-width <= 1.65    47/0 versicolor (0 1 0 ) *\n" +
                "|   |   |   |   |- 10. petal-width > 1.65    1/0 virginica (0 0 1 ) *\n" +
                "|   |   |   |- 6. petal-length > 4.95    6/2 virginica (0 0.333 0.667 ) [0.0444444]\n" +
                "|   |   |   |   |- 11. sepal-width <= 2.65    2/0 virginica (0 0 1 ) *\n" +
                "|   |   |   |   |- 12. sepal-width > 2.65    4/2 versicolor (0 0.5 0.5 ) [0.1666667]\n" +
                "|   |   |   |   |   |- 15. petal-width <= 1.65    3/1 virginica (0 0.333 0.667 ) [0]\n" +
                "|   |   |   |   |   |   |- 17. sepal-length <= 6.15    1/0 versicolor (0 1 0 ) *\n" +
                "|   |   |   |   |   |   |- 18. sepal-length > 6.15    2/0 virginica (0 0 1 ) *\n" +
                "|   |   |   |   |   |- 16. petal-width > 1.65    1/0 versicolor (0 1 0 ) *\n" +
                "|   |   |- 4. petal-width > 1.75    46/1 virginica (0 0.022 0.978 ) [0.0014703]\n" +
                "|   |   |   |- 7. sepal-width <= 3.15    32/0 virginica (0 0 1 ) *\n" +
                "|   |   |   |- 8. sepal-width > 3.15    14/1 virginica (0 0.071 0.929 ) [0]\n" +
                "|   |   |   |   |- 13. sepal-length <= 6.05    1/0 versicolor (0 1 0 ) *\n" +
                "|   |   |   |   |- 14. sepal-length > 6.05    13/0 virginica (0 0 1 ) *\n", model.toSummary());

        assertEquals(model.toContent(), model.toSummary());
        assertEquals(model.toFullContent(), model.toSummary());

        assertEquals("CTree{purity=GiniGain,splitter=Weighted,varSelector=VarSelector[ALL]}", model.toString());
    }
}
