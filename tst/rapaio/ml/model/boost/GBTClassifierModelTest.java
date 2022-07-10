/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.model.boost;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.datasets.Datasets;
import rapaio.ml.common.VarSelector;
import rapaio.ml.model.tree.RTree;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/11/20.
 */
public class GBTClassifierModelTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(133);
    }

    @Test
    void buildTest() throws IOException {

        var spam = Datasets.loadSpamBase();
        var model = GBTClassifierModel.newModel()
                .model.set(RTree.newC45().minCount.set(4).maxDepth.set(3).varSelector.set(VarSelector.fixed(10)))
                .debug.set(true)
                .runs.set(10)
                .seed.set(133L);

        String target = "spam";
        model.fit(spam, target);

        assertEquals(2, model.getTrees().size());
        assertEquals(10, model.getTrees().get(0).size());
        assertEquals(10, model.getTrees().get(1).size());

        assertEquals("GBTClassifier{debug=true,model=RTree{maxDepth=3,minCount=4,splitter=Random," +
                "varSelector=VarSelector[10]},runs=10,seed=133}; fitted=true, fitted trees=10", model.toString());

        var result = model.predict(spam, true, true);

        assertNotNull(result);
        assertEquals(spam.rvar("spam").levels().size(), result.firstDensity().varCount());
    }

    @Test
    void newInstanceTest() {
        var model = GBTClassifierModel.newModel()
                .model.set(RTree.newC45().minCount.set(4).maxDepth.set(5).varSelector.set(VarSelector.fixed(10)))
                .runs.set(10)
                .seed.set(133L);

        var copy = model.newInstance();

        assertEquals(model.toString(), copy.toString());
        assertEquals(model.toSummary(), copy.toSummary());
        assertEquals(model.toContent(), copy.toContent());
        assertEquals(model.toFullContent(), copy.toFullContent());
    }
}
