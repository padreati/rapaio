package rapaio.graphics.colors;

import java.awt.*;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class GrayColorPallete implements ColorPalette.Mapping {

	@Override
	public Color getColor(int index) {
		index %= 256;
		return new Color(index, index, index);
	}
}
