/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.image;

import rapaio.graphics.Figure;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ImageTools {

    public static final Map<RenderingHints.Key, Object> BEST_HINTS = new HashMap<>();
    public static final Map<RenderingHints.Key, Object> SPEED_HINTS = new HashMap<>();

    private static Map<RenderingHints.Key, Object> defaultHints = BEST_HINTS;

    static {
        BEST_HINTS.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        BEST_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        BEST_HINTS.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        BEST_HINTS.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        BEST_HINTS.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        BEST_HINTS.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        BEST_HINTS.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        BEST_HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        BEST_HINTS.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        SPEED_HINTS.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        SPEED_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        SPEED_HINTS.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        SPEED_HINTS.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        SPEED_HINTS.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        SPEED_HINTS.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        SPEED_HINTS.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        SPEED_HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        SPEED_HINTS.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private ImageTools() {
    }

    public static void setBestRenderingHints() {
        defaultHints = BEST_HINTS;
    }

    public static void setSpeedRenderingHints() {
        defaultHints = SPEED_HINTS;
    }

    public static Map<RenderingHints.Key, Object> getRenderingHints() {
        return defaultHints;
    }

    public static BufferedImage makeImage(Figure figure, int width, int height) {
        return makeImage(figure, width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
    }

    public static BufferedImage makeImage(Figure figure, int width, int height, int type) {
        BufferedImage newImage = new BufferedImage(width, height, type);
        Graphics g = newImage.getGraphics();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHints(defaultHints);
        Rectangle rect = new Rectangle(newImage.getWidth(), newImage.getHeight());
        figure.prepare(g2d, rect);
        figure.paint(g2d, rect);
        return newImage;
    }

    public static void saveFigureImage(Figure figure, int width, int height, String fileName) throws IOException {
        BufferedImage bi = makeImage(figure, width, height);
        ImageIO.write(bi, "png", new File(fileName));
    }

    public static void saveFigureImage(Figure figure, int width, int height, OutputStream os) throws IOException {
        BufferedImage bi = makeImage(figure, width, height);
        ImageIO.write(bi, "png", os);
    }

    public static BufferedImage readImage(File input) throws IOException {
        return ImageIO.read(input);
    }

    public static BufferedImage readImage(InputStream inputStream) throws IOException {
        return ImageIO.read(inputStream);
    }

    public static BufferedImage readImage(URL inputUrl) throws IOException {
        return ImageIO.read(inputUrl);
    }

    public static BufferedImage transformToBW(BufferedImage bi, Function<int[], Boolean> pixelFunction) {
        IndexColorModel cm = new IndexColorModel(1, 2,
                new byte[]{(byte) 0, (byte) 255},
                new byte[]{(byte) 0, (byte) 255},
                new byte[]{(byte) 0, (byte) 255});
        DataBufferByte db = new DataBufferByte((bi.getWidth() + 7) / 8 * bi.getHeight());
        WritableRaster raster = Raster.createPackedRaster(db, bi.getWidth(), bi.getHeight(), 1, null);

        int[] pixel = new int[bi.getColorModel().getNumColorComponents()];
        int pos = 0;
        for (int i = 0; i < bi.getWidth(); i++) {
            for (int j = 0; j < bi.getHeight(); j++) {
                bi.getData().getPixel(i, j, pixel);
                raster.setPixel(i, j, new int[]{pixelFunction.apply(pixel) ? 0 : 1});
            }
        }
        return new BufferedImage(cm, raster, false, null);
    }
}
