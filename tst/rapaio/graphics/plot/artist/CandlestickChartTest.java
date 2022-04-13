package rapaio.graphics.plot.artist;

import static rapaio.graphics.Plotter.*;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.datasets.Datasets;
import rapaio.finance.data.FinBar;
import rapaio.finance.data.FinBarSize;
import rapaio.graphics.plot.artist.AbstractArtistTest;
import rapaio.graphics.plot.artist.CandlestickChart;
import rapaio.image.ImageTools;

public class CandlestickChartTest extends AbstractArtistTest {

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testCandlestick() throws IOException {
        List<FinBar> bars = new ArrayList<>();
        Instant start = Instant.parse("2022-03-05T16:30:00.000Z");
        double mean = 200;
        for (int day = 0; day < 3; day++) {
            Instant startDay = start.plus(day, ChronoUnit.DAYS);
            for (int i = 0; i < 6 * 5; i++) {
                Instant date = startDay.plus(i * 10, ChronoUnit.MINUTES);

                mean = mean + Math.round(Normal.std().sampleNext() * 300) / 100.;

                double high = mean + Math.round(RandomSource.nextDouble()*300)/100.;
                double low = mean - Math.round(RandomSource.nextDouble()*300)/100.;

                double open = (high - low)*RandomSource.nextDouble()+low;
                double close = (high-low)*RandomSource.nextDouble()+low;

                FinBar bar = new FinBar(date, high, low, open, close, 0., 0, 0);
                bars.add(bar);
            }
        }
        CandlestickChart chart = new CandlestickChart(bars, FinBarSize._10min);
        assertTest(plot().add(chart), "candlestick-test");
    }
}
