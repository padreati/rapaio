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

package rapaio.data;

import rapaio.data.solid.SolidVarDouble;
import rapaio.data.solid.SolidVarFloat;

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
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public enum VarType {

    /**
     * Numeric values stored on 1 bit, encodes also
     * boolean values. Possible values are 0,1 or true,false.
     */
    BOOLEAN(true, false, true, "binary") {
        @Override
        public Var newInstance(int rows) {
            return VarBoolean.empty(rows);
        }
    },
    /**
     * Integer values on 32 bits
     */
    INT(true, false, false, "int") {
        @Override
        public Var newInstance(int rows) {
            return VarInt.empty(rows);
        }
    },
    SHORT(true, false, false, "short") {
        @Override
        public Var newInstance(int rows) {
            return VarShort.empty(rows);
        }
    },
    /**
     * Unordered categories: has label representation and
     * also positive integer representation.
     */
    NOMINAL(false, true, false, "nominal") {
        @Override
        public Var newInstance(int rows) {
            return VarNominal.empty(rows);
        }
    },
    /**
     * Numeric values stored in double precision
     */
    DOUBLE(true, false, false, "double") {
        @Override
        public Var newInstance(int rows) {
            return SolidVarDouble.empty(rows);
        }
    },
    FLOAT(true, false, false, "float") {
        @Override
        public Var newInstance(int rows) {
            return SolidVarFloat.empty(rows);
        }
    },
    /**
     * Ordered categories: has label representation and
     * also positive integer representation, comparison
     * on numeric representation is allowed
     */
    ORDINAL(false, true, false, "ordinal") {
        @Override
        public Var newInstance(int rows) {
            return VarOrdinal.empty(rows);
        }
    },
    /**
     * Long integer values.
     */
    LONG(false, false, false, "long") {
        @Override
        public Var newInstance(int rows) {
            return VarLong.empty(rows);
        }
    },
    /**
     * Variable type used only to store text.
     */
    TEXT(false, false, false, "text") {
        @Override
        public Var newInstance(int rows) {
            return VarText.empty(rows);
        }
    };

    private final boolean numeric;
    private final boolean nominal;
    private final boolean binary;
    private final String code;

    VarType(boolean numeric, boolean nominal, boolean binary, String code) {
        this.numeric = numeric;
        this.nominal = nominal;
        this.binary = binary;
        this.code = code;
    }

    /**
     * @return true if the variable type allows numerical manipulations.
     */
    public boolean isNumeric() {
        return numeric;
    }

    /**
     * @return true if the variable represents a categorical variable
     */
    public boolean isNominal() {
        return nominal;
    }

    public boolean isBinary() {
        return binary;
    }

    public String code() {
        return code;
    }

    /**
     * Builds a new empty instance of the given type
     *
     * @return new empty instance
     */
    public Var newInstance() {
        return newInstance(0);
    }

    /**
     * Builds a new empty instance of given size
     *
     * @param rows size of the new variable
     * @return new empty instance of given size
     */
    public abstract Var newInstance(int rows);
}
