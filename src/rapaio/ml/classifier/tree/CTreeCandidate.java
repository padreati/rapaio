/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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

package rapaio.ml.classifier.tree;

import rapaio.data.stream.FSpot;
import rapaio.ml.common.predicate.RowPredicate;
import rapaio.util.func.SPredicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public class CTreeCandidate implements Comparable<CTreeCandidate>, Serializable {

    private static final long serialVersionUID = -1547847207988912332L;

    private final double score;
    private final String testName;
    private final List<RowPredicate> groupPredicates = new ArrayList<>();

    public CTreeCandidate(double score, String testName) {
        this.score = score;
        this.testName = testName;
    }

    public void addGroup(RowPredicate predicate) {
        groupPredicates.add(predicate);
    }

    public List<RowPredicate> getGroupPredicates() {
        return groupPredicates;
    }

    public double getScore() {
        return score;
    }

    public String getTestName() {
        return testName;
    }

    @Override
    public int compareTo(CTreeCandidate o) {
        if (o == null) return 1;
        return -(Double.compare(score, o.score));
    }
}
