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

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorSpecies;
import rapaio.darray.storage.array.ByteArrayStorage;
import rapaio.darray.storage.array.DoubleArrayStorage;
import rapaio.darray.storage.array.FloatArrayStorage;
import rapaio.darray.storage.array.IntArrayStorage;
import rapaio.io.atom.AtomSerialization;
import rapaio.io.atom.LoadAtomHandler;
import rapaio.io.atom.SaveAtomHandler;

public abstract class Storage {

    public abstract DType<?> dtype();

    public abstract int size();

    public abstract boolean supportVectorization();


    public abstract byte getByte(int ptr);

    public abstract void setByte(int ptr, byte value);

    public abstract void incByte(int ptr, byte value);

    public abstract void fill(byte value, int start, int len);


    public abstract int getInt(int ptr);

    public abstract void setInt(int ptr, int value);

    public abstract void incInt(int ptr, int value);

    public abstract void fill(int value, int start, int len);


    public abstract float getFloat(int ptr);

    public abstract void setFloat(int ptr, float value);

    public abstract void incFloat(int ptr, float value);

    public abstract void fill(float value, int start, int len);


    public abstract double getDouble(int ptr);

    public abstract void setDouble(int ptr, double value);

    public abstract void incDouble(int ptr, double value);

    public abstract void fill(double value, int start, int len);


    public abstract ByteVector getByteVector(VectorSpecies<Byte> vs, int offset);

    public abstract ByteVector getByteVector(VectorSpecies<Byte> vs, int offset, int[] idx, int idxOffset);

    public abstract void setByteVector(ByteVector value, int offset);

    public abstract void setByteVector(ByteVector value, int offset, int[] idx, int idxOffset);


    public abstract IntVector getIntVector(VectorSpecies<Integer> vs, int offset);

    public abstract IntVector getIntVector(VectorSpecies<Integer> vs, int offset, int[] idx, int idxOffset);

    public abstract void setIntVector(IntVector value, int offset);

    public abstract void setIntVector(IntVector value, int offset, int[] idx, int idxOffset);


    public abstract FloatVector getFloatVector(VectorSpecies<Float> vs, int offset);

    public abstract FloatVector getFloatVector(VectorSpecies<Float> vs, int offset, int[] idx, int idxOffset);

    public abstract void setFloatVector(FloatVector value, int offset);

    public abstract void setFloatVector(FloatVector value, int offset, int[] idx, int idxOffset);


    public abstract DoubleVector getDoubleVector(VectorSpecies<Double> vs, int offset);

    public abstract DoubleVector getDoubleVector(VectorSpecies<Double> vs, int offset, int[] idx, int idxOffset);

    public abstract void setDoubleVector(DoubleVector value, int offset);

    public abstract void setDoubleVector(DoubleVector value, int offset, int[] idx, int idxOffset);


    public static class Serialization extends AtomSerialization<Storage> {

        @Override
        public LoadAtomHandler<? extends Storage> loadAtomHandler() {
            return (in, _) -> {
                String className = in.readString();
                if (ByteArrayStorage.class.getName().equals(className)) {
                    byte[] bytes = in.readBytes();
                    return new ByteArrayStorage(bytes);
                }
                if (IntArrayStorage.class.getName().equals(className)) {
                    int[] ints = in.readInts();
                    return new IntArrayStorage(ints);
                }
                if (FloatArrayStorage.class.getName().equals(className)) {
                    float[] floats = in.readFloats();
                    return new FloatArrayStorage(floats);
                }
                if (DoubleArrayStorage.class.getName().equals(className)) {
                    double[] doubles = in.readDoubles();
                    return new DoubleArrayStorage(doubles);
                }
                throw new RuntimeException("Unknown class " + className);
            };
        }

        @Override
        public SaveAtomHandler<? extends Storage> saveAtomHandler() {
            return (atom, out) -> {
                if (atom instanceof ByteArrayStorage bas) {
                    out.saveString(ByteArrayStorage.class.getName());
                    out.saveBytes(bas.array());
                    return;
                }
                if (atom instanceof IntArrayStorage ias) {
                    out.saveString(IntArrayStorage.class.getName());
                    out.saveInts(ias.array());
                    return;
                }
                if (atom instanceof FloatArrayStorage fas) {
                    out.saveString(FloatArrayStorage.class.getName());
                    out.saveFloats(fas.array());
                    return;
                }
                if (atom instanceof DoubleArrayStorage das) {
                    out.saveString(DoubleArrayStorage.class.getName());
                    out.saveDoubles(das.array());
                    return;
                }
                throw new RuntimeException("Unknown storage type: " + atom.getClass().getName());
            };
        }
    }
}
