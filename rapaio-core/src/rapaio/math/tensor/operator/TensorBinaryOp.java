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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.tensor.operator;

import jdk.incubator.vector.VectorOperators;

public abstract class TensorBinaryOp {

    public static final TensorBinaryOp ADD = new OpAdd();

    public static final TensorBinaryOp SUB = new OpSub();

    public static final TensorBinaryOp MUL = new OpMul();

    public static final TensorBinaryOp DIV = new OpDiv();

    public abstract VectorOperators.Binary vop();

    public abstract double applyDouble(double a, double b);

    public abstract float applyFloat(float a, float b);

    public abstract int applyInt(int a, int b);

    private static class OpAdd extends TensorBinaryOp {

        @Override
        public VectorOperators.Binary vop() {
            return VectorOperators.ADD;
        }

        @Override
        public double applyDouble(double v, double a) {
            return v + a;
        }

        @Override
        public float applyFloat(float v, float a) {
            return v + a;
        }

        @Override
        public int applyInt(int a, int b) {
            return a + b;
        }
    }

    private static class OpSub extends TensorBinaryOp {

        @Override
        public VectorOperators.Binary vop() {
            return VectorOperators.SUB;
        }

        @Override
        public double applyDouble(double v, double a) {
            return v - a;
        }

        @Override
        public float applyFloat(float v, float a) {
            return v - a;
        }

        @Override
        public int applyInt(int a, int b) {
            return a - b;
        }
    }

    private static class OpMul extends TensorBinaryOp {

        @Override
        public VectorOperators.Binary vop() {
            return VectorOperators.MUL;
        }

        @Override
        public double applyDouble(double v, double a) {
            return v * a;
        }

        @Override
        public float applyFloat(float v, float a) {
            return v * a;
        }

        @Override
        public int applyInt(int a, int b) {
            return a * b;
        }
    }

    private static class OpDiv extends TensorBinaryOp {

        @Override
        public VectorOperators.Binary vop() {
            return VectorOperators.DIV;
        }

        @Override
        public double applyDouble(double v, double a) {
            return v / a;
        }

        @Override
        public float applyFloat(float v, float a) {
            return v / a;
        }

        @Override
        public int applyInt(int a, int b) {
            return a / b;
        }
    }
}
