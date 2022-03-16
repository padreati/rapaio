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

package rapaio.experiment;

import static rapaio.graphics.Plotter.*;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

import rapaio.data.Mapping;
import rapaio.data.Unique;
import rapaio.data.VarDouble;
import rapaio.data.VarString;
import rapaio.graphics.Plotter;
import rapaio.sys.WS;
import rapaio.sys.With;

public class Sandbox {

    public static void main(String[] args) {

        Instant start = Instant.parse("2022-01-01T00:00:00.00Z");
        Instant end = Instant.parse("2023-01-01T00:00:00.00Z");


        VarString vday = VarString.empty(0).name("day");
        VarDouble vsum = VarDouble.empty(0).name("sum");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        while (start.isBefore(end)) {

            var local = start.atZone(ZoneId.of("UTC"));
            int year = local.getYear();
            int year_1 = year / 100;
            int year_2 = year % 100;
            int month = local.getMonthValue();
            int day = local.getDayOfMonth();

            vday.addLabel(sdf.format(Date.from(start)));
            vsum.addDouble(year_1 + year_2 + month + day);

            start = start.plus(1, ChronoUnit.DAYS);
        }

        Unique.of(vsum, true).printFullContent(With.textWidth(50));
        int id = Unique.of(vsum, true).valueSortedIds().getInt(68 - 44);
        Mapping mapping = Unique.of(vsum, true).rowList(id);
        vday.mapRows(mapping).printFullContent();

        System.out.println(100 * Math.pow(12 / 365., 3) + "%");
        System.out.println(Math.pow(365., 3)/Math.pow(12,3));

        WS.draw(hist(vsum, With.bins(85 - 44 + 1)).title("Frequency of sum for 2022"));
        WS.saveImage(hist(vsum, With.bins(85 - 44 + 1)).title("Frequency of sum for 2022"),
                800, 600, "/tmp/hist.png");
    }
}
