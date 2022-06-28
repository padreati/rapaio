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

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.*;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.graphics.plot.GridLayer;
import rapaio.image.ImageTools;

public class ImageArtistTest extends AbstractArtistTest {

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(1234);
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testImage() throws IOException {

        BufferedImage image = ImageTools.readImage(ImageArtistTest.class.getResourceAsStream("rapaio-logo.png"));

        GridLayer grid = new GridLayer(2, 2);

        grid.add(image(image).xLim(-1000, 1000).yLim(-1000, 1000));
        grid.add(image(image));
        grid.add(image(image, position(10,30,20,20)));

        assertTest(grid, "image-test");
    }
}
