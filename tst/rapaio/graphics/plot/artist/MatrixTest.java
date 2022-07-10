/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.graphics.plot.artist;

import static java.lang.StrictMath.*;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.*;

import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.tools.DistanceMatrix;
import rapaio.graphics.opt.NColor;
import rapaio.graphics.plot.GridLayer;
import rapaio.image.ImageTools;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;

public class MatrixTest extends AbstractArtistTest {

    private Random random;

    @BeforeEach
    void setUp() throws Exception {
        random = new Random(1234);
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testMatrix() throws IOException {
        GridLayer grid = new GridLayer(2, 2);

        int n = 6;

        DMatrix randomm = DMatrix.random(random, n, n);
        DVector mean = randomm.mean(0);
        DVector sd = randomm.sd(0).mul(sqrt(n - 1));

        randomm.sub(mean, 0).div(sd, 0);

        DMatrix cov = randomm.t().dot(randomm).roundValues(15);

        DistanceMatrix dm = DistanceMatrix.empty(n).fill(cov::get);
        grid.add(matrix(cov));
        grid.add(corrGram(dm));

        grid.add(matrix(cov, color(NColor.black)));
        grid.add(matrix(DMatrix.random(60, 80)));

        assertTest(grid, "matrix-test");
    }
}
