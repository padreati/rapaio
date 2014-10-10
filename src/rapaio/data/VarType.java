/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.data;

/**
 * Represents the type of variable and some accepted categories.
 * <p>
 * A variable holds data of a certain Java type. However, the type of the stored data
 * does not characterize completely the behavior of the variable. This happens because
 * the statistical procedures and machine learning algorithms uses variable values
 * in different ways, depending on the meaning of the data.
 * <p>
 * Suppose a given variable X has 3 possible values: 1, 2, 3. All these values
 * are integer numbers. As a consequence, one can consider a numerical
 * representation to be a best fit for this variable. However, it is not
 * possible that these values to be the result of an encoding of a category
 * where the textual corresponding labels are: low, medium and high.
 * Thus values like 1.5 are not possible under this meaning. And even if we
 * consider that there is an order on those values, a possible set of
 * categories could have labels for red, green and blue. In the latter case
 * no ordering makes sense so operations like average are meaningless.
 * <p>
 * Thus a variable has a type which defines also the Java types used for
 * storing values and operations allowed on the values of that variable.
 * What operations are allowed depends on the programs which uses variables,
 * however some hints are provided also by type class.
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public enum VarType {
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
        public Var newInstance() {
            return Numeric.newEmpty();
        }

        @Override
        public Var newInstance(int rows) {
            return Numeric.newEmpty(rows);
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
        public Var newInstance() {
            return Index.newEmpty();
        }

        @Override
        public Var newInstance(int rows) {
            return Index.newEmpty(rows);
        }
    },
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
        public Var newInstance() {
            return Stamp.newEmpty();
        }

        @Override
        public Var newInstance(int rows) {
            return Stamp.newEmpty(rows);
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
        public Var newInstance() {
            return Nominal.newEmpty();
        }

        @Override
        public Var newInstance(int rows) {
            return Nominal.newEmpty(rows);
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
        public Var newInstance() {
            return Ordinal.newEmpty();
        }

        @Override
        public Var newInstance(int rows) {
            return Ordinal.newEmpty(rows);
        }
    },
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
        public Var newInstance() {
            return Binary.newEmpty();
        }

        @Override
        public Var newInstance(int rows) {
            return Binary.newEmpty(rows);
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
            return Text.newEmpty();
        }

        @Override
        public Var newInstance(int rows) {
            return Text.newEmpty(rows);
        }

        @Override
        public boolean isNumeric() {
            return false;
        }
    };

    public abstract boolean isNumeric();

    public abstract boolean isNominal();

    public abstract Var newInstance();

    public abstract Var newInstance(int rows);
}
