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

package rapaio.ml.model.tree.ctree;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rapaio.ml.model.tree.RowPredicate;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/12/20.
 */
public record Candidate(double score, String testName, List<RowPredicate> groupPredicates) implements Serializable {

    @Serial
    private static final long serialVersionUID = -1547847207988912332L;

    public Candidate(double score, String testName) {
        this(score, testName, new ArrayList<>());
    }

    public void addGroup(RowPredicate predicate) {
        groupPredicates.add(predicate);
    }
}
