/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.supervised.tree;

import rapaio.ml.loss.Loss;
import rapaio.ml.supervised.tree.rtree.SearchPayload;

/**
 * Extended loss function which can be used to find the best split for a tree.
 * The argument contains needed information required to compute the reduce in loss after
 * the split.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/12/20.
 */
public interface RTreeLoss extends Loss {

    double computeSplitLossScore(SearchPayload payload);
}
