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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import rapaio.math.linear.DMatrix;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/17/21.
 */
public class HoughTransformTest {

    @Test
    void testAccumulators() throws IOException {

        BufferedImage bi = new BufferedImage(20, 20, BufferedImage.TYPE_BYTE_GRAY);
        Raster raster = bi.getData();
        byte[] buffer = ((DataBufferByte) raster.getDataBuffer()).getData();
        Arrays.fill(buffer, (byte) 255);
        BitSet bs = new BitSet(buffer.length);
        for (int i = 0; i < 20; i++) {
            buffer[i * 20 + 2] = (byte) 0;
            bs.set(i * 20 + 2);
        }
        for (int i = 0; i < 20; i++) {
            // 9,
            buffer[i * 20 + 19 - i] = (byte) 0;
            bs.set(i * 20 + 19 - i);
        }
        bi.setData(raster);

        HoughTransform ht = HoughTransform
                .newTransform()
                .thetaSize.set(100)
                .rhoSize.set(100)
                .fit(20, 20, bs);

        DMatrix htm = ht.getHsMatrix();

        List<HoughTransform.Line> lines = ht.getLines(0.001);
        assertEquals(20, lines.get(0).count());
        assertEquals(20, lines.get(1).count());

        for (int i = 2; i < lines.size(); i++) {
            assertTrue(lines.get(i).count() < 10);
        }
    }

    @Test
    void testCustom() throws IOException {

        BufferedImage bi = ImageTools.transformToBW(ImageTools.readImage(getClass().getResourceAsStream("sff1can1.gif")),
                pixel -> IntStream.of(pixel).sum() == 0);

        Raster raster = bi.getData();
        byte[] buffer = ((DataBufferByte) raster.getDataBuffer()).getData();
        BitSet bs = BitSet.valueOf(buffer);

        HoughTransform ht = HoughTransform
                .newTransform()
                .thetaSize.set(100)
                .rhoSize.set(100)
                .fit(100, 100, bs);


        DMatrix htm = ht.getHsMatrix();

//        ImageTools.setSpeedRenderingHints();
//        WS.draw(gridLayer(1, 2)
//                .add(image(bi))
//                .add(matrix(htm, palette(ColorPalette.RED_BLUE_GRADIENT)))
//        );
    }
}
