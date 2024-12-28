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

package rapaio.io.atom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class TextAtomProtocol {

    public static AtomOutputStream outputToFile(File file) throws IOException {
        return new BinaryAtomOutputStream(file);
    }

    public static AtomOutputStream outputToStream(OutputStream stream) throws IOException {
        return new BinaryAtomOutputStream(stream);
    }

    private static class BinaryAtomOutputStream implements AtomOutputStream {

        private final Writer out;

        public BinaryAtomOutputStream(File file) throws IOException {
            this.out = new BufferedWriter(new FileWriter(file));
        }

        public BinaryAtomOutputStream(OutputStream out) {
            this.out = new BufferedWriter(new OutputStreamWriter(out));
        }

        @Override
        public void close() throws IOException {
            out.close();
        }

        @Override
        public void saveByte(byte value) throws IOException {
            out.write("byte: " + value + "\n");
        }

        @Override
        public void saveInt(int value) throws IOException {
            out.write("int: " + value + "\n");
        }

        @Override
        public void saveFloat(float value) throws IOException {
            out.write("float: " + value + "\n");
        }

        @Override
        public void saveDouble(double value) throws IOException {
            out.write("double: " + value + "\n");
        }

        @Override
        public void saveString(String value) throws IOException {
            out.write("string: " + value + "\n");
        }

        @Override
        public void saveBytes(byte[] bytes) throws IOException {
            out.write("byte[" + bytes.length + "]: " + Arrays.toString(bytes) + "\n");
        }

        @Override
        public void saveInts(int[] array) throws IOException {
            out.write("int[" + array.length + "]: " + Arrays.toString(array) + "\n");
        }

        @Override
        public void saveFloats(float[] array) throws IOException {
            out.write("float[" + array.length + "]: " + Arrays.toString(array) + "\n");
        }

        @Override
        public void saveDoubles(double[] array) throws IOException {
            out.write("double[" + array.length + "]: " + Arrays.toString(array) + "\n");
        }

        @Override
        public void saveAtom(Object atom) throws IOException {
            var serializationHandler = AtomRegistry.instance().getSerializationHandler(atom.getClass());
            if (serializationHandler == null) {
                throw new IllegalArgumentException("Unknown serialization handler for type: " + atom.getClass().getCanonicalName());
            }
            out.write("begin atom ref: " + atom + "\n");
            serializationHandler.saveAtomHandler().saveAtom(atom, this);
            out.write("end atom ref: " + atom + "\n");
        }

        @Override
        public void saveAtoms(List<?> atoms) throws IOException {
            for (var atom : atoms) {
                var serializationHandler = AtomRegistry.instance().getSerializationHandler(atom.getClass());
                if (serializationHandler == null) {
                    throw new IllegalArgumentException("Unknown save handler for type: " + atom.getClass().getCanonicalName());
                }
                out.write("begin atom ref: " + atom + "\n");
                serializationHandler.saveAtomHandler().saveAtom(atom, this);
                out.write("end atom ref: " + atom + "\n");
            }
        }

    }
}
