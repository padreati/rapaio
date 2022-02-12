/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.graphics.plot.artist;

import java.awt.Graphics2D;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.distributions.Uniform;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInstant;
import rapaio.finance.data.FinBar;
import rapaio.finance.data.FinBarSize;
import rapaio.graphics.Plotter;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;
import rapaio.math.MathTools;
import rapaio.math.linear.DVector;
import rapaio.sys.WS;

public class CandlestickChart extends Artist {

    private final List<FinBar> bars;
    private final FinBarSize barSize;

    public CandlestickChart(List<FinBar> bars, FinBarSize barSize) {
        this.bars = bars;
        this.barSize = barSize;
    }

    @Override
    public Axis.Type xAxisType() {
        return Axis.Type.INSTANT;
    }

    @Override
    public Axis.Type yAxisType() {
        return Axis.Type.NUMERIC;
    }

    @Override
    public void updateDataRange(Graphics2D g2d) {
        for (FinBar bar : bars) {
            union(bar.time().toEpochMilli(), bar.low());
            union(bar.time().toEpochMilli(), bar.high());
        }
    }

    @Override
    public void paint(Graphics2D g2d) {

    }


    public static void main(String[] args) {

        Instant start = Instant.parse("2010-10-23T13:30:00.000Z");
        Normal normal = Normal.of(0, 0.1);
        List<FinBar> bars = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 60 * 4.5; j++) {
                Instant time = start.plus(i, ChronoUnit.DAYS).plus(j, ChronoUnit.MINUTES);
                double value = Math.sin(time.getEpochSecond() * MathTools.DOUBLE_PI / 20000);
                value += normal.sampleNext();

                DVector v = Uniform.of(0, 100).sample(4).dv().sortValues();

                bars.add(new FinBar(
                        time,
                        value + v.get(3),
                        value - (1 - v.get(0)),
                        value + v.get(2),
                        value - (1 - v.get(1)),
                        value,
                        RandomSource.nextInt(25),
                        RandomSource.nextInt(20)
                ));
            }
        }

        Frame df = FinBar.asDf(bars);

        df.printHead(10);
        WS.draw(Plotter.lines(df.rvar("time"), df.rvar("wap")));
    }
}
