/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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

package rapaio.graphics.base;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ImageUtility {

    public static BufferedImage buildImage(Figure figure, int width, int height) {
        return buildImage(figure, width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
    }

    public static BufferedImage buildImage(Figure figure, int width, int height, int type) {
        BufferedImage newImage = new BufferedImage(width, height, type);
        Graphics g = newImage.getGraphics();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        Rectangle rect = new Rectangle(newImage.getWidth(), newImage.getHeight());
        figure.paint(g2d, rect);
        return newImage;
    }

    public static void saveImage(Figure figure, int width, int height, String fileName) throws IOException {
        BufferedImage bi = buildImage(figure, width, height);
        ImageIO.write(bi, "png", new File(fileName));
    }

    public static void saveImage(Figure figure, int width, int height, OutputStream os) throws IOException {
        BufferedImage bi = buildImage(figure, width, height);
        ImageIO.write(bi, "png", os);
    }

    public static byte[] byteImage(Figure figure) throws IOException {
        return byteImage(figure, 1024, 800);
    }

    public static byte[] byteImage(Figure figure, int width, int height) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedImage bi = buildImage(figure, width, height);
        ImageIO.write(bi, "png", baos);
        return baos.toByteArray();
    }
}
