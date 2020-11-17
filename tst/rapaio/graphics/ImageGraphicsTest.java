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

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.correlation.CorrSpearman;
import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.core.distributions.empirical.KFuncGaussian;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Quantiles;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.filter.VApply;
import rapaio.datasets.Datasets;
import rapaio.experiment.ml.clustering.DistanceMatrix;
import rapaio.graphics.base.Figure;
import rapaio.graphics.plot.GridLayer;
import rapaio.graphics.plot.Plot;
import rapaio.image.ImageUtility;
import rapaio.ml.eval.metric.ROC;
import rapaio.sys.WS;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
    //            private static final boolean regenerate = true;
    private static final boolean show = false;
    //        private static final boolean show = true;
    private static final String root = "/home/ati/work/rapaio/tst";

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
//        ImageUtility.setBestHints();
        ImageUtility.setSpeedHints();
    }

    private void assertTest(Figure f, String name) throws IOException {
        if (show) {
            WS.draw(f);
        }
        if (regenerate) {
            ImageUtility.saveImage(f, 500, 400, root + "/rapaio/graphics/" + name + ".png");
        }

        BufferedImage bi1 = ImageUtility.buildImage(f, 500, 400);
        BufferedImage bi2 = ImageIO.read(this.getClass().getResourceAsStream(name + ".png"));
        boolean condition = bufferedImagesEqual(bi1, bi2);
        if (!condition) {
            WS.draw(f, 500, 400);
        }
        assertTrue(condition);
    }

    @Test
    @SneakyThrows
    void testABLine() {
        Plot plot = new Plot();
        plot.xLim(-10, 10);
        plot.yLim(-10, 10);

        plot.hLine(0, color(3));
        plot.hLine(1, color(4));

        plot.vLine(0, color(5));
        plot.vLine(1.2, color(6));

        plot.abLine(1, 0, color(7));
        plot.abLine(-1.2, 0, color(8));

        assertTest(plot, "abline-test");
    }

    @Test
    @SneakyThrows
    void testCorrGram() {
        Frame sel = Datasets.loadHousing();
        DistanceMatrix d = CorrSpearman.of(sel).matrix();
        assertTest(corrGram(d), "corrgram-test");
    }

    @Test
    void testBoxPlot() throws IOException {
        Var x = df.rvar(1);
        Var factor = df.rvar("class");
        Plot plot = boxplot(x, factor, color(10, 50, 100));

        assertTest(plot, "boxplot-test");
    }

    @Test
    @SneakyThrows
    void testBarPlot() {
        var mush = Datasets.loadMushrooms();
        mush.printSummary();

        GridLayer grid = gridLayer(2, 2);
        grid.add(barplot(mush.rvar("gill-color"), mush.rvar("classes"), stacked(false)));
        grid.add(barplot(mush.rvar("gill-color"), mush.rvar("classes"), stacked(true)));
        grid.add(barplot(mush.rvar("gill-color"), mush.rvar("classes"), stacked(false), sort(-1)));
        grid.add(barplot(mush.rvar("gill-color"), mush.rvar("classes"), stacked(true), sort(1), top(5)));

        assertTest(grid, "barplot-test");
    }

    @Test
    void testFunLine() throws IOException {

        Plot plot = funLine(x -> x * x, color(1))
                .funLine(Math::log1p, color(2))
                .funLine(x -> Math.sin(Math.pow(x, 3)) + 5, color(3), points(10_000))
                .hLine(5, color(Color.LIGHT_GRAY))
                .xLim(0, 10)
                .yLim(0, 10);

        assertTest(plot, "funline-test");
    }

    @Test
    void testQQPlot() throws IOException {

        final int N = 100;
        Var x = df.rvar(2);
        Distribution normal = Normal.of(Mean.of(x).value(), Variance.of(x).sdValue());
        Plot plot = qqplot(x, normal, pch(2), color(3))
                .vLine(0, color(Color.GRAY))
                .hLine(0, color(Color.GRAY));

        assertTest(plot, "qqplot-test");
    }

    @Test
    void testHistogram2D() throws IOException {

        Var x = df.rvar(0).copy().withName("x");
        Var y = df.rvar(1).copy().withName("y");

        Plot plot = hist2d(x, y, color(2), bins(20)).points(x, y, alpha(0.3f));
        assertTest(plot, "hist2d-test");
    }

    @Test
    void testHistogram() throws IOException {
        Var x = df.rvar(0).withName("x");
        assertTest(hist(x, bins(30)), "hist-test");
    }

    @Test
    void testGridLayer() throws IOException {

        Var x = df.rvar(0).withName("x");
        Var y = df.rvar(1).withName("y");

        Figure fig = gridLayer(3, 3)
                .add(1, 1, 2, 2, points(x, y, sz(2)))
                .add(3, 2, 2, 1, hist2d(x, y, color(2)))
                .add(lines(x))
                .add(hist(x, bins(20)))
                .add(hist(y, bins(20)));

        assertTest(fig, "grid-test");
    }

    @Test
    void testLines() throws IOException {

        Var x = df.rvar(0).fapply(VApply.onDouble(Math::log1p)).withName("x").stream().complete().toMappedVar();

        Figure fig = gridLayer(1, 2)
                .add(lines(x))
                .add(lines(x).yLim(-2, -1));

        assertTest(fig, "lines-test");
    }

    @Test
    void testPoints() throws IOException {

        Var x = df.rvar(0).fapply(VApply.onDouble(Math::log1p)).withName("x");
        Var y = df.rvar(1).fapply(VApply.onDouble(Math::log1p)).withName("y");

        Figure fig = gridLayer(1, 2)
                .add(points(x))
                .add(points(x, y).xLim(-3, -1));
        assertTest(fig, "points-test");
    }

    @Test
    void testDensity() throws IOException {

        Var x = df.rvar(0).mapRows(Mapping.range(200));
        Plot fig = densityLine(x, new KFuncGaussian(), lwd(30), alpha(0.1f), color(2));
        for (int i = 10; i < 150; i += 5) {
            fig.densityLine(x, i / 300.0);
        }
        fig.densityLine(x, lwd(2), color(1));
        assertTest(fig, "density-test");
    }

    @Test
    void testRocCurve() throws IOException {

        ROC roc = ROC.from(df.rvar(0), df.rvar("class"), 2);
        Figure fig = rocCurve(roc);
        assertTest(fig, "roc-test");
    }

    @Test
    @SneakyThrows
    void testSegment() {
        Plot plot = new Plot();
        plot.xLim(0, 1);
        plot.yLim(0, 1);

        plot.segmentLine(0.1, 0.1, 0.7, 0.7, color(1));
        plot.segmentArrow(0.1, 0.9, 0.9, 0.1, color(2), lwd(6));

        assertTest(plot, "segment-test");
    }

    boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
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
        boolean condition = Quantiles.of(s1.copy().op().minus(s2), 0.90).values()[0] < 15;
        if (!condition) {
            return condition;
        }
        return img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight();
    }
}
