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

package rapaio.graphics.base;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.data.VarDouble;
import rapaio.data.VarInstant;
import rapaio.graphics.Plotter;
import rapaio.math.MathTools;
import rapaio.sys.WS;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/11/17.
 */
public class XWilkinsonTest {

    @Test
    void testLocale() {

        XWilkinson.Labels labels1 = XWilkinson.base10(XWilkinson.DEEFAULT_EPS).searchBounded(0.123, 0.96, 5);
        XWilkinson.Labels labels2 = XWilkinson.base10(XWilkinson.DEEFAULT_EPS).searchBounded(0, 10000000, 5);

        assertEquals("0.15;0.3;0.45;0.6;0.75;0.9",
                labels1.getList().stream().map(labels1::getFormattedValue).collect(Collectors.joining(";")));
        assertEquals("0;2,000,000;4,000,000;6,000,000;8,000,000;10,000,000",
                labels2.getList().stream().map(labels2::getFormattedValue).collect(Collectors.joining(";")));

        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("ru-RU"));

        assertEquals("0,15;0,3;0,45;0,6;0,75;0,9",
                labels1.getList().stream().map(labels1::getFormattedValue).collect(Collectors.joining(";")));

        Locale.setDefault(defaultLocale);
    }

    //    @Test
    void baseTest() {
        XWilkinson x = XWilkinson.base10(XWilkinson.DEEFAULT_EPS);

        WS.println(x.searchBounded(1e-20, 3e-20, 10).toString());

        // First examples taken from the paper pg 6, Fig 4
        x.loose = true;
//        System.out.println(x.search(-98.0, 18.0, 3).toString());
        x.loose = false;
//        System.out.println(x.search(-98.0, 18.0, 3).toString());

//        System.out.println();

        x.loose = true;
//        System.out.println(x.search(-1.0, 200.0, 3).toString());
        x.loose = false;
//        System.out.println(x.search(-1.0, 200.0, 3).toString());

//        System.out.println();

        x.loose = true;
//        System.out.println(x.search(119.0, 178.0, 3).toString());
        x.loose = false;
//        System.out.println(x.search(119.0, 178.0, 3).toString());

//        System.out.println();

        x.loose = true;
        System.out.println(x.search(-31.0, 27.0, 4).toString());
        x.loose = false;
        System.out.println(x.search(-31.0, 27.0, 3).toString());

        System.out.println();

        x.loose = true;
        System.out.println(x.search(-55.45, -49.99, 2).toString());
        x.loose = false;
        System.out.println(x.search(-55.45, -49.99, 3).toString());

        System.out.println();
        x.loose = false;
        System.out.println(x.search(0, 100, 2).toString());
        System.out.println(x.search(0, 100, 3).toString());
        System.out.println(x.search(0, 100, 4).toString());
        System.out.println(x.search(0, 100, 5).toString());
        System.out.println(x.search(0, 100, 6).toString());
        System.out.println(x.search(0, 100, 7).toString());
        System.out.println(x.search(0, 100, 8).toString());
        System.out.println(x.search(0, 100, 9).toString());
        System.out.println(x.search(0, 100, 10).toString());

        System.out.println("Some additional tests: Testing with base2");
        x = XWilkinson.base2(XWilkinson.DEEFAULT_EPS);
        System.out.println(x.search(0, 32, 8).toString());

        System.out.println("Quick experiment with minutes: Check the logic");
        x = XWilkinson.forMinutes(XWilkinson.DEEFAULT_EPS);
        System.out.println(x.search(0, 240, 16));
        System.out.println(x.search(0, 240, 9));

        System.out.println("Quick experiment with minutes: Convert values to HH:mm");
        LocalTime start = LocalTime.now();
        LocalTime end = start.plusMinutes(245); // add 4 hrs 5 mins (245 mins) to the start

        int dmin = start.toSecondOfDay() / 60;
        int dmax = end.toSecondOfDay() / 60;
        if (dmin > dmax) {
            // if adding 4 hrs exceeds the midnight simply swap the values this is just an
            // example...
            int swap = dmin;
            dmin = dmax;
            dmax = swap;
        }
        System.out.println("dmin: " + dmin + " dmax: " + dmax);
        XWilkinson.Labels labels = x.search(dmin, dmax, 15);
        System.out.println("labels");
        for (double time = labels.getMin(); time < labels.getMax(); time += labels.getStep()) {
            LocalTime lt = LocalTime.ofSecondOfDay(Double.valueOf(time).intValue() * 60);
            System.out.println(lt);
        }
    }
}
