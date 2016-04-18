/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
 * representation to be a best fit for this variable. However, it is also
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
    BINARY {
        @Override
        public boolean isNumeric() {
            return true;
        }

        @Override
        public boolean isNominal() {
            return false;
        }

        @Override
        public boolean isBinary() {
            return true;
        }

        @Override
        public String code() {
            return "bin";
        }

        @Override
        public Var newInstance() {
            return Binary.newEmpty();
        }

        @Override
        public Var newInstance(int rows) {
            return Binary.newEmpty(rows);
        }
    },
    /**
     * Integer values on 32 bits
     */
    INDEX {
        @Override
        public boolean isNumeric() {
            return true;
        }

        @Override
        public boolean isNominal() {
            return false;
        }

        @Override
        public String code() {
            return "idx";
        }

        @Override
        public Var newInstance() {
            return Index.empty();
        }

        @Override
        public Var newInstance(int rows) {
            return Index.empty(rows);
        }
    },
    /**
     * Unordered categories: has label representation and
     * also positive integer representation.
     */
    NOMINAL {
        @Override
        public boolean isNumeric() {
            return false;
        }

        @Override
        public boolean isNominal() {
            return true;
        }

        @Override
        public String code() {
            return "nom";
        }

        @Override
        public Var newInstance() {
            return Nominal.newEmpty();
        }

        @Override
        public Var newInstance(int rows) {
            return Nominal.newEmpty(rows);
        }
    },
    /**
     * Numeric values stored in double precision
     */
    NUMERIC {
        @Override
        public boolean isNumeric() {
            return true;
        }

        @Override
        public boolean isNominal() {
            return false;
        }

        @Override
        public String code() {
            return "num";
        }

        @Override
        public Var newInstance() {
            return Numeric.newEmpty();
        }

        @Override
        public Var newInstance(int rows) {
            return Numeric.newEmpty(rows);
        }
    },
    /**
     * Ordered categories: has label representation and
     * also positive integer representation, comparison
     * on numeric representation is allowed
     */
    ORDINAL {
        @Override
        public boolean isNumeric() {
            return false;
        }

        @Override
        public boolean isNominal() {
            return true;
        }

        @Override
        public String code() {
            return "ord";
        }

        @Override
        public Var newInstance() {
            return Ordinal.empty();
        }

        @Override
        public Var newInstance(int rows) {
            return Ordinal.empty(rows);
        }
    },
    /**
     * Time stamp long integer values.
     */
    STAMP {
        @Override
        public boolean isNumeric() {
            return false;
        }

        @Override
        public boolean isNominal() {
            return false;
        }

        @Override
        public String code() {
            return "dat";
        }

        @Override
        public Var newInstance() {
            return Stamp.empty();
        }

        @Override
        public Var newInstance(int rows) {
            return Stamp.empty(rows);
        }
    },
    /**
     * Variable type used only to store text.
     */
    TEXT {
        @Override
        public boolean isNominal() {
            return false;
        }

        @Override
        public Var newInstance() {
            return Text.empty();
        }

        @Override
        public String code() {
            return "txt";
        }

        @Override
        public Var newInstance(int rows) {
            return Text.empty(rows);
        }

        @Override
        public boolean isNumeric() {
            return false;
        }
    };

    /**
     * @return true if the variable type allows numerical manipulations.
     */
    public abstract boolean isNumeric();

    /**
     * @return true if the variable represents a categorical variable
     */
    public abstract boolean isNominal();

    public boolean isBinary() {
        return false;
    }

    public abstract String code();

    /**
     * Builds a new empty instance of the given type
     *
     * @return new empty instance
     */
    public abstract Var newInstance();

    /**
     * Builds a new empty instance of given size
     * @param rows size of the new variable
     * @return new empty instance of given size
     */
    public abstract Var newInstance(int rows);
}
