/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.ml.classifier.tree.ctree;

import lombok.RequiredArgsConstructor;
import rapaio.core.tools.DensityVector;
import rapaio.experiment.ml.common.predicate.RowPredicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/12/20.
 */
@RequiredArgsConstructor
public final class Node implements Serializable {

    private static final long serialVersionUID = -5045581827808911763L;

    public final Node parent;
    public final int id;
    public final int depth;
    public final String groupName;
    public final RowPredicate predicate;

    public final List<Node> children = new ArrayList<>();
    public boolean leaf = true;
    public DensityVector<String> density;
    public DensityVector<String> counter;
    public String bestLabel;
    public Candidate bestCandidate;

    public void cut() {
        leaf = true;
        children.clear();
    }
}
