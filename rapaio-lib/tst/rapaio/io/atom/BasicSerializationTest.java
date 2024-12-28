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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BasicSerializationTest {

    @TempDir
    Path tempDir;

    @Test
    void testBasicBinarySerialization() throws IOException {
        Path tempFile = tempDir.resolve("temp.txt");
        AtomRegistry.instance().register(Point.Serialization.class);

        List<Point> points = List.of(new Point(1, 1), new Point(2, 2));
        try (AtomOutputStream out = BinaryAtomProtocol.outputToFile(tempFile.toFile())) {
            out.saveAtoms(points);
        }

        List<Point> deserializedPoints = new ArrayList<>();
        try (AtomInputStream is = BinaryAtomProtocol.inputFromFile(tempFile.toFile())) {
            deserializedPoints.add(is.loadAtom(Point.class));
            deserializedPoints.add(is.loadAtom(Point.class));
        }

        for (int i = 0; i < deserializedPoints.size(); i++) {
            Point p1 = deserializedPoints.get(i);
            Point p2 = points.get(i);
            assertNotNull(p1);
            assertEquals(p1.x, p2.x);
            assertEquals(p1.y, p2.y);
        }
    }

    @Test
    void testBasicTextOutputSerialization() throws Exception {
        AtomRegistry.instance().register(Point.Serialization.class);

        List<Point> points = List.of(new Point(1, 1), new Point(2, 2));
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             AtomOutputStream out = TextAtomProtocol.outputToStream(baos)) {
            out.saveAtoms(points);
            System.out.println(baos);
        }
        assertTrue(true);
    }

    record Point(double x, double y) {

        static class Serialization extends AtomSerialization<Point> {

            public Serialization() {}

            @Override
            public LoadAtomHandler<Point> loadAtomHandler() {
                return (in, _) -> {
                    double x = in.readDouble();
                    double y = in.readDouble();
                    return new Point(x, y);
                };
            }

            @Override
            public SaveAtomHandler<Point> saveAtomHandler() {
                return (atom, out) -> {
                    if (atom instanceof Point p) {
                        out.saveDouble(p.x);
                        out.saveDouble(p.y);
                    }
                };
            }
        }
    }
}
