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

package rapaio.printer;

import rapaio.graphics.base.Figure;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/3/15.
 */
public class BeakerPrinter implements Printer {

    private int textWidth = 120;
    private int graphicWidth = 800;
    private int graphicHeight = 600;

    @Override
    public int getTextWidth() {
        return textWidth;
    }

    @Override
    public void setTextWidth(int chars) {
        this.textWidth = chars;
    }

    @Override
    public int getGraphicWidth() {
        return graphicWidth;
    }

    @Override
    public void setGraphicWidth(int width) {
        this.graphicWidth = width;
    }

    @Override
    public int getGraphicHeight() {
        return graphicHeight;
    }

    @Override
    public void setGraphicHeight(int height) {
        this.graphicHeight = height;
    }

    @Override
    public void print(String message) {
        System.out.print(message);
    }

    @Override
    public void println() {
        System.out.println();
    }

    @Override
    public void error(String message, Throwable throwable) {
        System.err.println(message);
        System.err.println(throwable.getMessage());
    }

    @Override
    public void preparePrinter() {

    }

    @Override
    public void closePrinter() {

    }

    @Override
    public void heading(int h, String lines) {
        System.out.println("<h" + h + ">" + lines + "</h" + h + ">");
    }

    @Override
    public void code(String lines) {
        System.out.print(lines);
    }

    @Override
    public void p(String lines) {
        System.out.print(lines);
    }

    @Override
    public void eqn(String equation) {
        System.out.print(equation);
    }

    @Override
    public void draw(Figure figure, int width, int height) {
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = newImage.getGraphics();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        Rectangle rect = new Rectangle(newImage.getWidth(), newImage.getHeight());
        figure.paint(g2d, rect);
        String imageString;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(newImage, "png", bos);
            byte[] imageBytes = bos.toByteArray();
            BASE64Encoder encoder = new BASE64Encoder();
            imageString = encoder.encode(imageBytes);
            bos.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not produce image", e);
        }
        System.out.println("<img src=\"data:image/png;base64," + imageString + "\" />\n");
    }

    @Override
    public void draw(Figure figure) {
        draw(figure, graphicWidth, graphicHeight);
    }
}
