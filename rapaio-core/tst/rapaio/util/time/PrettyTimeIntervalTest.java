/*
 *
 *  * Apache License
 *  * Version 2.0, January 2004
 *  * http://www.apache.org/licenses/
 *  *
 *  * Copyright 2013 - 2022 Aurelian Tutuianu
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package rapaio.util.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

public class PrettyTimeIntervalTest {

    @Test
    void testInterval() {
        Instant start = Instant.parse("2011-02-02T14:36:00.000Z");
        Instant end = start.plus(18, ChronoUnit.MINUTES);

        List<Instant> instants = PrettyTimeInterval._2_MIN.getInstantList(start, end);
        List<Instant> expected = List.of(
                Instant.parse("2011-02-02T14:36:00.000Z"),
                Instant.parse("2011-02-02T14:38:00.000Z"),
                Instant.parse("2011-02-02T14:40:00.000Z"),
                Instant.parse("2011-02-02T14:42:00.000Z"),
                Instant.parse("2011-02-02T14:44:00.000Z"),
                Instant.parse("2011-02-02T14:46:00.000Z"),
                Instant.parse("2011-02-02T14:48:00.000Z"),
                Instant.parse("2011-02-02T14:50:00.000Z"),
                Instant.parse("2011-02-02T14:52:00.000Z"),
                Instant.parse("2011-02-02T14:54:00.000Z")
        );

        assertEquals(expected.size(), instants.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), instants.get(i));
        }
    }

    @Test
    void testBeforeAfter() {
        assertEquals(
                PrettyTimeInterval._1_YEAR.getInstantAfter(Instant.parse("2020-02-01T01:07:30.000Z")),
                PrettyTimeInterval._1_YEAR.getInstantBefore(Instant.parse("2021-02-01T01:07:30.000Z"))
        );
        assertEquals(
                PrettyTimeInterval._1_MONTH.getInstantAfter(Instant.parse("2020-02-01T01:07:30.000Z")),
                PrettyTimeInterval._1_MONTH.getInstantBefore(Instant.parse("2020-03-01T01:07:30.000Z"))
        );

        for(var ti : PrettyTimeInterval.values()) {
            Instant instant = Instant.parse("2021-01-02T02:03:43.000Z");
            assertEquals(ti.getInstantAfter(ti.getInstantBefore(instant)), ti.getInstantAfter(instant));
            assertNotEquals(
                    ti.groupFormat().format(Date.from(ti.getInstantBefore(instant))),
                    ti.groupFormat().format(Date.from(ti.getInstantAfter(instant))));
        }
    }
}
