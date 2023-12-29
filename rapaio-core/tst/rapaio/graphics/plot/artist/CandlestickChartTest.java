/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.graphics.plot.artist;

import static rapaio.graphics.Plotter.*;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.data.finance.FinBar;
import rapaio.data.finance.FinBarSize;
import rapaio.printer.ImageTools;

public class CandlestickChartTest extends AbstractArtistTest {

    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random(1234);
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

                double high = mean + Math.round(random.nextDouble() * 300) / 100.;
                double low = mean - Math.round(random.nextDouble() * 300) / 100.;

                double open = (high - low) * random.nextDouble() + low;
                double close = (high - low) * random.nextDouble() + low;

                FinBar bar = new FinBar(date, high, low, open, close, 0., 0, 0);
                bars.add(bar);
            }
        }
        CandlestickChart chart = new CandlestickChart(bars, FinBarSize._10min);
        assertTest(plot().add(chart), "candlestick-test");
    }
}
