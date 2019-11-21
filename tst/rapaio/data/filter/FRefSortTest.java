package rapaio.data.filter;

import org.junit.Test;
import rapaio.data.Frame;

import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/4/18.
 */
public class FRefSortTest {

    @Test
    public void testRefSort() {

        Frame src = FFilterTestUtil.allDoubleNominal(10, 2, 2);

        Frame sort1 = src.fapply(FRefSort.by(src.rvar("v2").refComparator()));
        Frame sort2 = sort1.fapply(FRefSort.by(sort1.rvar("v1").refComparator()));
        Frame sort3 = src.fapply(FRefSort.by(src.rvar("v1").refComparator(), src.rvar("v2").refComparator()).newInstance());

        assertTrue(sort2.deepEquals(sort3));
    }
}
