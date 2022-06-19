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

package rapaio.ml.model.tree.rowpredicate;

import java.util.Set;
import java.util.stream.Collectors;

import rapaio.data.Frame;
import rapaio.data.VarNominal;
import rapaio.ml.model.tree.RowPredicate;

public record NominalNotInSet(String testName, Set<String> testValues) implements RowPredicate {

    @Override
    public boolean test(int row, Frame df) {
        if (df.isMissing(row, testName)) {
            return !testValues.contains(VarNominal.MISSING_VALUE);
        }
        return !testValues.contains(df.getLabel(row, testName));
    }

    @Override
    public String toString() {
        return testName + " not in [" + testValues.stream().map(s -> '\'' + s + '\'').collect(Collectors.joining(",")) + ']';
    }
}
