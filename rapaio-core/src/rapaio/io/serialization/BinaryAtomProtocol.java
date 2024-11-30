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

package rapaio.io.serialization;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class BinaryAtomProtocol {

    public static AtomInputStream inputFromFile(File file) throws IOException {
        return new BinaryAtomInputStream(file);
    }

    public static AtomInputStream inputFromStream(InputStream stream) throws IOException {
        return new BinaryAtomInputStream(stream);
    }

    public static AtomOutputStream outputToFile(File file) throws IOException {
        return new BinaryAtomOutputStream(file);
    }

    public static AtomOutputStream outputToStream(OutputStream stream) throws IOException {
        return new BinaryAtomOutputStream(stream);
    }

    private static class BinaryAtomOutputStream implements AtomOutputStream {

        private final DataOutputStream out;

        public BinaryAtomOutputStream(File file) throws IOException {
            this.out = new DataOutputStream(new FileOutputStream(file));
        }

        public BinaryAtomOutputStream(OutputStream out) {
            this.out = new DataOutputStream(out);
        }

        @Override
        public void close() throws IOException {
            out.close();
        }

        @Override
        public void saveByte(byte value) throws IOException {
            out.writeByte(value);
        }

        @Override
        public void saveInt(int value) throws IOException {
            out.writeInt(value);
        }

        @Override
        public void saveFloat(float value) throws IOException {
            out.writeFloat(value);
        }

        @Override
        public void saveDouble(double value) throws IOException {
            out.writeDouble(value);
        }

        @Override
        public void saveString(String value) throws IOException {
            out.writeUTF(value);
        }

        @Override
        public void saveBytes(byte[] bytes) throws IOException {
            out.writeInt(bytes.length);
            out.write(bytes, 0, bytes.length);
        }

        @Override
        public void saveInts(int[] array) throws IOException {
            out.writeInt(array.length);
            for (int j : array) {
                out.writeInt(j);
            }
        }

        @Override
        public void saveFloats(float[] array) throws IOException {
            out.writeInt(array.length);
            for (float f : array) {
                out.writeFloat(f);
            }
        }

        @Override
        public void saveDoubles(double[] array) throws IOException {
            out.writeInt(array.length);
            for (double d : array) {
                out.writeDouble(d);
            }
        }

        @Override
        public void saveAtom(Object atom) throws IOException {
            var serializationHandler = AtomRegistry.instance().getSerializationHandler(atom.getClass());
            if(serializationHandler == null) {
                throw new IllegalArgumentException("Unknown serialization handler for type: " + atom.getClass().getCanonicalName());
            }
            serializationHandler.saveAtomHandler().saveAtom(atom, this);
        }

        @Override
        public void saveAtoms(List<?> atoms) throws IOException {
            for (var atom : atoms) {
                var serializationHandler = AtomRegistry.instance().getSerializationHandler(atom.getClass());
                if (serializationHandler == null) {
                    throw new IllegalArgumentException("Unknown save handler for type: " + atom.getClass().getCanonicalName());
                }
                serializationHandler.saveAtomHandler().saveAtom(atom, this);
            }
        }

    }

    private static class BinaryAtomInputStream implements AtomInputStream, AutoCloseable {

        private final DataInputStream inputStream;

        public BinaryAtomInputStream(File file) throws IOException {
            this.inputStream = new DataInputStream(new FileInputStream(file));
        }

        public BinaryAtomInputStream(InputStream inputStream) {
            this.inputStream = new DataInputStream(inputStream);
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
        }

        @Override
        public byte readByte() throws IOException {
            return inputStream.readByte();
        }

        @Override
        public int readInt() throws IOException {
            return inputStream.readInt();
        }

        @Override
        public float readFloat() throws IOException {
            return inputStream.readFloat();
        }

        @Override
        public double readDouble() throws IOException {
            return inputStream.readDouble();
        }

        @Override
        public String readString() throws IOException {
            return inputStream.readUTF();
        }

        @Override
        public byte[] readBytes() throws IOException {
            int len = inputStream.readInt();
            byte[] buffer = new byte[len];
            inputStream.readNBytes(buffer, 0, len);
            return buffer;
        }

        @Override
        public int[] readInts() throws IOException {
            int len = readInt();
            int[] buffer = new int[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = readInt();
            }
            return buffer;
        }

        @Override
        public float[] readFloats() throws IOException {
            int len = readInt();
            float[] buffer = new float[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = readFloat();
            }
            return buffer;
        }

        @Override
        public double[] readDoubles() throws IOException {
            int len = readInt();
            double[] buffer = new double[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = readDouble();
            }
            return buffer;
        }

        @Override
        public <T> T loadAtom(Class<T> clazz) throws IOException {
            var serializationHandler = AtomRegistry.instance().getSerializationHandler(clazz);
            return serializationHandler.loadAtomHandler().loadAtom(this);
        }
    }
}
