/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.graphics.plot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import static rapaio.graphics.Plotter.funLine;
import static rapaio.graphics.opt.GOptions.color;
import static rapaio.graphics.opt.GOptions.labels;

import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.core.distributions.StudentT;
import rapaio.sys.WS;

public class LegendBugTest {

    @Test
    void bug() {
        Normal n = Normal.std();
        StudentT t1 = StudentT.of(1);
        StudentT t2 = StudentT.of(2);
        StudentT t10 = StudentT.of(10);
        StudentT t100 = StudentT.of(100);

        Plot p = funLine(n::pdf, color(1))
                .funLine(t1::pdf, color(2))
                .funLine(t2::pdf, color(3))
                .funLine(t10::pdf, color(4))
                .funLine(t100::pdf, color(5))
                .xLim(-10, 10).yLim(0, 0.45)
                .legend(3, 0.4, color(1, 2, 3, 4, 5), labels("normal", "t1", "t2", "t10", "t100"));

        assertDoesNotThrow(() -> WS.image(p));
    }
}
