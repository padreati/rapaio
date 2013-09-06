package rapaio.graphics.colors;

import java.awt.*;
import java.io.Serializable;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public enum ColorPalette {

    STANDARD(new StandardColorPalette());
    //
    private final Mapping palette;

    ColorPalette(Mapping palette) {
        this.palette = palette;
    }

    public Color getColor(int index) {
        return palette.getColor(index);
    }

    public static interface Mapping extends Serializable {

        Color getColor(int index);
    }
}
