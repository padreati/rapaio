package rapaio.data.filter;

import org.junit.jupiter.api.Test;
import rapaio.data.VRange;
import rapaio.data.VType;
import rapaio.datasets.Datasets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/21/20.
 */
public class FFilterSequenceTest {

    private static final double TOL = 1e-12;

    @Test
    void smokeTest() {
        var iris = Datasets.loadIrisDataset();
        var filters = FFilterSequence.of(
                FApply.onDouble(x -> x + 10, VRange.onlyTypes(VType.DOUBLE)),
                FApply.onLabel(cl -> cl + "-x", VRange.of("class"))
        );

        var transformed = iris.copy().fapply(filters);

        assertFalse(iris.deepEquals(transformed));
        transformed.rvar("class").forEachSpot(s -> assertTrue(s.getLabel().endsWith("-x")));
        transformed.rvar(0).forEachSpot(s -> assertEquals(iris.getDouble(s.row(), 0), s.getDouble() - 10, TOL));

        var second = iris.copy().fapply(filters.newInstance());

        assertTrue(second.deepEquals(transformed));

        Set<String> names = new HashSet<>(Arrays.asList(filters.varNames()));
        assertEquals(names, new HashSet<>(Arrays.asList(iris.varNames())));
    }
}
