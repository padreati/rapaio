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

package rapaio.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.printer.Printer.textWidth;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.format.InstantFormatter;
import rapaio.data.format.InstantParser;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/10/20.
 */
public class VarInstantTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void testConstructors() {
        var t1 = VarInstant.empty(10);
        for (int i = 0; i < t1.size(); i++) {
            assertTrue(t1.isMissing(i));
            assertNull(t1.getInstant(i));
            assertEquals("?", t1.getLabel(i));
        }

        assertEquals("VarInstant", t1.toStringClassName());
        assertEquals(VarType.INSTANT, t1.type());

        ZonedDateTime start = ZonedDateTime.now();
        var t2 = VarInstant.from(10, row -> Instant.from(start.plus(row, ChronoUnit.DAYS)));
        for (int i = 0; i < t2.size(); i++) {
            assertEquals(start.plus(i, ChronoUnit.DAYS).toInstant(), t2.getInstant(i));
        }

        var t3 = VarInstant.from(1, 2, 3);
        assertEquals(3, t3.size());
        assertEquals(1, t3.getLong(0));
        assertEquals(3, t3.getLong(2));
        assertEquals(Instant.ofEpochMilli(1), t3.getInstant(0));
        assertEquals(Instant.ofEpochMilli(2), t3.getInstant(1));
    }

    @Test
    void testParserAndFormatter() {
        var t = VarInstant.empty(10);
        assertEquals(t.getParser(), InstantParser.ISO);
        assertEquals(t.getFormatter(), InstantFormatter.ISO);

        t.setLabel(0, "?");
        t.setLabel(1, "2007-12-03T10:15:30.000Z");
        t.setLabel(4, "2007-12-03T10:15:30.010Z");
        t.addLabel("2007-12-03T10:15:30.010Z");

        assertNull(t.getInstant(0));
        assertEquals(Instant.parse("2007-12-03T10:15:30.00Z"), t.getInstant(1));
        assertEquals("2007-12-03T10:15:30Z", t.getLabel(1));
        assertEquals("2007-12-03T10:15:30.010Z", t.getLabel(4));
        assertNull(t.getInstant(2));
        assertEquals("?", t.getLabel(3));
        assertEquals("2007-12-03T10:15:30.010Z", t.getLabel(10));

        // sets different parsers

        var t2 = VarInstant.empty(1);
        t2.withParser(input -> input.equals("?") ? null : Instant.from(ZonedDateTime.of(Integer.parseInt(input),
                1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))));
        t2.withFormatter(value -> value == null ? "?" : DateTimeFormatter.ofPattern("yyyy").withZone(ZoneId.of("UTC")).format(value));

        t2.setLabel(0, "1999");
        t2.addLabel("2020");
        t2.addLabel("?");

        assertEquals("1999", t2.getLabel(0));
        assertEquals("2020", t2.getLabel(1));
        assertEquals("?", t2.getLabel(2));


        assertEquals(Instant.from(ZonedDateTime.of(1999,
                1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))), t2.getInstant(0));
    }

    @Test
    void testLongRepresentation() {
        var t = VarInstant.empty(1);
        t.setLong(0, 100);
        t.addLong(200);
        t.addMissing();

        assertEquals(3, t.size());
        assertEquals(Instant.ofEpochMilli(100), t.getInstant(0));
        assertEquals(Instant.ofEpochMilli(200), t.getInstant(1));
        assertEquals(200, t.getLong(1));
        assertNull(t.getInstant(2));
    }

    @Test
    void testPrintable() {
        var t = VarInstant.from(20, row -> Instant.ofEpochMilli(row * 1_000_000));
        assertEquals("VarInstant [name:\"?\", rowCount:20, values: 1970-01-01T00:00:00Z, 1970-01-01T00:16:40Z, " +
                "1970-01-01T00:33:20Z, 1970-01-01T00:50:00Z, 1970-01-01T01:06:40Z, 1970-01-01T01:23:20Z, ..., " +
                "1970-01-01T05:00:00Z, 1970-01-01T05:16:40Z]", t.toString());

        t = VarInstant.from(120, row -> Instant.ofEpochMilli(row * 1_000_000));
        assertEquals("VarInstant [name:\"?\", rowCount:120]\n" +
                " row         value          row         value          row         value          row         value         \n" +
                "  [0] 1970-01-01T00:00:00Z  [26] 1970-01-01T07:13:20Z  [52] 1970-01-01T14:26:40Z  [78] 1970-01-01T21:40:00Z \n" +
                "  [1] 1970-01-01T00:16:40Z  [27] 1970-01-01T07:30:00Z  [53] 1970-01-01T14:43:20Z  ...          ...          \n" +
                "  [2] 1970-01-01T00:33:20Z  [28] 1970-01-01T07:46:40Z  [54] 1970-01-01T15:00:00Z [100] 1970-01-02T03:46:40Z \n" +
                "  [3] 1970-01-01T00:50:00Z  [29] 1970-01-01T08:03:20Z  [55] 1970-01-01T15:16:40Z [101] 1970-01-02T04:03:20Z \n" +
                "  [4] 1970-01-01T01:06:40Z  [30] 1970-01-01T08:20:00Z  [56] 1970-01-01T15:33:20Z [102] 1970-01-02T04:20:00Z \n" +
                "  [5] 1970-01-01T01:23:20Z  [31] 1970-01-01T08:36:40Z  [57] 1970-01-01T15:50:00Z [103] 1970-01-02T04:36:40Z \n" +
                "  [6] 1970-01-01T01:40:00Z  [32] 1970-01-01T08:53:20Z  [58] 1970-01-01T16:06:40Z [104] 1970-01-02T04:53:20Z \n" +
                "  [7] 1970-01-01T01:56:40Z  [33] 1970-01-01T09:10:00Z  [59] 1970-01-01T16:23:20Z [105] 1970-01-02T05:10:00Z \n" +
                "  [8] 1970-01-01T02:13:20Z  [34] 1970-01-01T09:26:40Z  [60] 1970-01-01T16:40:00Z [106] 1970-01-02T05:26:40Z \n" +
                "  [9] 1970-01-01T02:30:00Z  [35] 1970-01-01T09:43:20Z  [61] 1970-01-01T16:56:40Z [107] 1970-01-02T05:43:20Z \n" +
                " [10] 1970-01-01T02:46:40Z  [36] 1970-01-01T10:00:00Z  [62] 1970-01-01T17:13:20Z [108] 1970-01-02T06:00:00Z \n" +
                " [11] 1970-01-01T03:03:20Z  [37] 1970-01-01T10:16:40Z  [63] 1970-01-01T17:30:00Z [109] 1970-01-02T06:16:40Z \n" +
                " [12] 1970-01-01T03:20:00Z  [38] 1970-01-01T10:33:20Z  [64] 1970-01-01T17:46:40Z [110] 1970-01-02T06:33:20Z \n" +
                " [13] 1970-01-01T03:36:40Z  [39] 1970-01-01T10:50:00Z  [65] 1970-01-01T18:03:20Z [111] 1970-01-02T06:50:00Z \n" +
                " [14] 1970-01-01T03:53:20Z  [40] 1970-01-01T11:06:40Z  [66] 1970-01-01T18:20:00Z [112] 1970-01-02T07:06:40Z \n" +
                " [15] 1970-01-01T04:10:00Z  [41] 1970-01-01T11:23:20Z  [67] 1970-01-01T18:36:40Z [113] 1970-01-02T07:23:20Z \n" +
                " [16] 1970-01-01T04:26:40Z  [42] 1970-01-01T11:40:00Z  [68] 1970-01-01T18:53:20Z [114] 1970-01-02T07:40:00Z \n" +
                " [17] 1970-01-01T04:43:20Z  [43] 1970-01-01T11:56:40Z  [69] 1970-01-01T19:10:00Z [115] 1970-01-02T07:56:40Z \n" +
                " [18] 1970-01-01T05:00:00Z  [44] 1970-01-01T12:13:20Z  [70] 1970-01-01T19:26:40Z [116] 1970-01-02T08:13:20Z \n" +
                " [19] 1970-01-01T05:16:40Z  [45] 1970-01-01T12:30:00Z  [71] 1970-01-01T19:43:20Z [117] 1970-01-02T08:30:00Z \n" +
                " [20] 1970-01-01T05:33:20Z  [46] 1970-01-01T12:46:40Z  [72] 1970-01-01T20:00:00Z [118] 1970-01-02T08:46:40Z \n" +
                " [21] 1970-01-01T05:50:00Z  [47] 1970-01-01T13:03:20Z  [73] 1970-01-01T20:16:40Z [119] 1970-01-02T09:03:20Z \n" +
                " [22] 1970-01-01T06:06:40Z  [48] 1970-01-01T13:20:00Z  [74] 1970-01-01T20:33:20Z                            \n" +
                " [23] 1970-01-01T06:23:20Z  [49] 1970-01-01T13:36:40Z  [75] 1970-01-01T20:50:00Z \n" +
                " [24] 1970-01-01T06:40:00Z  [50] 1970-01-01T13:53:20Z  [76] 1970-01-01T21:06:40Z \n" +
                " [25] 1970-01-01T06:56:40Z  [51] 1970-01-01T14:10:00Z  [77] 1970-01-01T21:23:20Z \n",
                t.toContent(textWidth(100)));

        assertEquals("VarInstant [name:\"?\", rowCount:120]\n" +
                " row         value          row         value          row         value          row         value         \n" +
                "  [0] 1970-01-01T00:00:00Z  [30] 1970-01-01T08:20:00Z  [60] 1970-01-01T16:40:00Z  [90] 1970-01-02T01:00:00Z \n" +
                "  [1] 1970-01-01T00:16:40Z  [31] 1970-01-01T08:36:40Z  [61] 1970-01-01T16:56:40Z  [91] 1970-01-02T01:16:40Z \n" +
                "  [2] 1970-01-01T00:33:20Z  [32] 1970-01-01T08:53:20Z  [62] 1970-01-01T17:13:20Z  [92] 1970-01-02T01:33:20Z \n" +
                "  [3] 1970-01-01T00:50:00Z  [33] 1970-01-01T09:10:00Z  [63] 1970-01-01T17:30:00Z  [93] 1970-01-02T01:50:00Z \n" +
                "  [4] 1970-01-01T01:06:40Z  [34] 1970-01-01T09:26:40Z  [64] 1970-01-01T17:46:40Z  [94] 1970-01-02T02:06:40Z \n" +
                "  [5] 1970-01-01T01:23:20Z  [35] 1970-01-01T09:43:20Z  [65] 1970-01-01T18:03:20Z  [95] 1970-01-02T02:23:20Z \n" +
                "  [6] 1970-01-01T01:40:00Z  [36] 1970-01-01T10:00:00Z  [66] 1970-01-01T18:20:00Z  [96] 1970-01-02T02:40:00Z \n" +
                "  [7] 1970-01-01T01:56:40Z  [37] 1970-01-01T10:16:40Z  [67] 1970-01-01T18:36:40Z  [97] 1970-01-02T02:56:40Z \n" +
                "  [8] 1970-01-01T02:13:20Z  [38] 1970-01-01T10:33:20Z  [68] 1970-01-01T18:53:20Z  [98] 1970-01-02T03:13:20Z \n" +
                "  [9] 1970-01-01T02:30:00Z  [39] 1970-01-01T10:50:00Z  [69] 1970-01-01T19:10:00Z  [99] 1970-01-02T03:30:00Z \n" +
                " [10] 1970-01-01T02:46:40Z  [40] 1970-01-01T11:06:40Z  [70] 1970-01-01T19:26:40Z [100] 1970-01-02T03:46:40Z \n" +
                " [11] 1970-01-01T03:03:20Z  [41] 1970-01-01T11:23:20Z  [71] 1970-01-01T19:43:20Z [101] 1970-01-02T04:03:20Z \n" +
                " [12] 1970-01-01T03:20:00Z  [42] 1970-01-01T11:40:00Z  [72] 1970-01-01T20:00:00Z [102] 1970-01-02T04:20:00Z \n" +
                " [13] 1970-01-01T03:36:40Z  [43] 1970-01-01T11:56:40Z  [73] 1970-01-01T20:16:40Z [103] 1970-01-02T04:36:40Z \n" +
                " [14] 1970-01-01T03:53:20Z  [44] 1970-01-01T12:13:20Z  [74] 1970-01-01T20:33:20Z [104] 1970-01-02T04:53:20Z \n" +
                " [15] 1970-01-01T04:10:00Z  [45] 1970-01-01T12:30:00Z  [75] 1970-01-01T20:50:00Z [105] 1970-01-02T05:10:00Z \n" +
                " [16] 1970-01-01T04:26:40Z  [46] 1970-01-01T12:46:40Z  [76] 1970-01-01T21:06:40Z [106] 1970-01-02T05:26:40Z \n" +
                " [17] 1970-01-01T04:43:20Z  [47] 1970-01-01T13:03:20Z  [77] 1970-01-01T21:23:20Z [107] 1970-01-02T05:43:20Z \n" +
                " [18] 1970-01-01T05:00:00Z  [48] 1970-01-01T13:20:00Z  [78] 1970-01-01T21:40:00Z [108] 1970-01-02T06:00:00Z \n" +
                " [19] 1970-01-01T05:16:40Z  [49] 1970-01-01T13:36:40Z  [79] 1970-01-01T21:56:40Z [109] 1970-01-02T06:16:40Z \n" +
                " [20] 1970-01-01T05:33:20Z  [50] 1970-01-01T13:53:20Z  [80] 1970-01-01T22:13:20Z [110] 1970-01-02T06:33:20Z \n" +
                " [21] 1970-01-01T05:50:00Z  [51] 1970-01-01T14:10:00Z  [81] 1970-01-01T22:30:00Z [111] 1970-01-02T06:50:00Z \n" +
                " [22] 1970-01-01T06:06:40Z  [52] 1970-01-01T14:26:40Z  [82] 1970-01-01T22:46:40Z [112] 1970-01-02T07:06:40Z \n" +
                " [23] 1970-01-01T06:23:20Z  [53] 1970-01-01T14:43:20Z  [83] 1970-01-01T23:03:20Z [113] 1970-01-02T07:23:20Z \n" +
                " [24] 1970-01-01T06:40:00Z  [54] 1970-01-01T15:00:00Z  [84] 1970-01-01T23:20:00Z [114] 1970-01-02T07:40:00Z \n" +
                " [25] 1970-01-01T06:56:40Z  [55] 1970-01-01T15:16:40Z  [85] 1970-01-01T23:36:40Z [115] 1970-01-02T07:56:40Z \n" +
                " [26] 1970-01-01T07:13:20Z  [56] 1970-01-01T15:33:20Z  [86] 1970-01-01T23:53:20Z [116] 1970-01-02T08:13:20Z \n" +
                " [27] 1970-01-01T07:30:00Z  [57] 1970-01-01T15:50:00Z  [87] 1970-01-02T00:10:00Z [117] 1970-01-02T08:30:00Z \n" +
                " [28] 1970-01-01T07:46:40Z  [58] 1970-01-01T16:06:40Z  [88] 1970-01-02T00:26:40Z [118] 1970-01-02T08:46:40Z \n" +
                " [29] 1970-01-01T08:03:20Z  [59] 1970-01-01T16:23:20Z  [89] 1970-01-02T00:43:20Z [119] 1970-01-02T09:03:20Z \n", t.toFullContent(textWidth(100)));
    }
}
