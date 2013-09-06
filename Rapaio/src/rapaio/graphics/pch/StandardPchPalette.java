package rapaio.graphics.pch;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class StandardPchPalette implements PchPalette.Mapping {

    private final ArrayList<Drawer> pchs = new ArrayList<>();

    public StandardPchPalette() {
        pchs.add(new Drawer() {
            @Override
            public void draw(Graphics2D g2d, int x, int y, double size) {
                g2d.drawOval((int) (x - size), (int) (y - size), (int) (size * 2 + 1), (int) (size * 2 + 1));
            }
        });
        pchs.add(new Drawer() {
            @Override
            public void draw(Graphics2D g2d, int x, int y, double size) {
                g2d.fillOval((int) (x - size), (int) (y - size), (int) (size * 2 + 1), (int) (size * 2 + 1));
            }
        });
    }

    @Override
    public void draw(Graphics2D g2d, int x, int y, double size, int pch) {
        if (pch < 0) {
            pch = 0;
        }
        if (pch >= pchs.size()) {
            pch %= pchs.size();
        }
        pchs.get(pch).draw(g2d, x, y, size);
    }
}

interface Drawer {

    void draw(Graphics2D g2d, int x, int y, double size);
}