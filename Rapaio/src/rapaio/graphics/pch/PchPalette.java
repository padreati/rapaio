package rapaio.graphics.pch;

import java.awt.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public enum PchPalette {

    STANDARD(new StandardPchPalette());
    private final Mapping mapping;

    private PchPalette(Mapping mapping) {
        this.mapping = mapping;
    }

    public void draw(Graphics2D g2d, int x, int y, double size, int pch) {
        mapping.draw(g2d, x, y, size, pch);
    }

    public static interface Mapping {
        void draw(Graphics2D g2d, int x, int y, double size, int pch);
    }
}
