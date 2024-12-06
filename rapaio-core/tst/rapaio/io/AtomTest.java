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

package rapaio.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import rapaio.darray.DType;
import rapaio.darray.Shape;
import rapaio.darray.Storage;
import rapaio.darray.StorageManager;
import rapaio.darray.layout.StrideLayout;
import rapaio.darray.storage.array.ByteArrayStorage;
import rapaio.darray.storage.array.DoubleArrayStorage;
import rapaio.darray.storage.array.FloatArrayStorage;
import rapaio.darray.storage.array.IntArrayStorage;
import rapaio.io.atom.AtomOutputStream;
import rapaio.io.atom.BinaryAtomProtocol;

public class AtomTest {

    @TempDir
    public Path tempDir;

    @Test
    void testSerializeNArray() throws IOException {

        Shape shape1 = Shape.of(1, 2, 3);

        StrideLayout scalarLayout1 = StrideLayout.of(Shape.of(), 3, new int[] {});
        StrideLayout vectorLayout1 = StrideLayout.of(Shape.of(5), 3, new int[] {1});
        StrideLayout matrixLayout1 = StrideLayout.of(Shape.of(7, 5), 3, new int[] {1, 2});
        StrideLayout tensorLayout1 = StrideLayout.of(Shape.of(7, 5, 12, 3), 8, new int[] {10, 2, 7, 2});

        Storage byteStorage1 = StorageManager.array().from(DType.BYTE, (byte) 1, (byte) 3, (byte) 5);
        Storage intStorage1 = StorageManager.array().from(DType.INTEGER, 1, 2, 3);
        Storage floatStorage1 = StorageManager.array().from(DType.FLOAT, 0.1f, 0.2f, 0.3f);
        Storage doubleStorage1 = StorageManager.array().from(DType.DOUBLE, 0.2, 0.4, 0.6);

        try (AtomOutputStream out = BinaryAtomProtocol.outputToFile(tempDir.resolve("shape.atom").toFile())) {
            out.saveAtom(shape1);
            out.saveAtom(scalarLayout1);
            out.saveAtoms(List.of(vectorLayout1, matrixLayout1, tensorLayout1));
            out.saveAtoms(List.of(byteStorage1, intStorage1, floatStorage1, doubleStorage1));
        }

        try (var in = BinaryAtomProtocol.inputFromFile(tempDir.resolve("shape.atom").toFile())) {

            Shape shape2 = in.loadAtom(Shape.class);

            StrideLayout scalarLayout2 = in.loadAtom(StrideLayout.class);
            StrideLayout vectorLayout2 = in.loadAtom(StrideLayout.class);
            StrideLayout matrixLayout2 = in.loadAtom(StrideLayout.class);
            StrideLayout tensorLayout2 = in.loadAtom(StrideLayout.class);

            Storage byteStorage2 = in.loadAtom(Storage.class);
            Storage intStorage2 = in.loadAtom(Storage.class);
            Storage floatStorage2 = in.loadAtom(Storage.class);
            Storage doubleStorage2 = in.loadAtom(Storage.class);

            assertEquals(shape1, shape2);
            assertEquals(scalarLayout1, scalarLayout2);
            assertEquals(vectorLayout1, vectorLayout2);
            assertEquals(matrixLayout1, matrixLayout2);
            assertEquals(tensorLayout1, tensorLayout2);

            assertEquals(byteStorage1.dtype(), byteStorage2.dtype());
            assertArrayEquals(((ByteArrayStorage) byteStorage1).array(), ((ByteArrayStorage) byteStorage2).array());
            assertEquals(intStorage1.dtype(), intStorage2.dtype());
            assertArrayEquals(((IntArrayStorage) intStorage1).array(), ((IntArrayStorage) intStorage2).array());
            assertEquals(floatStorage1.dtype(), floatStorage2.dtype());
            assertArrayEquals(((FloatArrayStorage) floatStorage1).array(), ((FloatArrayStorage) floatStorage2).array());
            assertEquals(doubleStorage1.dtype(), doubleStorage2.dtype());
            assertArrayEquals(((DoubleArrayStorage) doubleStorage1).array(), ((DoubleArrayStorage) doubleStorage2).array());

        }
    }
}
