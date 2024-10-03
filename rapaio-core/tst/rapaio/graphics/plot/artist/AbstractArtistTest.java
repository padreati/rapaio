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

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;

import rapaio.core.stat.Quantiles;
import rapaio.data.VarDouble;
import rapaio.printer.Figure;
import rapaio.printer.ImageTools;
import rapaio.sys.WS;

public abstract class AbstractArtistTest {

    public static final String ROOT = "/home/ati/work/rapaio/rapaio-core/tst";

    @BeforeEach
    void setUp() throws Exception {
        ImageTools.setBestRenderingHints();
    }

    protected final boolean regenerate() {
        return false;
    }

    protected void assertTest(Figure f, String name) throws IOException {
        if (regenerate()) {
            ImageTools.saveFigureImage(f, 800, 600, ROOT + "/rapaio/graphics/plot/artist/" + name + ".png");
        }

        BufferedImage bi1 = ImageTools.makeImage(f, 800, 600);
        BufferedImage bi2 = ImageIO.read(Objects.requireNonNull(RapaioLogoTest.class.getResourceAsStream(name + ".png")));
        boolean condition = bufferedImagesEqual(bi1, bi2);
        assertTrue(condition);
    }

    boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth()) {
            return false;
        }
        if (img1.getHeight() != img2.getHeight()) {
            return false;
        }
        VarDouble s1 = VarDouble.empty();
        VarDouble s2 = VarDouble.empty();
        for (int x = 0; x < img1.getWidth(); x++) {
            for (int y = 0; y < img1.getHeight(); y++) {
                Color c1 = new Color(img1.getRGB(x, y));
                Color c2 = new Color(img2.getRGB(x, y));

                s1.addDouble(c1.getAlpha());
                s1.addDouble(c1.getRed());
                s1.addDouble(c1.getGreen());
                s1.addDouble(c1.getBlue());

                s2.addDouble(c2.getAlpha());
                s2.addDouble(c2.getRed());
                s2.addDouble(c2.getGreen());
                s2.addDouble(c2.getBlue());
            }
        }
//        TODO: fix me, suspend testing until we found a reliable way to do comparison
        var delta = s1.copy();
        delta.tensor_().sub_(s2.tensor_()).abs_();
        double percent = 0.9;
        double threshold = 45;
        double quantile = Quantiles.of(delta, percent).values()[0];
        if (!(quantile <= threshold)) {
            WS.printf("Percentage: %f, quantile: %f, threshold: %f", percent, quantile, threshold);
            return false;
        }
        return true;
    }

}
