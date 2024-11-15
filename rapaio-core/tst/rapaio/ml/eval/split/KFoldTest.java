/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.ml.eval.split;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/18/20.
 */
public class KFoldTest {

    private Frame df;

    private Var weights;

    @BeforeEach
    void beforeEach() {
        weights = VarDouble.seq(10).name("w");
        df = SolidFrame.byVars(weights);
    }

    @Test
    void testKFold() {

        KFold kFold = new KFold(2, 2);
        assertEquals(2, kFold.folds());
        assertEquals(2, kFold.rounds());

        List<Split> splits = kFold.generateSplits(df, weights, new Random());

        assertNotNull(splits);
        assertEquals(4, splits.size());

        int roundSum = 0;
        int foldSum = 0;
        Map<Integer, Double> dfsSum = new HashMap<>();
        Map<Integer, Double> weightsSum = new HashMap<>();
        for (Split split : splits) {
            roundSum += split.round();
            foldSum += split.fold();

            if (!dfsSum.containsKey(split.round())) {
                dfsSum.put(split.round(), 0.0);
            }
            if (!weightsSum.containsKey(split.round())) {
                weightsSum.put(split.round(), 0.0);
            }
            dfsSum.put(split.round(), dfsSum.get(split.round()) + split.trainDf().rvar(0).narray_().nanSum());
            weightsSum.put(split.round(), weightsSum.get(split.round()) + split.trainWeights().narray_().nanSum());
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

        assertEquals(kFold1.rounds(), kFold2.rounds());
        assertEquals(kFold1.folds(), kFold2.folds());
    }
}
