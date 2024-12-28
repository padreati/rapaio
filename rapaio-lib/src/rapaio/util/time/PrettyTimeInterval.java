/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.util.time;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public enum PrettyTimeInterval {

    _1_YEAR() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            return instant.atZone(TimeZone.getTimeZone("UTC").toZoneId())
                    .with(TemporalAdjusters.firstDayOfYear())
                    .truncatedTo(ChronoUnit.DAYS)
                    .toInstant();
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant)
                    .atZone(ZoneId.of("UTC"))
                    .with(TemporalAdjusters.lastDayOfYear())
                    .toInstant()
                    .plus(1, ChronoUnit.DAYS);
        }
    },
    _1_MONTH() {
        @Override
        public SimpleDateFormat groupFormat() {
            return utcSimpleDateFormat("yyyy-MM");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            return instant.atZone(TimeZone.getTimeZone("UTC").toZoneId())
                    .with(TemporalAdjusters.firstDayOfMonth())
                    .truncatedTo(ChronoUnit.DAYS)
                    .toInstant();
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant)
                    .atZone(ZoneId.of("UTC"))
                    .with(TemporalAdjusters.lastDayOfMonth())
                    .toInstant()
                    .plus(1, ChronoUnit.DAYS);
        }
    },
    _1_DAY() {
        @Override
        public SimpleDateFormat groupFormat() {
            return utcSimpleDateFormat("yyyy-MM-dd");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            return instant.truncatedTo(ChronoUnit.DAYS);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(1, ChronoUnit.DAYS);
        }
    },
    _12_HOUR() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.HOURS)
                    .toInstant();
            int hour = instant.atZone(ZoneId.of("UTC")).getHour();
            return instant.minus(hour % 12, ChronoUnit.HOURS);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(12, ChronoUnit.HOURS);
        }
    },
    _8_HOUR() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.HOURS)
                    .toInstant();
            int hour = instant.atZone(ZoneId.of("UTC")).getHour();
            return instant.minus(hour % 8, ChronoUnit.HOURS);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(8, ChronoUnit.HOURS);
        }
    },
    _6_HOUR() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.HOURS)
                    .toInstant();
            int hour = instant.atZone(ZoneId.of("UTC")).getHour();
            return instant.minus(hour % 6, ChronoUnit.HOURS);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(6, ChronoUnit.HOURS);
        }
    },
    _4_HOUR() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.HOURS)
                    .toInstant();
            int hour = instant.atZone(ZoneId.of("UTC")).getHour();
            return instant.minus(hour % 4, ChronoUnit.HOURS);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(4, ChronoUnit.HOURS);
        }
    },
    _3_HOUR() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.HOURS)
                    .toInstant();
            int hour = instant.atZone(ZoneId.of("UTC")).getHour();
            return instant.minus(hour % 3, ChronoUnit.HOURS);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(3, ChronoUnit.HOURS);
        }
    },
    _2_HOUR() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.HOURS)
                    .toInstant();
            int hour = instant.atZone(ZoneId.of("UTC")).getHour();
            return instant.minus(hour % 2, ChronoUnit.HOURS);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(2, ChronoUnit.HOURS);
        }
    },
    _1_HOUR() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            return instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.HOURS)
                    .toInstant();
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(1, ChronoUnit.HOURS);
        }
    },
    _30_MIN() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH:mm");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.MINUTES)
                    .toInstant();
            int minutes = instant.atZone(ZoneId.of("UTC")).getMinute();
            return instant.minus(minutes % 30, ChronoUnit.MINUTES);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(30, ChronoUnit.MINUTES);
        }
    },
    _20_MIN() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH:mm");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.MINUTES)
                    .toInstant();
            int minutes = instant.atZone(ZoneId.of("UTC")).getMinute();
            return instant.minus(minutes % 20, ChronoUnit.MINUTES);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(20, ChronoUnit.MINUTES);
        }
    },
    _15_MIN() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH:mm");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.MINUTES)
                    .toInstant();
            int minutes = instant.atZone(ZoneId.of("UTC")).getMinute();
            return instant.minus(minutes % 15, ChronoUnit.MINUTES);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(15, ChronoUnit.MINUTES);
        }
    },
    _10_MIN() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH:mm");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.MINUTES)
                    .toInstant();
            int minutes = instant.atZone(ZoneId.of("UTC")).getMinute();
            return instant.minus(minutes % 10, ChronoUnit.MINUTES);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(10, ChronoUnit.MINUTES);
        }
    },
    _5_MIN() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH:mm");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.MINUTES)
                    .toInstant();
            int minutes = instant.atZone(ZoneId.of("UTC")).getMinute();
            return instant.minus(minutes % 5, ChronoUnit.MINUTES);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(5, ChronoUnit.MINUTES);
        }
    },
    _2_MIN() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH:mm");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.MINUTES)
                    .toInstant();
            int minutes = instant.atZone(ZoneId.of("UTC")).getMinute();
            return instant.minus(minutes % 2, ChronoUnit.MINUTES);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(2, ChronoUnit.MINUTES);
        }
    },
    _1_MIN() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH:mm");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            return instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.MINUTES)
                    .toInstant();
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(1, ChronoUnit.MINUTES);
        }
    },
    _30_SEC() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.SECONDS)
                    .toInstant();
            int seconds = instant.atZone(ZoneId.of("UTC")).getSecond();
            return instant.minus(seconds % 30, ChronoUnit.SECONDS);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(30, ChronoUnit.SECONDS);
        }
    },
    _20_SEC() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.SECONDS)
                    .toInstant();
            int second = instant.atZone(ZoneId.of("UTC")).getSecond();
            return instant.minus(second % 20, ChronoUnit.SECONDS);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(20, ChronoUnit.SECONDS);
        }
    },
    _15_SEC() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.SECONDS)
                    .toInstant();
            int second = instant.atZone(ZoneId.of("UTC")).getSecond();
            return instant.minus(second % 15, ChronoUnit.SECONDS);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(15, ChronoUnit.SECONDS);
        }
    },
    _10_SEC() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.SECONDS)
                    .toInstant();
            int second = instant.atZone(ZoneId.of("UTC")).getSecond();
            return instant.minus(second % 10, ChronoUnit.SECONDS);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(10, ChronoUnit.SECONDS);
        }
    },
    _5_SEC() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.SECONDS)
                    .toInstant();
            int second = instant.atZone(ZoneId.of("UTC")).getSecond();
            return instant.minus(second % 5, ChronoUnit.SECONDS);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(5, ChronoUnit.SECONDS);
        }
    },
    _2_SEC() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            instant = instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.SECONDS)
                    .toInstant();
            int second = instant.atZone(ZoneId.of("UTC")).getSecond();
            return instant.minus(second % 2, ChronoUnit.SECONDS);
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(2, ChronoUnit.SECONDS);
        }
    },
    _1_SEC() {
        @Override
        public SimpleDateFormat groupFormat() {
            return PrettyTimeInterval.utcSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        @Override
        public Instant getInstantBefore(Instant instant) {
            return instant.atZone(ZoneId.of("UTC"))
                    .truncatedTo(ChronoUnit.SECONDS)
                    .toInstant();
        }

        @Override
        public Instant getInstantAfter(Instant instant) {
            return getInstantBefore(instant).plus(1, ChronoUnit.SECONDS);
        }
    };

    public abstract SimpleDateFormat groupFormat();

    public abstract Instant getInstantBefore(Instant instant);

    public abstract Instant getInstantAfter(Instant instant);

    public List<Instant> getInstantList(Instant start, Instant end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Start instant is greater than last instant.");
        }
        List<Instant> list = new ArrayList<>();
        Instant first = getInstantBefore(start);
        Instant last = end.equals(getInstantBefore(end)) ? end : getInstantAfter(end);

        while (first.isBefore(last) || first.equals(last)) {
            list.add(first);
            first = getInstantAfter(first);
        }
        return list;
    }

    private static SimpleDateFormat utcSimpleDateFormat(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf;
    }
}
