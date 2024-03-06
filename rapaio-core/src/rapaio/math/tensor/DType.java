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

package rapaio.math.tensor;

import java.util.Comparator;
import java.util.Objects;

public abstract sealed class DType<N extends Number> permits DType.DTypeDouble, DType.DTypeFloat, DType.DTypeInteger, DType.DTypeByte {

    public static final DType<Byte> BYTE = new DTypeByte();
    public static final DType<Integer> INTEGER = new DTypeInteger();
    public static final DType<Float> FLOAT = new DTypeFloat();
    public static final DType<Double> DOUBLE = new DTypeDouble();

    public enum Id {
        BYTE,
        INTEGER,
        FLOAT,
        DOUBLE;
    }

    private final Id id;
    private final byte byteCount;
    private final boolean isInteger;

    protected DType(Id id, byte byteCount, boolean isInteger) {
        this.id = id;
        this.byteCount = byteCount;
        this.isInteger = isInteger;
    }

    public Id id() {
        return id;
    }

    public boolean isInteger() {
        return isInteger;
    }

    public boolean floatingPoint() {
        return !isInteger;
    }

    public int byteCount() {
        return byteCount;
    }

    public abstract <M extends Number> N castValue(M value);

    public abstract N castValue(byte value);

    public abstract N castValue(int value);

    public abstract N castValue(float value);

    public abstract N castValue(double value);

    public abstract boolean isNaN(N value);

    public abstract Comparator<N> naturalComparator();

    public abstract Comparator<N> reverseComparator();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DType<?> dType)) {
            return false;
        }
        return Objects.equals(id, dType.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static final class DTypeByte extends DType<Byte> {

        public DTypeByte() {
            super(Id.BYTE, (byte) 4, true);
        }

        @Override
        public <M extends Number> Byte castValue(M value) {
            return value.byteValue();
        }

        @Override
        public Byte castValue(byte value) {
            return value;
        }

        @Override
        public Byte castValue(int value) {
            return (byte) value;
        }

        @Override
        public Byte castValue(float value) {
            return (byte) value;
        }

        @Override
        public Byte castValue(double value) {
            return (byte) value;
        }

        @Override
        public boolean isNaN(Byte value) {
            return false;
        }

        @Override
        public Comparator<Byte> naturalComparator() {
            return Comparator.naturalOrder();
        }

        @Override
        public Comparator<Byte> reverseComparator() {
            return Comparator.reverseOrder();
        }
    }

    public static final class DTypeDouble extends DType<Double> {

        public DTypeDouble() {
            super(Id.DOUBLE, (byte) 8, false);
        }

        @Override
        public <M extends Number> Double castValue(M value) {
            return value.doubleValue();
        }

        @Override
        public Double castValue(byte value) {
            return (double) value;
        }

        @Override
        public Double castValue(int value) {
            return (double) value;
        }

        @Override
        public Double castValue(float value) {
            return (double) value;
        }

        @Override
        public Double castValue(double value) {
            return value;
        }

        @Override
        public boolean isNaN(Double value) {
            return Double.isNaN(value);
        }

        @Override
        public Comparator<Double> naturalComparator() {
            return Comparator.naturalOrder();
        }

        @Override
        public Comparator<Double> reverseComparator() {
            return Comparator.reverseOrder();
        }
    }

    public static final class DTypeFloat extends DType<Float> {


        public DTypeFloat() {
            super(Id.FLOAT, (byte) 4, false);
        }

        @Override
        public <M extends Number> Float castValue(M value) {
            return value.floatValue();
        }

        @Override
        public Float castValue(byte value) {
            return (float) value;
        }

        @Override
        public Float castValue(int value) {
            return (float) value;
        }

        @Override
        public Float castValue(float value) {
            return value;
        }

        @Override
        public Float castValue(double value) {
            return (float) value;
        }

        @Override
        public boolean isNaN(Float value) {
            return Float.isNaN(value);
        }

        @Override
        public Comparator<Float> naturalComparator() {
            return Comparator.naturalOrder();
        }

        @Override
        public Comparator<Float> reverseComparator() {
            return Comparator.reverseOrder();
        }
    }

    public static final class DTypeInteger extends DType<Integer> {

        public DTypeInteger() {
            super(Id.INTEGER, (byte) 4, true);
        }

        @Override
        public <M extends Number> Integer castValue(M value) {
            return value.intValue();
        }

        @Override
        public Integer castValue(byte value) {
            return (int) value;
        }

        @Override
        public Integer castValue(int value) {
            return (int) value;
        }

        @Override
        public Integer castValue(float value) {
            return (int) value;
        }

        @Override
        public Integer castValue(double value) {
            return (int) value;
        }

        @Override
        public boolean isNaN(Integer value) {
            return false;
        }

        @Override
        public Comparator<Integer> naturalComparator() {
            return Comparator.naturalOrder();
        }

        @Override
        public Comparator<Integer> reverseComparator() {
            return Comparator.reverseOrder();
        }
    }
}
