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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import rapaio.io.serialization.AtomOutputStream;
import rapaio.io.serialization.BinaryAtomProtocol;
import rapaio.narray.Shape;
import rapaio.narray.layout.StrideLayout;

public class AtomTest {

    @TempDir
    public Path tempDir;

    @Disabled
    @Test
    void testSerializeNArray() throws IOException {

        Shape shape1 = Shape.of(1, 2, 3);
        StrideLayout scalarLayout1 = StrideLayout.of(Shape.of(), 3, new int[] {});

        try (AtomOutputStream out = BinaryAtomProtocol.outputToFile(tempDir.resolve("shape.atom").toFile())) {
            out.saveAtom(shape1);
            out.saveAtom(scalarLayout1);
        }

        try (var in = BinaryAtomProtocol.inputFromFile(tempDir.resolve("shape.atom").toFile())) {

            Shape shape2 = in.loadAtom(Shape.class);
            StrideLayout scalarLayout2 = in.loadAtom(StrideLayout.class);

            assertEquals(shape1, shape2);
            assertEquals(scalarLayout1, scalarLayout2);
        }
    }
}
