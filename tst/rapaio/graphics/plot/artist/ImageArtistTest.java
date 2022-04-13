package rapaio.graphics.plot.artist;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.position;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.graphics.plot.GridLayer;
import rapaio.image.ImageTools;

public class ImageArtistTest extends AbstractArtistTest {

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(1234);
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testImage() throws IOException {

        BufferedImage image = ImageTools.readImage(ImageArtistTest.class.getResourceAsStream("rapaio-logo.png"));

        GridLayer grid = new GridLayer(2, 2);

        grid.add(image(image).xLim(-1000, 1000).yLim(-1000, 1000));
        grid.add(image(image));
        grid.add(image(image, position(10,30,20,20)));

        assertTest(grid, "image-test");
    }
}
