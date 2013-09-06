package rapaio.graphics.base;

import rapaio.graphics.options.GraphicOptions;

import java.awt.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public interface Figure {

    GraphicOptions getOp();

    void paint(Graphics2D g2d, Rectangle rect);

}
