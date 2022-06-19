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

import java.io.Serial;

import rapaio.data.Frame;
import rapaio.ml.model.tree.RowPredicate;
import rapaio.printer.Format;

public record NumGreater(String testName, double testValue) implements RowPredicate {

    @Serial
    private static final long serialVersionUID = 5664720893373938432L;

    @Override
    public boolean test(int row, Frame df) {
        if (df.isMissing(row, testName)) {
            return false;
        }
        double value = df.getDouble(row, testName);
        return value > testValue;
    }

    @Override
    public String toString() {
        return testName + ">" + Format.floatFlex(testValue);
    }
}
