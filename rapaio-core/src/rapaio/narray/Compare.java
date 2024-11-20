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

package rapaio.narray;

import jdk.incubator.vector.VectorOperators;

public enum Compare {

    GT(VectorOperators.GT) {
        @Override
        public boolean compareByte(byte a, byte b) {
            return a > b;
        }

        @Override
        public boolean compareInt(int a, int b) {
            return a > b;
        }

        @Override
        public boolean compareFloat(float a, float b) {
            return a > b;
        }

        @Override
        public boolean compareDouble(double a, double b) {
            return a > b;
        }
    },
    GE(VectorOperators.GE) {
        @Override
        public boolean compareByte(byte a, byte b) {
            return a >= b;
        }

        @Override
        public boolean compareInt(int a, int b) {
            return a >= b;
        }

        @Override
        public boolean compareFloat(float a, float b) {
            return a >= b;
        }

        @Override
        public boolean compareDouble(double a, double b) {
            return a >= b;
        }
    },
    EQ(VectorOperators.EQ) {
        @Override
        public boolean compareByte(byte a, byte b) {
            return a == b;
        }

        @Override
        public boolean compareInt(int a, int b) {
            return a == b;
        }

        @Override
        public boolean compareFloat(float a, float b) {
            return a == b;
        }

        @Override
        public boolean compareDouble(double a, double b) {
            return a == b;
        }
    },
    NE(VectorOperators.NE) {
        @Override
        public boolean compareByte(byte a, byte b) {
            return a != b;
        }

        @Override
        public boolean compareInt(int a, int b) {
            return a != b;
        }

        @Override
        public boolean compareFloat(float a, float b) {
            return a != b;
        }

        @Override
        public boolean compareDouble(double a, double b) {
            return a != b;
        }
    },
    LE(VectorOperators.LE) {
        @Override
        public boolean compareByte(byte a, byte b) {
            return a <= b;
        }

        @Override
        public boolean compareInt(int a, int b) {
            return a <= b;
        }

        @Override
        public boolean compareFloat(float a, float b) {
            return a <= b;
        }

        @Override
        public boolean compareDouble(double a, double b) {
            return a <= b;
        }
    },
    LT(VectorOperators.LT) {
        @Override
        public boolean compareByte(byte a, byte b) {
            return a < b;
        }

        @Override
        public boolean compareInt(int a, int b) {
            return a < b;
        }

        @Override
        public boolean compareFloat(float a, float b) {
            return a < b;
        }

        @Override
        public boolean compareDouble(double a, double b) {
            return a < b;
        }
    };

    private final VectorOperators.Comparison vectorComparison;

    Compare(VectorOperators.Comparison vectorComparison) {
        this.vectorComparison = vectorComparison;
    }

    public VectorOperators.Comparison vectorComparison() {
        return vectorComparison;
    }

    public abstract boolean compareByte(byte a, byte b);

    public abstract boolean compareInt(int a, int b);

    public abstract boolean compareFloat(float a, float b);

    public abstract boolean compareDouble(double a, double b);
}
