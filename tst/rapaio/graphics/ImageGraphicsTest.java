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

package rapaio.graphics;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

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
import rapaio.core.tools.DistanceMatrix;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.filter.VApply;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.GridLayer;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.artist.PolyFill;
import rapaio.graphics.plot.artist.PolyLine;
import rapaio.graphics.plot.artist.Text;
import rapaio.image.ImageTools;
import rapaio.ml.clustering.km.KMCluster;
import rapaio.ml.clustering.km.KMClusterResult;
import rapaio.ml.eval.ClusterSilhouette;
import rapaio.ml.eval.metric.ROC;
import rapaio.sys.WS;

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
    private static final String root = "/home/ati/work/rapaio/tst";

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    private void assertTest(Figure f, String name) throws IOException {
        if (regenerate) {
            ImageTools.saveFigureImage(f, 500, 400, root + "/rapaio/graphics/" + name + ".png");
        }

        BufferedImage bi1 = ImageTools.makeImage(f, 500, 400);
        BufferedImage bi2 = ImageIO.read(this.getClass().getResourceAsStream(name + ".png"));
        boolean condition = bufferedImagesEqual(bi1, bi2);
        assertTrue(condition);
    }

    @Test
    void testABLine() throws IOException {
        Plot plot = new Plot();
        plot.xLim(-10, 10);
        plot.yLim(-10, 10);

        plot.hLine(0, fill(3));
        plot.hLine(1, fill(4));

        plot.vLine(0, fill(5));
        plot.vLine(1.2, fill(6));

        plot.abLine(1, 0, fill(7));
        plot.abLine(-1.2, 0, fill(8));

        assertTest(plot, "abline-test");
    }

    @Test
    void testCorrGram() throws IOException {
        Frame sel = Datasets.loadHousing();
        DistanceMatrix d = CorrSpearman.of(sel).matrix();
        assertTest(corrGram(d), "corrgram-test");
    }

    @Test
    void testBoxPlot() throws IOException {
        Var x = df.rvar(1);
        Var factor = df.rvar("class");
        var plot = boxplot(x, factor, fill(10, 50, 100));
        assertTest(plot, "boxplot-test");
    }

    @Test
    void testBarPlot() throws IOException {
        var mush = Datasets.loadMushrooms();

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
        Plot plot = qqplot(x, normal, pch(2), fill(3))
                .vLine(0, fill(Color.GRAY))
                .hLine(0, fill(Color.GRAY));

        assertTest(plot, "qqplot-test");
    }

    @Test
    void testHistogram2D() throws IOException {

        Var x = df.rvar(0).copy().name("x");
        Var y = df.rvar(1).copy().name("y");

        Plot plot = hist2d(x, y, fill(2), bins(20)).points(x, y, alpha(0.3f));
        assertTest(plot, "hist2d-test");
    }

    @Test
    void testHistogram() throws IOException {
        Var x = df.rvar(0).name("x");
        assertTest(hist(x, bins(30)), "hist-test");
    }

    @Test
    void testGridLayer() throws IOException {

        Var x = df.rvar(0).name("x");
        Var y = df.rvar(1).name("y");

        Figure fig = gridLayer(3, 3)
                .add(0, 0, 2, 2, points(x, y, sz(2)))
                .add(2, 1, 2, 1, hist2d(x, y, fill(2)))
                .add(lines(x))
                .add(hist(x, bins(20)))
                .add(hist(y, bins(20)));

        assertTest(fig, "grid-test");
    }

    @Test
    void testLines() throws IOException {

        Var x = df.rvar(0).fapply(VApply.onDouble(Math::log1p)).name("x").stream().complete().toMappedVar();
        Figure fig = gridLayer(1, 2)
                .add(lines(x))
                .add(lines(x).yLim(-2, -1));
        assertTest(fig, "lines-test");
    }

    @Test
    void testPoints() throws IOException {

        Var x = df.rvar(0).fapply(VApply.onDouble(Math::log1p)).name("x");
        Var y = df.rvar(1).fapply(VApply.onDouble(Math::log1p)).name("y");

        Figure fig = gridLayer(1, 2)
                .add(points(x))
                .add(points(x, y, pch(2), fill(2), color(1)).xLim(-3, -1));
        assertTest(fig, "points-test");
    }

    @Test
    void testDensity() throws IOException {

        Var x = df.rvar(0).mapRows(Mapping.range(200));
        var up = densityLine(x, new KFuncGaussian(), lwd(30), alpha(0.1f), color(2));
        for (int i = 10; i < 150; i += 5) {
            up.densityLine(x, i / 300.0);
        }
        up.densityLine(x, lwd(2), color(1));

        var down = densityLine(df.rvar(0), fill(13));
        assertTest(gridLayer(1, 2).add(up).add(down), "density-test");
    }

    @Test
    void testRocCurve() throws IOException {

        ROC roc = ROC.from(df.rvar(0), df.rvar("class"), 2);
        Figure fig = rocCurve(roc);
        assertTest(fig, "roc-test");
    }

    @Test
    void testSegment() throws IOException {
        Plot plot = new Plot();
        plot.xLim(0, 1);
        plot.yLim(0, 1);

        plot.segmentLine(0.1, 0.1, 0.7, 0.7, fill(1));
        plot.segmentArrow(0.1, 0.9, 0.9, 0.1, fill(2), lwd(6));

        assertTest(plot, "segment-test");
    }

    @Test
    void testText() throws IOException {
        var plot = plot().xLim(0, 1).yLim(0, 1);
        plot.text(0.1, 0.9, "Ana\nAre\nMere", hAlign(HALIGN_LEFT));
        plot.text(0.5, 0.9, "Ana\nAre\nMere", hAlign(HALIGN_CENTER), color(2));
        plot.text(0.8, 0.9, "Ana\nAre\nMere", hAlign(HALIGN_RIGHT), color(4));

        assertTest(plot, "text-test");
    }

    @Test
    void testSilhouette() throws IOException {
        Frame df = Datasets.loadIrisDataset().removeVars("class");

        KMCluster kMeans = KMCluster.newKMeans().k.set(2).method.set(KMCluster.KMeans);
        kMeans.fit(df);
        KMClusterResult prediction = kMeans.predict(df);
        VarInt assignment = prediction.getAssignment();

        DistanceMatrix dm = DistanceMatrix.empty(df.rowCount()).fill((i, j) -> {
            double sum = 0;
            for (int k = 0; k < df.varCount(); k++) {
                double delta = df.getDouble(i, k) - df.getDouble(j, k);
                sum += delta * delta;
            }
            return Math.sqrt(sum);
        });
        ClusterSilhouette silhouette = ClusterSilhouette.from(assignment, dm, false).compute();

        assertTest(silhouette(silhouette, horizontal(true)), "silhouette-test");
    }

    @Test
    void testRapaioLogo() throws IOException {
        var x = VarDouble.seq(0, 1, 0.004).name("x");

        var green = Normal.of(0.24, 0.08);
        var blue = Normal.of(0.37, 0.15);
        var orange = Normal.of(0.59, 0.13);
        var red = Normal.of(0.80, 0.06);

        Color cgreen = Color.decode("0x2ca02c");
        Color cblue = Color.decode("0x1f77b4");
        Color corange = Color.decode("0xff7f0e");
        Color cred = Color.decode("0xd62728");

        var ygreen = VarDouble.from(x, green::pdf).name("y");
        var yblue = VarDouble.from(x, blue::pdf).name("y");
        var yorange = VarDouble.from(x, orange::pdf).name("y");
        var yred = VarDouble.from(x, red::pdf).name("y");

        float alpha = 0.5f;
        float lwd = 5f;

        Plot up = plot();

        up.add(new PolyFill(x, yblue, fill(cblue), alpha(alpha)));
        up.add(new PolyFill(x, yorange, fill(corange), alpha(alpha)));
        up.add(new PolyFill(x, ygreen, fill(cgreen), alpha(alpha)));
        up.add(new PolyFill(x, yred, fill(cred), alpha(alpha)));

        up.add(new PolyLine(false, x, yblue, color(cblue), lwd(lwd)));
        up.add(new PolyLine(false, x, yorange, color(corange), lwd(lwd)));
        up.add(new PolyLine(false, x, ygreen, color(cgreen), lwd(lwd)));
        up.add(new PolyLine(false, x, yred, color(cred), lwd(lwd)));

        up.xLim(0, 1);
        up.leftThick(false);
        up.leftMarkers(false);
        up.bottomThick(false);
        up.bottomMarkers(false);

        Plot down = plot();

        down.leftThick(false);
        down.leftMarkers(false);
        down.bottomThick(false);
        down.bottomMarkers(false);

        down.add(new Text(0.5, 0.6, "rapaio", font("DejaVu Sans", Font.BOLD, 110),
                hAlign(HALIGN_CENTER), vAlign(VALIGN_CENTER), color(Color.decode("0x096b87"))));
        down.xLim(0, 1);
        down.yLim(0, 1);

        Figure fig = gridLayer(2, 1, heights(0.7, 0.3)).add(up).add(down);
        assertTest(fig, "rapaio-logo");
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
//        TODO: fix me, suspend testing until we found a reliable way to do comparison
        var delta = s1.copy();
        delta.dv().sub(s2.dv()).apply(Math::abs);
        double percent = 0.9;
        double threshold = 45;
        double quantile = Quantiles.of(delta, percent).values()[0];
        if (!(quantile <= threshold)) {
            WS.printf("Percentage: %f, quantile: %f, threshold: %f", percent, quantile, threshold);
            return false;
        }
        return img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight();
    }
}
