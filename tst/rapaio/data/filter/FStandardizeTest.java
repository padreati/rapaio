package rapaio.data.filter;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.VType;

import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/3/18.
 */
public class FStandardizeTest {

    @Test
    public void testDouble() {
        Frame src = FFilterTestUtil.allDoubles(100, 2);

        FStandardize filter = FStandardize.on(VRange.all());
        filter.fit(src);

        Frame std1 = src.copy().fapply(filter);
        Frame std2 = src.copy().fapply(FStandardize.on(VRange.onlyTypes(VType.DOUBLE)).newInstance());

        assertTrue(std1.deepEquals(std2));
    }
}
