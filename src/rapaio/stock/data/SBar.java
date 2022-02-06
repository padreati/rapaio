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

package rapaio.stock.data;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarInstant;
import rapaio.data.VarInt;
import rapaio.data.VarLong;

/**
 * Summary bar of data. The data bar is a summary of transaction values during a specific period of time.
 * <p>
 * If multiple values are produced at various times the data bar summary is required to
 * handle all that information using equal time intervals.
 * <p>
 * The time interval which is covered by this bar is described by {@link #time()} which represents the start of the interval and
 * {@link #duration()} which tell us how long was the interval.
 * <p>
 * The values covered by this bar are summarized by the following:
 * <ul>
 *     <li>high</li> highest value
 *     <li>low</li> lowest value
 *     <li>open</li> first time ordered value
 *     <li>close</li> last time ordered value
 *     <li>wap</li> weighted average price value
 *     <li>volume</li> volume of transacted units
 *     <li>count</li> number of transactions
 * </ul>
 */
public record SBar(Instant time, Duration duration,
                   double high, double low, double open, double close, double wap,
                   long volume, int count) {

    /**
     * Builds a data frame with time sorted values from a list of data bars.
     *
     * @param bars collection of data bars
     * @return data frame
     */
    public static Frame asDf(Collection<SBar> bars) {
        var time = VarInstant.empty().name("time");
        var open = VarDouble.empty().name("open");
        var low = VarDouble.empty().name("low");
        var high = VarDouble.empty().name("high");
        var close = VarDouble.empty().name("close");
        var wap = VarDouble.empty().name("wap");
        var volume = VarLong.empty().name("volume");
        var count = VarInt.empty().name("count");

        List<SBar> copy = new ArrayList<>(bars);
        copy.sort(Comparator.comparing(SBar::time));
        for (SBar bar : copy) {
            time.addInstant(bar.time);
            open.addDouble(bar.open);
            high.addDouble(bar.high);
            low.addDouble(bar.low);
            close.addDouble(bar.close);
            wap.addDouble(bar.wap);
            volume.addLong(bar.volume);
            count.addInt(bar.count);
        }
        return SolidFrame.byVars(time, open, low, high, close, wap, volume, count);
    }
}
