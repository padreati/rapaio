package rapaio.graphics.plot.artist;

import static rapaio.sys.With.fill;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.artist.AbstractArtistTest;

public class ABLineTest extends AbstractArtistTest {

    @Test
    void testABLine() throws IOException {
        Plot plot = new Plot();
        plot.xLim(-10, 10);
        plot.yLim(-10, 10);

        plot.hLine(0, fill(3));
        plot.hLine(1, fill(4));

        plot.vLine(0, fill(5));
        plot.vLine(1.2, fill(6));

        plot.abLine(1, 0, fill(7));
        plot.abLine(-1.2, 0, fill(8));

        assertTest(plot, "abline-test");
    }
}
