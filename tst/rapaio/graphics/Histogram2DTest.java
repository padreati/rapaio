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
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.filter.Filters;
import rapaio.data.filter.VFilter;
import rapaio.datasets.Datasets;
import rapaio.graphics.base.ImageUtility;
import rapaio.graphics.opt.ColorPalette;
import rapaio.graphics.plot.Plot;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.data.filter.Filters.jitter;
import static rapaio.graphics.Plotter.*;

public class Histogram2DTest {

    @Test
    public void testHistogram2D() throws IOException, URISyntaxException {

        WS.setPrinter(new IdeaPrinter());
        RandomSource.setSeed(0);
        Frame df = Datasets.loadIrisDataset();

        Var x = jitter(df.var(0).solidCopy(), 0.01);
        Var y = jitter(df.var(1).solidCopy(), 0.01);

        Plot plot = hist2d(x, y, color(2), bins(10)).points(x, y);
        WS.draw(plot);
//        ImageUtility.saveImage(plot, 500, 400, "/home/ati/work/rapaio/tst/rapaio/graphics/hist2d-test.png");
        BufferedImage bi1 = ImageUtility.buildImage(plot, 500, 400);
        BufferedImage bi2 = ImageIO.read(this.getClass().getResourceAsStream("hist2d-test.png"));
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
