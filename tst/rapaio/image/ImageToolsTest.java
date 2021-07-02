/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.image;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/10/21.
 */
public class ImageToolsTest {

    @Test
    void testGray() throws IOException {
        BufferedImage bi = ImageIO.read(Objects.requireNonNull(this.getClass().getResourceAsStream("sff1can1.gif")));
        BufferedImage transformed = ImageTools.transformToBW(bi, pixel -> IntStream.of(pixel).sum() == 0);

//        WS.draw(gridLayer(2, 1)
//                .add(image(bi))
//                .add(image(transformed))
//        );
    }
}
