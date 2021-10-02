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

package rapaio.ml.regression.tree.rtree;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.experiment.ml.common.predicate.RowPredicate;
import rapaio.ml.loss.Loss;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/2/17.
 */
public class Node implements Serializable {

    @Serial
    private static final long serialVersionUID = 385363626560575837L;

    public final Node parent;
    public final int id;
    public final String groupName;
    public final RowPredicate predicate;
    public final int depth;


    public boolean leaf = true;
    public double value;
    public double weight;

    public final List<Node> children = new ArrayList<>();
    public Candidate bestCandidate;

    public Node(Node parent, int id, String groupName, RowPredicate predicate, int depth) {
        this.parent = parent;
        this.id = id;
        this.groupName = groupName;
        this.predicate = predicate;
        this.depth = depth;
    }

    public void boostUpdate(Frame x, Var y, Var fx, Loss loss, Splitter splitter) {
        if (leaf) {
            value = loss.additiveScalarMinimizer(y, fx);
            return;
        }

        List<RowPredicate> groupPredicates = new ArrayList<>();
        for (Node child : children) {
            groupPredicates.add(child.predicate);
        }

        List<Mapping> mappings = splitter.performSplitMapping(x, VarDouble.fill(x.rowCount(), 1), groupPredicates);

        for (int i = 0; i < children.size(); i++) {
            children.get(i).boostUpdate(
                    x.mapRows(mappings.get(i)),
                    y.mapRows(mappings.get(i)),
                    fx.mapRows(mappings.get(i)),
                    loss, splitter);
        }
    }
}
