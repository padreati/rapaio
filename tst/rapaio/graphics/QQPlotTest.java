/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.graphics;

import org.junit.Assert;
import org.junit.Test;
import rapaio.core.CoreTools;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Distribution;
import rapaio.data.Numeric;
import rapaio.graphics.base.ImageUtility;
import rapaio.graphics.plot.Plot;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static rapaio.graphics.Plotter.*;

public class QQPlotTest {

    @Test
    public void testQQPlot() throws IOException {

        WS.setPrinter(new IdeaPrinter());

        RandomSource.setSeed(1);
        final int N = 100;
        Distribution normal = CoreTools.distNormal();
        Numeric x = Numeric.from(N, row -> normal.sampleNext());
        Plot plot = qqplot(x, normal, pch(2), color(3))
                .abLine(false, 0, color(Color.GRAY))
                .abLine(true, 0, color(Color.GRAY));

        WS.draw(plot);
//        ImageUtility.saveImage(plot, 500, 400,
//                "/home/ati/work/rapaio/tst/rapaio/graphics/qqplot-test.png");
        BufferedImage bi1 = ImageUtility.buildImage(plot, 500, 400);
        BufferedImage bi2 = ImageIO.read(this.getClass().getResourceAsStream("qqplot-test.png"));
        Assert.assertTrue(bufferedImagesEqual(bi1, bi2));
    }

    boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
            for (int x = 0; x < img1.getWidth(); x++) {
                for (int y = 0; y < img1.getHeight(); y++) {
                    if (img1.getRGB(x, y) != img2.getRGB(x, y))
                        return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
}
