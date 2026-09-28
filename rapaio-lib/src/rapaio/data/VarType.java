/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.data;

import rapaio.util.function.SFunction;

/**
 * Represents the type of variable.
 * <p>
 * A variable holds data of a certain Java type. However, the Java type of the stored data
 * does not characterize completely the behavior of the variable. This happens because
 * the statistical procedures and machine learning algorithms uses variable values
 * in different ways, depending on the meaning of the data.
 * <p>
 * Suppose a given variable X has 3 possible values: 1, 2, 3. All these values
 * are integer numbers. As a consequence, one can consider a numerical
 * representation to be a best predict for this variable. However, it is also
 * possible that those values to be the result of a category encoding
 * where the textual corresponding labels are: low, medium and high.
 * Thus, values like 1.5 are not possible under this meaning. And even if we
 * consider that there is an order on those values, a possible set of
 * categories could have labels for red, green and blue. In the latter case
 * no ordering makes sense so operations like average are meaningless.
 * <p>
 * Thus a variable has a type which defines the Java types used for
 * storing values and operations allowed on the values of that variable.
 * What operations are allowed depends on the programs which uses variables,
 * however some hints are provided also by type class.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public enum VarType {

    /**
     * Numeric values stored on 1 bit, encodes also
     * boolean values. Possible values are 0,1 or true,false.
     */
    BINARY("bin", VarBinary::empty),
    /**
     * Integer values on 32 bits
     */
    INT("int", VarInt::empty),
    /**
     * Unordered categories: has label representation and
     * also positive integer representation.
     */
    NOMINAL("nom", VarNominal::empty),
    /**
     * Numeric values stired in simple precision
     */
    FLOAT("flt", VarFloat::empty),
    /**
     * Numeric values stored in double precision
     */
    DOUBLE("dbl", VarDouble::empty),
    /**
     * Long integer values.
     */
    LONG("long", VarLong::empty),
    /**
     * Instant values
     */
    INSTANT("instant", VarInstant::empty),
    /**
     * Variable type used only to store text.
     */
    STRING("str", VarString::empty);

    private final String code;
    private final SFunction<Integer, ? extends Var> newInstanceFunction;

    VarType(String code, SFunction<Integer, ? extends Var> newInstanceFunction) {
        this.code = code;
        this.newInstanceFunction = newInstanceFunction;
    }

    public String code() {
        return code;
    }

    /**
     * Tells if this type has a numeric representation, that is if {@code getDouble} / {@code getLong} carry
     * the value's meaning rather than an encoding of it. See {@link #isNumeric(VarType)}.
     */
    public boolean isNumeric() {
        return isNumeric(this);
    }

    /**
     * Tells if this type stores values as floating point numbers. See {@link #isFloatingPoint(VarType)}.
     */
    public boolean isFloatingPoint() {
        return isFloatingPoint(this);
    }

    public boolean isNominal() {
        return isNominal(this);
    }

    /**
     * Builds a new empty empty variable of the given type
     *
     * @return new empty variable of the given type
     */
    public Var newInstance() {
        return newInstanceFunction.apply(0);
    }

    /**
     * Builds a new empty instance of given type and size
     *
     * @param rows size of the new variable
     * @return new empty instance of given size
     */
    public Var newInstance(int rows) {
        return newInstanceFunction.apply(rows);
    }

    /**
     * Tells if values of the given type have a numeric meaning, so that reading them through
     * {@code getDouble} or {@code getLong} yields the value itself rather than an encoding of it.
     * <p>
     * True for {@link #DOUBLE}, {@link #FLOAT}, {@link #INT}, {@link #LONG} and {@link #BINARY}. Notably
     * <b>false</b> for {@link #INSTANT}: an instant does have a numeric representation (epoch milliseconds)
     * but arithmetic on it is not meaningful in the way it is for a measurement, and false for
     * {@link #NOMINAL}, whose integer view is a category index.
     * <p>
     * This says nothing about whether an arbitrary real number can be <i>stored</i> back into such a
     * variable: writing {@code 0.5} into an {@link #INT} or {@link #LONG} variable truncates. Use
     * {@link #isFloatingPoint(VarType)} for that question.
     */
    public static boolean isNumeric(VarType type) {
        return switch (type) {
            case DOUBLE, FLOAT, INT, LONG, BINARY -> true;
            case NOMINAL, INSTANT, STRING -> false;
        };
    }

    /**
     * Tells if the given type stores values as floating point numbers, so that an arbitrary real number can
     * be written into it without truncation. True for {@link #DOUBLE} and {@link #FLOAT} only.
     */
    public static boolean isFloatingPoint(VarType type) {
        return switch (type) {
            case DOUBLE, FLOAT -> true;
            case INT, LONG, BINARY, NOMINAL, INSTANT, STRING -> false;
        };
    }

    public static boolean isNominal(VarType type) {
        return switch (type) {
            case NOMINAL -> true;
            case DOUBLE, FLOAT, INT, LONG, BINARY, INSTANT, STRING -> false;
        };
    }
}
