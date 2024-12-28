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

package rapaio.ml.model.tree;

import java.io.Serializable;
import java.util.Set;

import rapaio.data.Frame;
import rapaio.ml.model.tree.rowpredicate.All;
import rapaio.ml.model.tree.rowpredicate.BinaryEqual;
import rapaio.ml.model.tree.rowpredicate.BinaryNotEqual;
import rapaio.ml.model.tree.rowpredicate.NominalEqual;
import rapaio.ml.model.tree.rowpredicate.NominalInSet;
import rapaio.ml.model.tree.rowpredicate.NominalNotEqual;
import rapaio.ml.model.tree.rowpredicate.NominalNotInSet;
import rapaio.ml.model.tree.rowpredicate.NumGreater;
import rapaio.ml.model.tree.rowpredicate.NumGreaterEqual;
import rapaio.ml.model.tree.rowpredicate.NumLess;
import rapaio.ml.model.tree.rowpredicate.NumLessEqual;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/21/17.
 */
@FunctionalInterface
public interface RowPredicate extends Serializable {

    /**
     * Tests if a row from a frame evaluate this predicate to true.
     *
     * @param row row number
     * @param df  data frame
     * @return result
     */
    boolean test(int row, Frame df);

    static RowPredicate all() {
        return new All();
    }

    static RowPredicate numLessEqual(String testName, double testValue) {
        return new NumLessEqual(testName, testValue);
    }

    static RowPredicate numLess(String testName, double testValue) {
        return new NumLess(testName, testValue);
    }

    static RowPredicate numGreaterEqual(String testName, double testValue) {
        return new NumGreaterEqual(testName, testValue);
    }

    static RowPredicate numGreater(String testName, double testValue) {
        return new NumGreater(testName, testValue);
    }

    static RowPredicate binEqual(String testName, boolean testValue) {
        return new BinaryEqual(testName, testValue);
    }

    static RowPredicate binNotEqual(String testName, boolean testValue) {
        return new BinaryNotEqual(testName, testValue);
    }

    static RowPredicate nomEqual(String testName, String testValue) {
        return new NominalEqual(testName, testValue);
    }

    static RowPredicate nomNotEqual(String testName, String testValue) {
        return new NominalNotEqual(testName, testValue);
    }

    static RowPredicate nomInSet(String testName, Set<String> testValues) {
        return new NominalInSet(testName, testValues);
    }

    static RowPredicate nomNotInSet(String testName, Set<String> testValues) {
        return new NominalNotInSet(testName, testValues);
    }
}

