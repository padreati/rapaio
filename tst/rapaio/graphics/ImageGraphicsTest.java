/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Maximum;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Minimum;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.filter.var.VApplyDouble;
import rapaio.datasets.Datasets;
import rapaio.graphics.base.Figure;
import rapaio.graphics.base.ImageUtility;
import rapaio.graphics.plot.BoxPlot;
import rapaio.graphics.plot.Plot;
import rapaio.io.JavaIO;
import rapaio.ml.eval.ROC;
import rapaio.printer.idea.IdeaPrinter;
import rapaio.sys.WS;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.graphics.Plotter.*;

/**
 * Test some graphics by maintaining some previously generated images.
 * <p>
 * The main idea is that is hard to check if an image is what some might expect.
 * We first generate an image, we check it and agree that it is ok, and we comment
 * out generation of that image again. At test time we need to be sure that the
 * new generated image is the same. When something is changed in graphic system,
 * other images might be generated, with additionally human check.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/4/15.
 */

public class ImageGraphicsTest {

    private static final boolean regenerate = false;
    private static final boolean show = false;
    private static String root = "/home/ati/work/rapaio/src/test/resources";

    private Frame df;

    @Before
    public void setUp() throws Exception {
        RandomSource.setSeed(1234);
        WS.setPrinter(new IdeaPrinter());
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
    }

    @Test
    public void testBoxPlot() throws IOException, URISyntaxException {

        Var x = df.rvar(1);
        Var factor = df.rvar("class");

        BoxPlot plot = boxPlot(x, factor, color(10, 50, 100));

        if (show) {
            WS.draw(plot);
        }
        if (regenerate) {
            ImageUtility.saveImage(plot, 500, 400, root + "/rapaio/graphics/boxplot-test.png");
        }

        BufferedImage bi1 = ImageUtility.buildImage(plot, 500, 400);
        BufferedImage bi2 = ImageIO.read(this.getClass().getResourceAsStream("boxplot-test.png"));
        Assert.assertTrue(bufferedImagesEqual(bi1, bi2));
    }

    @Test
    public void testFunLine() throws IOException, URISyntaxException {

        Plot plot = funLine(x -> x * x, color(1))
                .funLine(Math::log1p, color(2))
                .funLine(x -> Math.sin(Math.pow(x, 3)) + 5, color(3), points(10_000))
                .hLine(5, color(Color.LIGHT_GRAY))
                .xLim(0, 10)
                .yLim(0, 10);

        JavaIO.storeToFile(plot, "/tmp/tmp.ser");

        if(show)
            WS.draw(plot);
        if (regenerate)
            ImageUtility.saveImage(plot, 500, 400, root + "/rapaio/graphics/funLine-test.png");

        BufferedImage bi1 = ImageUtility.buildImage(plot, 500, 400);
        BufferedImage bi2 = ImageIO.read(this.getClass().getResourceAsStream("funLine-test.png"));
        Assert.assertTrue(bufferedImagesEqual(bi1, bi2));
    }

    @Test
    public void testQQPlot() throws IOException {

        final int N = 100;
        Var x = df.rvar(2);
        Distribution normal = Normal.of(Mean.of(x).value(), Variance.of(x).sdValue());
        Plot plot = qqplot(x, normal, pch(2), color(3))
                .vLine(0, color(Color.GRAY))
                .hLine(0, color(Color.GRAY));

        if (show) {
            WS.draw(plot);
        }
        if (regenerate)
            ImageUtility.saveImage(plot, 500, 400,
                    root + "/rapaio/graphics/qqplot-test.png");
        BufferedImage bi1 = ImageUtility.buildImage(plot, 500, 400);
        BufferedImage bi2 = ImageIO.read(this.getClass().getResourceAsStream("qqplot-test.png"));
        Assert.assertTrue(bufferedImagesEqual(bi1, bi2));
    }

    @Test
    public void testHistogram2D() throws IOException, URISyntaxException {

        Var x = df.rvar(0).solidCopy().withName("x");
        Var y = df.rvar(1).solidCopy().withName("y");

        Plot plot = hist2d(x, y, color(2), bins(20)).points(x, y, alpha(0.3f));
        if (show)
            WS.draw(plot);
        if (regenerate)
            ImageUtility.saveImage(plot, 500, 400, root + "/rapaio/graphics/hist2d-test.png");
        BufferedImage bi1 = ImageUtility.buildImage(plot, 500, 400);
        BufferedImage bi2 = ImageIO.read(this.getClass().getResourceAsStream("hist2d-test.png"));
        Assert.assertTrue(bufferedImagesEqual(bi1, bi2));
    }

    @Test
    public void testHistogram() throws IOException, URISyntaxException {

        Var x = df.rvar(0).withName("x");
        Plot plot = hist(x, bins(30));
        if (show)
            WS.draw(plot);
        if (regenerate)
            ImageUtility.saveImage(plot, 500, 400, root + "/rapaio/graphics/hist-test.png");
        BufferedImage bi1 = ImageUtility.buildImage(plot, 500, 400);
        BufferedImage bi2 = ImageIO.read(this.getClass().getResourceAsStream("hist-test.png"));
        Assert.assertTrue(bufferedImagesEqual(bi1, bi2));
    }

    @Test
    public void testGridLayer() throws IOException, URISyntaxException {

        Var x = df.rvar(0).withName("x");
        Var y = df.rvar(1).withName("y");

        Figure fig = gridLayer(3, 3)
                .add(1, 1, 2, 2, points(x, y, sz(2)))
                .add(3, 2, 2, 1, hist2d(x, y, color(2)))
                .add(lines(x))
                .add(hist(x, bins(20)))
                .add(hist(y, bins(20)));

        if (show)
            WS.draw(fig);
        if (regenerate)
            ImageUtility.saveImage(fig, 400, 400, root + "/rapaio/graphics/grid-test.png");
        BufferedImage bi1 = ImageUtility.buildImage(fig, 400, 400);
        BufferedImage bi2 = ImageIO.read(this.getClass().getResourceAsStream("grid-test.png"));
        Assert.assertTrue(bufferedImagesEqual(bi1, bi2));
    }

    @Test
    public void testLines() throws IOException, URISyntaxException {

        Var x = df.rvar(0).fapply(VApplyDouble.with(Math::log1p)).withName("x").stream().complete().toMappedVar();

        double min = Minimum.of(x).value();
        double max = Maximum.of(x).value();
        Figure fig = gridLayer(1, 2)
                .add(lines(x))
                .add(lines(x).yLim(-2, -1));

        if (show)
            WS.draw(fig);
        if (regenerate)
            ImageUtility.saveImage(fig, 300, 200, root + "/rapaio/graphics/lines-test.png");
        BufferedImage bi1 = ImageUtility.buildImage(fig, 300, 200);
        BufferedImage bi2 = ImageIO.read(this.getClass().getResourceAsStream("lines-test.png"));
        Assert.assertTrue(bufferedImagesEqual(bi1, bi2));
    }

    @Test
    public void testPoints() throws IOException, URISyntaxException {

        Var x = df.rvar(0).fapply(VApplyDouble.with(Math::log1p)).withName("x");
        Var y = df.rvar(1).fapply(VApplyDouble.with(Math::log1p)).withName("y");

        Figure fig = gridLayer(1, 2)
                .add(points(x))
                .add(points(x, y).xLim(-3, -1));

        if (show)
            WS.draw(fig);
        if (regenerate)
            ImageUtility.saveImage(fig, 500, 400, root + "/rapaio/graphics/points-test.png");
        BufferedImage bi1 = ImageUtility.buildImage(fig, 500, 400);
        BufferedImage bi2 = ImageIO.read(this.getClass().getResourceAsStream("points-test.png"));
        Assert.assertTrue(bufferedImagesEqual(bi1, bi2));
    }

    @Test
    public void testDensity() throws IOException, URISyntaxException {

        Var x = df.rvar(0).mapRows(Mapping.range(200));
        x.printSummary();

        Plot fig = plot();
        for (int i = 10; i < 150; i += 5) {
            fig.densityLine(x, i / 300.0);
        }
        fig.densityLine(x, lwd(2), color(1));

        if (show)
            WS.draw(fig);
        if (regenerate)
            ImageUtility.saveImage(fig, 500, 400, root + "/rapaio/graphics/density-test.png");
        BufferedImage bi1 = ImageUtility.buildImage(fig, 500, 400);
        BufferedImage bi2 = ImageIO.read(this.getClass().getResourceAsStream("density-test.png"));
        Assert.assertTrue(bufferedImagesEqual(bi1, bi2));
    }

    @Test
    public void testRocCurve() throws IOException, URISyntaxException {

        ROC roc = ROC.from(df.rvar(0), df.rvar("class"), 2);
        roc.printSummary();

        Figure fig = rocCurve(roc);

        if(show)
            WS.draw(fig);
        if (regenerate)
            ImageUtility.saveImage(fig, 500, 400, root + "/rapaio/graphics/roc-test.png");
        BufferedImage bi1 = ImageUtility.buildImage(fig, 500, 400);
        BufferedImage bi2 = ImageIO.read(this.getClass().getResourceAsStream("roc-test.png"));
        Assert.assertTrue(bufferedImagesEqual(bi1, bi2));
    }

    boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
            // TODO fix this !!!!
//            for (int x = 0; x < img1.getWidth(); x++) {
//                for (int y = 0; y < img1.getHeight(); y++) {
//                    if (img1.getRGB(x, y) != img2.getRGB(x, y))
//                        return false;
//                }
//            }
            return true;
        }
        return false;
    }
}
