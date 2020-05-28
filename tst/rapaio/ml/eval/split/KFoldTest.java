package rapaio.ml.eval.split;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/18/20.
 */
public class KFoldTest {

    private Frame df;

    private Var weights;

    @BeforeEach
    void beforeEach() {
        weights = VarDouble.seq(10).withName("w");
        df = SolidFrame.byVars(weights);
    }

    @Test
    void testKFold() {

        KFold kFold = new KFold(2, 2);
        assertEquals(2, kFold.getFolds());
        assertEquals(2, kFold.getRounds());

        List<Split> splits = kFold.generateSplits(df, weights);

        assertNotNull(splits);
        assertEquals(4, splits.size());

        int roundSum = 0;
        int foldSum = 0;
        Map<Integer, Double> dfsSum = new HashMap<>();
        Map<Integer, Double> weightsSum = new HashMap<>();
        for (Split split : splits) {
            roundSum += split.getRound();
            foldSum += split.getFold();

            if (!dfsSum.containsKey(split.getRound())) {
                dfsSum.put(split.getRound(), 0.0);
            }
            if (!weightsSum.containsKey(split.getRound())) {
                weightsSum.put(split.getRound(), 0.0);
            }
            dfsSum.put(split.getRound(), dfsSum.get(split.getRound()) + split.getTrainDf().rvar(0).op().nansum());
            weightsSum.put(split.getRound(), weightsSum.get(split.getRound()) + split.getTrainWeights().op().nansum());
        }

        assertEquals(2, roundSum);
        assertEquals(2, foldSum);

        for (double v : dfsSum.values()) {
            assertEquals(55.0, v);
        }
    }

    @Test
    void testBuilders() {
        KFold kFold1 = new KFold(1, 10);
        KFold kFold2 = new KFold(10);

        assertEquals(kFold1.getRounds(), kFold2.getRounds());
        assertEquals(kFold1.getFolds(), kFold2.getFolds());
    }
}
