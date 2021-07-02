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

package rapaio.experiment.ml.common.predicate;

import rapaio.data.Frame;
import rapaio.printer.Format;

import java.io.Serial;
import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/21/17.
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
}

final class All implements RowPredicate {

    @Serial
    private static final long serialVersionUID = -3530613310623768690L;

    @Override
    public boolean test(int row, Frame df) {
        return true;
    }

    @Override
    public String toString() {
        return "all";
    }
}

record NumLessEqual(String testName, double testValue) implements RowPredicate {

    @Serial
    private static final long serialVersionUID = 8215441575970091295L;

    @Override
    public boolean test(int row, Frame df) {
        return df.getDouble(row, testName) <= testValue;
    }

    @Override
    public String toString() {
        return testName + "<=" + Format.floatFlex(testValue);
    }
}

record NumGreaterEqual(String testName, double testValue) implements RowPredicate {

    @Serial
    private static final long serialVersionUID = 8904590203760623732L;

    @Override
    public boolean test(int row, Frame df) {
        if (df.isMissing(row, testName))
            return false;
        return df.getDouble(row, testName) >= testValue;
    }

    @Override
    public String toString() {
        return testName + ">=" + Format.floatFlex(testValue);
    }
}

record NumLess(String testName, double testValue) implements RowPredicate {

    @Serial
    private static final long serialVersionUID = -8274469785632211359L;

    @Override
    public boolean test(int row, Frame df) {
        if (df.isMissing(row, testName))
            return false;
        double value = df.getDouble(row, testName);
        return value < testValue;
    }

    @Override
    public String toString() {
        return testName + "<" + Format.floatFlex(testValue);
    }
}

record NumGreater(String testName, double testValue) implements RowPredicate {

    @Serial
    private static final long serialVersionUID = 5664720893373938432L;

    @Override
    public boolean test(int row, Frame df) {
        if (df.isMissing(row, testName))
            return false;
        double value = df.getDouble(row, testName);
        return value > testValue;
    }

    @Override
    public String toString() {
        return testName + ">" + Format.floatFlex(testValue);
    }
}

record BinaryEqual(String testName, boolean testValue) implements RowPredicate {

    @Serial
    private static final long serialVersionUID = 830863153933290391L;

    @Override
    public boolean test(int row, Frame df) {
        if (df.isMissing(row, testName))
            return false;
        return df.getInt(row, testName) == (testValue ? 1 : 0);
    }

    @Override
    public String toString() {
        return testName + "=" + (testValue ? 1 : 0);
    }
}

record BinaryNotEqual(String testName, boolean testValue) implements RowPredicate {

    @Serial
    private static final long serialVersionUID = 830863153933290391L;

    @Override
    public boolean test(int row, Frame df) {
        if (df.isMissing(row, testName))
            return false;
        return df.getInt(row, testName) != (testValue ? 1 : 0);
    }

    @Override
    public String toString() {
        return testName + "!=" + (testValue ? 1 : 0);
    }
}

record NominalEqual(String testName, String testValue) implements RowPredicate {

    @Serial
    private static final long serialVersionUID = -148943086245103236L;

    @Override
    public boolean test(int row, Frame df) {
        if (df.isMissing(row, testName))
            return false;
        return df.getLabel(row, testName).equals(testValue);
    }

    @Override
    public String toString() {
        return testName + "='" + testValue + "'";
    }
}

record NominalNotEqual(String testName, String testValue) implements RowPredicate {

    @Serial
    private static final long serialVersionUID = -148943086245103236L;

    @Override
    public boolean test(int row, Frame df) {
        if (df.isMissing(row, testName))
            return false;
        return !df.getLabel(row, testName).equals(testValue);
    }

    @Override
    public String toString() {
        return testName + "!='" + testValue + "'";
    }
}
