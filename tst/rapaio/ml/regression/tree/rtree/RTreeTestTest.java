package rapaio.ml.regression.tree.rtree;

import org.junit.Before;
import org.junit.Test;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.filter.var.VRefSort;
import rapaio.datasets.Datasets;
import rapaio.ml.regression.tree.RTree;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/20/19.
 */
public class RTreeTestTest {

    private Frame df;
    private Var w;
    private RTree tree;
    private static final String TARGET = "humidity";
    private static final String NUM_TEST = "temp";
    private static final String NOM_TEST = "outlook";

    @Before
    public void setUp() {
        df = Datasets.loadPlay();
        w = VarDouble.fill(df.rowCount(), 1);
        tree = RTree.newDecisionStump();
    }

    @Test
    public void ignoreTest() {

        RTreeTest m = RTreeTest.Ignore;
        Optional<RTreeCandidate> cs = m.computeCandidate(tree, df, w, NOM_TEST, TARGET,
                RTreePurityFunction.WEIGHTED_VAR_GAIN);

        assertEquals("Ignore", m.name());
        assertFalse(cs.isPresent());
    }

    @Test
    public void nominalFullTest() {

        RTreeTest m = RTreeTest.NominalFull;
        Optional<RTreeCandidate> cs = m.computeCandidate(tree, df, w, NOM_TEST, TARGET,
                RTreePurityFunction.WEIGHTED_VAR_GAIN);

        assertEquals("NominalFull", m.name());
        assertTrue(cs.isPresent());

        RTreeCandidate c = cs.get();
        assertEquals(NOM_TEST, c.getTestName());

        assertEquals(3, c.getGroupNames().size());
        assertEquals(3, c.getGroupPredicates().size());

        assertEquals("outlook = 'sunny'", c.getGroupNames().get(0));
        assertEquals("outlook = 'overcast'", c.getGroupNames().get(1));
        assertEquals("outlook = 'rain'", c.getGroupNames().get(2));

        assertEquals(4.432653061224499, c.getScore(), 1e-20);
    }

    @Test
    public void nominalFullTestFailed() {

        RTreeTest m = RTreeTest.NominalFull;
        Optional<RTreeCandidate> cs = m.computeCandidate(tree, df.mapRows(1), w.mapRows(1),
                NOM_TEST, TARGET, RTreePurityFunction.WEIGHTED_VAR_GAIN);

        assertEquals("NominalFull", m.name());
        assertFalse(cs.isPresent());
    }

    @Test
    public void nominalBinaryTest() {
        RTreeTest m = RTreeTest.NominalBinary;

        assertEquals("NominalBinary", m.name());

        Optional<RTreeCandidate> cs = m.computeCandidate(tree, df, w,
                NOM_TEST, TARGET, RTreePurityFunction.WEIGHTED_VAR_GAIN);

        assertTrue(cs.isPresent());

        assertEquals("Candidate{score=4.318367346938771, testName='outlook', groupNames=[outlook = 'overcast', outlook != 'overcast']}",
                cs.get().toString());
    }

    @Test
    public void numericBinaryTest() {
        RTreeTest m = RTreeTest.NumericBinary;

        assertEquals("NumericBinary", m.name());

        Var target = df.rvar(TARGET).fapply(new VRefSort(df.rvar(NUM_TEST).refComparator()));
        Var test = df.rvar(NUM_TEST).fapply(new VRefSort(df.rvar(NUM_TEST).refComparator()));
        Var weights = w.fapply(new VRefSort(df.rvar(NUM_TEST).refComparator()));

        double variance = Variance.of(target).value();
        for(int i=1; i<test.rowCount()-2; i++) {
            double value = test.getDouble(i);

            Var left = target.stream().filter(s -> test.getDouble(s.row()) <= value).toMappedVar();
            Var right = target.stream().filter(s -> test.getDouble(s.row()) > value).toMappedVar();

            double varLeft = Variance.of(left).value();
            double varRight = Variance.of(right).value();

            System.out.println(value + "  => " + varLeft + " | " + varRight + "    -> "
                    + (variance - varLeft*left.rowCount()/test.rowCount() - varRight*right.rowCount()/test.rowCount()));
        }

        Optional<RTreeCandidate> c = m.computeCandidate(tree, df, w, NUM_TEST, TARGET,
                RTreePurityFunction.WEIGHTED_VAR_GAIN);

        assertTrue(c.isPresent());
        assertEquals("Candidate{score=32.657653061224444, testName='temp', groupNames=[temp <= 69.5, temp > 69.5]}",
                c.get().toString());
    }

}
