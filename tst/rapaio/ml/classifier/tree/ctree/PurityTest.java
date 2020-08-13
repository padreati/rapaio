package rapaio.ml.classifier.tree.ctree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.tests.ChiSqIndependence;
import rapaio.core.tools.DensityTable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/13/20.
 */
public class PurityTest {

    private DensityTable<String, String> dt;

    @BeforeEach
    void beforeEach() {
        dt = DensityTable.emptyByLabel(true, List.of("a", "b"), List.of("x", "y"));

        // 3,6
        // 3,2
        dt.increment(0, 0, 3);
        dt.increment(0, 1, 6);
        dt.increment(1, 0, 3);
        dt.increment(1, 1, 2);
    }

    @Test
    void purityTest() {

        assertEquals(dt.splitByRowInfoGain(), Purity.InfoGain.compute(dt));
        assertEquals(dt.splitByRowGainRatio(), Purity.GainRatio.compute(dt), 1e-10);
        assertEquals(dt.splitByRowGiniGain(), Purity.GiniGain.compute(dt));
        assertEquals(1 - ChiSqIndependence.from(dt, false).pValue(), Purity.ChiSquare.compute(dt));
    }
}
