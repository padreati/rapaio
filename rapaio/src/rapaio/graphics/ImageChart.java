package rapaio.graphics;

import rapaio.graphics.base.AbstractFigure;
import rapaio.graphics.base.Range;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class ImageChart extends AbstractFigure {

    private final BufferedImage image;

    public ImageChart(BufferedImage image) {
        this.image = image;
    }

    @Override
    public Range buildRange() {
        return null;
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle rect) {
        super.paint(g2d, rect);

        g2d.drawImage(image, null, 0, 0);
    }
}
