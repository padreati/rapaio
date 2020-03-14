package rapaio.experiment.ml.classifier.tree.ctree;

import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.experiment.ml.classifier.tree.CTreeCandidate;
import rapaio.experiment.ml.common.predicate.RowPredicate;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/21/17.
 */
public class CTreeCandidateTest {

    @Test
    void basic() {
        CTreeCandidate c = new CTreeCandidate(0.1, "t1");
        c.addGroup(RowPredicate.numLessEqual("x", 1));
        c.addGroup(RowPredicate.numGreater("x", 1));

        Frame df = SolidFrame.byVars(VarDouble.wrap(0).withName("x"));

        assertTrue(c.getGroupPredicates().get(0).test(0, df));
        assertFalse(c.getGroupPredicates().get(1).test(0, df));

        assertEquals(0.1, c.getScore(), 1e-10);
        assertEquals("t1", c.getTestName());

        CTreeCandidate b = new CTreeCandidate(0.2, "t2");

        assertTrue(c.getScore() < b.getScore());
    }
}
