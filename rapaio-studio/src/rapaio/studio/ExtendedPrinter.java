package rapaio.studio;

import rapaio.printer.Printer;

import java.awt.image.BufferedImage;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public interface ExtendedPrinter extends Printer {

    void setImage(BufferedImage image);
}
