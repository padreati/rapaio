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

package rapaio.experiment.fx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import javafx.scene.shape.Mesh;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

public class MeshImporter {


    public static Mesh loadAsciiStl(InputStream is) throws IOException {
        return loadAsciiStl(is, false);
    }

    public static Mesh loadAsciiStl(InputStream is, boolean loadNormals) throws IOException {
        TriangleMesh mesh = new TriangleMesh(loadNormals ? VertexFormat.POINT_NORMAL_TEXCOORD : VertexFormat.POINT_TEXCOORD);
        try (Scanner sc = new Scanner(new BufferedReader(new InputStreamReader(is)))) {
            String line = sc.nextLine();
            if (line == null) {
                throw new IOException("Empty file");
            }
            String[] tokens = line.split("\\s");
            if (!tokens[0].equals("solid")) {
                throw new IOException("Not an ascii stl file.");
            }

            float[] normal = new float[3];
            float[] vertices = new float[9];

            mesh.getTexCoords().addAll(0f, 0f);

            int faceCount = 0;
            while (sc.hasNext()) {

                String next = sc.next();
                if (next.equals("endsolid")) {
                    break;
                }

                if (!next.equals("facet")) {
                    throw new IllegalStateException("Expected token: " + next + ", found: " + next);
                }

                checkNextTokens(sc, "normal");

                for (int i = 0; i < 3; i++) {
                    normal[i] = Float.parseFloat(sc.next());
                }

                checkNextTokens(sc, "outer", "loop");

                for (int i = 0; i < 3; i++) {
                    checkNextTokens(sc, "vertex");
                    for (int j = 0; j < 3; j++) {
                        vertices[i * 3 + j] = Float.parseFloat(sc.next());
                    }
                }
                checkNextTokens(sc, "endloop", "endfacet");

                mesh.getPoints().addAll(vertices);
                if (loadNormals) {
                    mesh.getNormals().addAll(normal);
                }

                if (loadNormals) {
                    mesh.getFaces().addAll(faceCount * 3, faceCount, 0, faceCount * 3 + 1, faceCount, 0, faceCount * 3 + 2, faceCount, 0);
                } else {
                    mesh.getFaces().addAll(faceCount * 3, 0, faceCount * 3 + 1, 0, faceCount * 3 + 2, 0);
                }
                faceCount++;
            }
            return mesh;
        }
    }

    private static void checkNextTokens(Scanner sc, String... tokens) {
        for (String token : tokens) {
            String next = sc.next();
            if (!token.equals(next)) {
                throw new IllegalStateException("Expected token: " + token + ", found: " + next);
            }
        }
    }
}
