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

package rapaio.ml.model.tree.ctree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.ml.model.tree.CTree;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/13/20.
 */
public class PruningTest {

    private final Frame iris = Datasets.loadIrisDataset();
    private CTree model;

    @BeforeEach
    void beforeEach() {

        // select a model which expands as much as possible
        model = CTree.newCART()
                .maxDepth.set(Integer.MAX_VALUE)
                .minCount.set(1)
                .minGain.set(0.0)
                .pruning.set(Pruning.None)
                .fit(iris, "class");
    }

    @Test
    void noPruningTest() {
        int initCountNodes = model.countNodes(false);
        Pruning.None.prune(model, iris);
        int afterCountNodes = model.countNodes(false);

        assertEquals(initCountNodes, afterCountNodes);
    }

    @Test
    void reducedErrorPruningTest() {
        int initCountNodes = model.countNodes(false);
        Pruning.ReducedError.prune(model, iris);
        int afterCountNodes = model.countNodes(false);

        assertTrue(initCountNodes > afterCountNodes);
    }
}
