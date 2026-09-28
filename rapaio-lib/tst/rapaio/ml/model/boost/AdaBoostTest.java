/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.ml.model.boost;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.datasets.Datasets;
import rapaio.ml.common.VarSelector;
import rapaio.ml.model.tree.CTree;

public class AdaBoostTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void buildTest() throws IOException {

        int times = 10;
        var spam = Datasets.loadSpamBase();

        while (times-- > 0) {

            long seed = random.nextLong();
            var model = AdaBoost.newModel()
                    .model.set(CTree.newC45().minCount.set(4).maxDepth.set(5).varSelector.set(VarSelector.fixed(10)))
                    .seed.set(seed)
                    .runs.set(10);

            String target = "spam";
            model.fit(spam, target);

            assertTrue(model.getLearners().size() <= 10);
            int treeCount = model.getLearners().size();
            assertEquals(treeCount, model.getAlphas().size());

            // a model valued parameter prints its fullName (with parameters), as the regression
            // models already did; until 2026-09-28 a classifier printed the bare name() instead,
            // which depended on the reflective method order in ParamSet.format
            assertEquals("AdaBoost{model=CTree{maxDepth=5,minCount=4,purity=GainRatio,splitter=Weighted,"
                    + "testMap={BINARY=Binary,INT=NumericBinary,NOMINAL=NominalFull,DOUBLE=NumericBinary},"
                    + "varSelector=VarSelector[10]},runs=10,seed=" + seed + "}; fitted=true, fitted trees="
                    + treeCount, model.toString());

            var result = model.predict(spam, true, true);

            assertNotNull(result);
            assertEquals(spam.rvar("spam").levels().size(), result.firstDensity().varCount());
        }
    }

    @Test
    void newInstanceTest() {
        var model = AdaBoost.newModel()
                .model.set(CTree.newC45().minCount.set(4).maxDepth.set(5).varSelector.set(VarSelector.fixed(10)))
                .runs.set(10);

        var copy = model.newInstance();

        assertEquals(model.toString(), copy.toString());
        assertEquals(model.toSummary(), copy.toSummary());
        assertEquals(model.toContent(), copy.toContent());
        assertEquals(model.toFullContent(), copy.toFullContent());
    }
}
