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

package rapaio.ml.supervised.tree.rtree;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import rapaio.ml.supervised.tree.RowPredicate;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/1/17.
 */
public class Candidate implements Serializable {

    @Serial
    private static final long serialVersionUID = 6698766675237089849L;
    private final double score;
    private final String testName;
    private final List<RowPredicate> groupPredicates = new ArrayList<>();

    public Candidate(double score, String testName) {
        this.score = score;
        this.testName = testName;
    }

    public void addGroup(RowPredicate predicate) {
        if (groupPredicates.contains(predicate)) {
            throw new IllegalArgumentException("group already defined");
        }
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
    public String toString() {
        return "Candidate{" +
                "score=" + score +
                ", testName='" + testName + '\'' +
                ", predicates=[" + groupPredicates.stream().map(RowPredicate::toString).collect(Collectors.joining(", ")) +
                "]}";
    }
}