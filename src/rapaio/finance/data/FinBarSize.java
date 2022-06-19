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

package rapaio.finance.data;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public enum FinBarSize {
    _1sec("1 sec", Duration.of(1, ChronoUnit.SECONDS)),
    _5sec("5 sec", Duration.of(5, ChronoUnit.SECONDS)),
    _10sec("10 sec", Duration.of(10, ChronoUnit.SECONDS)),
    _15sec("15 sec", Duration.of(15, ChronoUnit.SECONDS)),
    _30sec("30 sec", Duration.of(30, ChronoUnit.SECONDS)),
    _1min("1 min", Duration.of(1, ChronoUnit.MINUTES)),
    _2min("2 min", Duration.of(2, ChronoUnit.MINUTES)),
    _3min("3 min", Duration.of(3, ChronoUnit.MINUTES)),
    _5min("5 min", Duration.of(5, ChronoUnit.MINUTES)),
    _10min("10 min", Duration.of(10, ChronoUnit.MINUTES)),
    _15min("15 min", Duration.of(115, ChronoUnit.MINUTES)),
    _20min("20 min", Duration.of(20, ChronoUnit.MINUTES)),
    _30min("30 min", Duration.of(30, ChronoUnit.MINUTES)),
    _1hour("1 hour", Duration.of(1, ChronoUnit.HOURS)),
    _2hour("2 hour", Duration.of(2, ChronoUnit.HOURS)),
    _4hour("4 hour", Duration.of(4, ChronoUnit.HOURS)),
    _1day("1 day", Duration.of(1, ChronoUnit.DAYS)),
    _1week("1 week", null),
    _1month("1 month", null);

    private final String description;
    private final Duration duration;

    FinBarSize(String description, Duration duration) {
        this.description = description;
        this.duration = duration;
    }

    public String toString() {
        return this.description;
    }

    public Duration duration() {
        return duration;
    }
}
