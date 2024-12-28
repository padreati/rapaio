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

package rapaio.darray;

import java.util.Comparator;
import java.util.Objects;

import jdk.incubator.vector.VectorSpecies;

public abstract class DType<N extends Number> {

    public static final DType<Byte> BYTE = new DTypeByte();
    public static final DType<Integer> INTEGER = new DTypeInteger();
    public static final DType<Float> FLOAT = new DTypeFloat();
    public static final DType<Double> DOUBLE = new DTypeDouble();

    public static DType<?> fromId(String id) {
        if(id == null) {
            throw new IllegalArgumentException("id is null");
        }
        if(id.equalsIgnoreCase("double")) {
            return DOUBLE;
        }
        if(id.equalsIgnoreCase("float")) {
            return FLOAT;
        }
        if(id.equalsIgnoreCase("int")) {
            return INTEGER;
        }
        if(id.equalsIgnoreCase("byte")) {
            return BYTE;
        }
        throw new IllegalArgumentException("Unknown dtype: " + id);
    }

    public enum Id {
        BYTE,
        INTEGER,
        FLOAT,
        DOUBLE
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

    public abstract <M extends Number> N cast(M value);

    public abstract N cast(byte value);

    public abstract N cast(int value);

    public abstract N cast(float value);

    public abstract N cast(double value);

    public abstract boolean isNaN(N value);

    public abstract Comparator<N> naturalComparator();

    public abstract Comparator<N> reverseComparator();

    public abstract VectorSpecies<N> vs();

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

    private static final class DTypeByte extends DType<Byte> {

        public DTypeByte() {
            super(Id.BYTE, (byte) 4, true);
        }

        @Override
        public <M extends Number> Byte cast(M value) {
            return value.byteValue();
        }

        @Override
        public Byte cast(byte value) {
            return value;
        }

        @Override
        public Byte cast(int value) {
            return (byte) value;
        }

        @Override
        public Byte cast(float value) {
            return (byte) value;
        }

        @Override
        public Byte cast(double value) {
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

        @Override
        public VectorSpecies<Byte> vs() {
            return Simd.vsb;
        }
    }

    private static final class DTypeInteger extends DType<Integer> {

        public DTypeInteger() {
            super(Id.INTEGER, (byte) 4, true);
        }

        @Override
        public <M extends Number> Integer cast(M value) {
            return value.intValue();
        }

        @Override
        public Integer cast(byte value) {
            return (int) value;
        }

        @Override
        public Integer cast(int value) {
            return value;
        }

        @Override
        public Integer cast(float value) {
            return (int) value;
        }

        @Override
        public Integer cast(double value) {
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

        @Override
        public VectorSpecies<Integer> vs() {
            return Simd.vsi;
        }
    }

    private static final class DTypeFloat extends DType<Float> {

        public DTypeFloat() {
            super(Id.FLOAT, (byte) 4, false);
        }

        @Override
        public <M extends Number> Float cast(M value) {
            return value.floatValue();
        }

        @Override
        public Float cast(byte value) {
            return (float) value;
        }

        @Override
        public Float cast(int value) {
            return (float) value;
        }

        @Override
        public Float cast(float value) {
            return value;
        }

        @Override
        public Float cast(double value) {
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

        @Override
        public VectorSpecies<Float> vs() {
            return Simd.vsf;
        }
    }

    private static final class DTypeDouble extends DType<Double> {

        public DTypeDouble() {
            super(Id.DOUBLE, (byte) 8, false);
        }

        @Override
        public <M extends Number> Double cast(M value) {
            return value.doubleValue();
        }

        @Override
        public Double cast(byte value) {
            return (double) value;
        }

        @Override
        public Double cast(int value) {
            return (double) value;
        }

        @Override
        public Double cast(float value) {
            return (double) value;
        }

        @Override
        public Double cast(double value) {
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

        @Override
        public VectorSpecies<Double> vs() {
            return Simd.vsd;
        }
    }
}
