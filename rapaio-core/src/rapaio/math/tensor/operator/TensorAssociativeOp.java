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

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

public abstract class TensorAssociativeOp {

    public static final TensorAssociativeOp ADD = new OpAdd();
    public static final TensorAssociativeOp MUL = new OpMul();
    public static final TensorAssociativeOp MAX = new OpMax();
    public static final TensorAssociativeOp MIN = new OpMin();

    public abstract DoubleVector initialVectorDouble(VectorSpecies<Double> species);

    public abstract FloatVector initialVectorFloat(VectorSpecies<Float> species);

    public abstract VectorOperators.Associative vop();

    public abstract double initialDouble();

    public abstract double applyDouble(double a, double b);

    public abstract float initialFloat();

    public abstract float applyFloat(float a, float b);

    private static final class OpAdd extends TensorAssociativeOp {

        @Override
        public DoubleVector initialVectorDouble(VectorSpecies<Double> species) {
            return DoubleVector.zero(species);
        }

        @Override
        public FloatVector initialVectorFloat(VectorSpecies<Float> species) {
            return FloatVector.zero(species);
        }

        @Override
        public VectorOperators.Associative vop() {
            return VectorOperators.ADD;
        }

        @Override
        public double initialDouble() {
            return 0;
        }

        @Override
        public double applyDouble(double a, double b) {
            return a + b;
        }

        @Override
        public float initialFloat() {
            return 0f;
        }

        @Override
        public float applyFloat(float a, float b) {
            return a + b;
        }
    }

    private static final class OpMul extends TensorAssociativeOp {

        @Override
        public DoubleVector initialVectorDouble(VectorSpecies<Double> species) {
            return DoubleVector.broadcast(species, 1);
        }

        @Override
        public FloatVector initialVectorFloat(VectorSpecies<Float> species) {
            return FloatVector.broadcast(species, 1);
        }

        @Override
        public VectorOperators.Associative vop() {
            return VectorOperators.MUL;
        }

        @Override
        public double initialDouble() {
            return 1;
        }

        @Override
        public double applyDouble(double a, double b) {
            return a * b;
        }

        @Override
        public float initialFloat() {
            return 1f;
        }

        @Override
        public float applyFloat(float a, float b) {
            return a * b;
        }
    }

    private static final class OpMax extends TensorAssociativeOp {

        @Override
        public DoubleVector initialVectorDouble(VectorSpecies<Double> species) {
            return DoubleVector.broadcast(species, Double.NEGATIVE_INFINITY);
        }

        @Override
        public FloatVector initialVectorFloat(VectorSpecies<Float> species) {
            return FloatVector.broadcast(species, Float.NEGATIVE_INFINITY);
        }

        @Override
        public VectorOperators.Associative vop() {
            return VectorOperators.MAX;
        }

        @Override
        public double initialDouble() {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        public double applyDouble(double a, double b) {
            return Math.max(a, b);
        }

        @Override
        public float initialFloat() {
            return Float.NEGATIVE_INFINITY;
        }

        @Override
        public float applyFloat(float a, float b) {
            return Math.max(a, b);
        }
    }

    private static final class OpMin extends TensorAssociativeOp {

        @Override
        public DoubleVector initialVectorDouble(VectorSpecies<Double> species) {
            return DoubleVector.broadcast(species, Double.POSITIVE_INFINITY);
        }

        @Override
        public FloatVector initialVectorFloat(VectorSpecies<Float> species) {
            return FloatVector.broadcast(species, Float.POSITIVE_INFINITY);
        }

        @Override
        public VectorOperators.Associative vop() {
            return VectorOperators.MIN;
        }

        @Override
        public double initialDouble() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public double applyDouble(double a, double b) {
            return Math.min(a, b);
        }

        @Override
        public float initialFloat() {
            return Float.POSITIVE_INFINITY;
        }

        @Override
        public float applyFloat(float a, float b) {
            return Math.min(a, b);
        }
    }
}
