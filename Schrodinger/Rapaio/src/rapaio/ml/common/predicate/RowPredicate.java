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

package rapaio.ml.common.predicate;

import rapaio.data.Frame;
import rapaio.data.stream.FSpot;
import rapaio.sys.WS;

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

    String toString();

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

final class NumLessEqual implements RowPredicate {

    private static final long serialVersionUID = 8215441575970091295L;
    private final String testName;
    private final double testValue;

    NumLessEqual(String testName, double testValue) {
        this.testName = testName;
        this.testValue = testValue;
    }

    @Override
    public boolean test(int row, Frame df) {
        double value = df.value(row, testName);
        return !Double.isNaN(value) && value <= testValue;
    }

    @Override
    public String toString() {
        return testName + " <= " + WS.formatFlex(testValue);
    }
}

final class NumGreaterEqual implements RowPredicate {

    private static final long serialVersionUID = 8904590203760623732L;
    public final String testName;
    public final double testValue;

    NumGreaterEqual(String testName, double testValue) {
        this.testName = testName;
        this.testValue = testValue;
    }

    @Override
    public boolean test(int row, Frame df) {
        double value = df.value(row, testName);
        return !Double.isNaN(value) && value >= testValue;
    }

    @Override
    public String toString() {
        return testName + " >= " + WS.formatFlex(testValue);
    }
}

final class NumLess implements RowPredicate {

    private static final long serialVersionUID = -8274469785632211359L;
    public final String testName;
    public final double testValue;

    NumLess(String testName, double testValue) {
        this.testName = testName;
        this.testValue = testValue;
    }

    @Override
    public boolean test(int row, Frame df) {
        double value = df.value(row, testName);
        return !Double.isNaN(value) && value < testValue;
    }

    @Override
    public String toString() {
        return testName + " < " + WS.formatFlex(testValue);
    }
}

final class NumGreater implements RowPredicate {

    private static final long serialVersionUID = 5664720893373938432L;
    public final String testName;
    public final double testValue;

    NumGreater(String testName, double testValue) {
        this.testName = testName;
        this.testValue = testValue;
    }

    @Override
    public boolean test(int row, Frame df) {
        double value = df.value(row, testName);
        return !Double.isNaN(value) && value > testValue;
    }

    @Override
    public String toString() {
        return testName + " > " + WS.formatFlex(testValue);
    }
}

final class BinaryEqual implements RowPredicate {

    private static final long serialVersionUID = 830863153933290391L;

    private final String testName;
    private final boolean testValue;

    public BinaryEqual(String testName, boolean testValue) {
        this.testName = testName;
        this.testValue = testValue;
    }

    @Override
    public boolean test(int row, Frame df) {
        return !df.isMissing(row) && df.binary(row, testName) == testValue;
    }

    @Override
    public String toString() {
        return testName + " = " + testValue;
    }
}

final class BinaryNotEqual implements RowPredicate {

    private static final long serialVersionUID = 830863153933290391L;

    private final String testName;
    private final boolean testValue;

    public BinaryNotEqual(String testName, boolean testValue) {
        this.testName = testName;
        this.testValue = testValue;
    }

    @Override
    public boolean test(int row, Frame df) {
        return !df.isMissing(row) && df.binary(row, testName) != testValue;
    }

    @Override
    public String toString() {
        return testName + " != " + testValue;
    }
}

final class NominalEqual implements RowPredicate {


    private static final long serialVersionUID = -148943086245103236L;
    private final String testName;
    private final String testValue;

    public NominalEqual(String testName, String testValue) {
        this.testName = testName;
        this.testValue = testValue;
    }

    @Override
    public boolean test(int row, Frame df) {
        return df.label(row, testName).equals(testValue);
    }

    @Override
    public String toString() {
        return testName + " = \'" + testValue + "\'";
    }
}

final class NominalNotEqual implements RowPredicate {


    private static final long serialVersionUID = -148943086245103236L;
    private final String testName;
    private final String testValue;

    public NominalNotEqual(String testName, String testValue) {
        this.testName = testName;
        this.testValue = testValue;
    }

    @Override
    public boolean test(int row, Frame df) {
        return !df.label(row, testName).equals(testValue);
    }

    @Override
    public String toString() {
        return testName + " != \'" + testValue + "\'";
    }
}
