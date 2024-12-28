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

package rapaio.graphics.plot.artist;

import static rapaio.graphics.Plotter.corrGram;
import static rapaio.graphics.Plotter.matrix;
import static rapaio.graphics.opt.GOpts.color;

import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.tools.DistanceMatrix;
import rapaio.darray.DArrays;
import rapaio.darray.Shape;
import rapaio.graphics.opt.NColor;
import rapaio.graphics.plot.GridLayer;
import rapaio.printer.ImageTools;

public class MatrixTest extends AbstractArtistTest {

    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random(1234);
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testMatrix() throws IOException {
        GridLayer grid = new GridLayer(2, 2);

        int n = 6;

        var randomm = DArrays.random(Shape.of(n, n), random);
        var mean = randomm.mean1d(0);
        var sd = randomm.std1d(0, 1);

        randomm.sub(mean).div(sd);

        var cov = randomm.t().mm(randomm);//.round(15);

        DistanceMatrix dm = DistanceMatrix.empty(n).fill(cov::getDouble);
        grid.add(matrix(cov));
        grid.add(corrGram(dm));

        grid.add(matrix(cov, color(NColor.black)));
        grid.add(matrix(DArrays.random(Shape.of(60, 80), random)));

        assertTest(grid, "matrix-test");
    }
}
