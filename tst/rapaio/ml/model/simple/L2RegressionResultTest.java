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

package rapaio.ml.model.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/9/19.
 */
public class L2RegressionResultTest {

    private static final double TOL = 1e-20;
    private Frame df;
    private Frame bigDf;

    @BeforeEach
    void setUp() throws IOException {
        df = Datasets.loadISLAdvertising();
        Var[] vars = new Var[30];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VarDouble.seq(30).name("x" + (i + 1));
        }
        bigDf = SolidFrame.byVars(vars);
    }

    @Test
    void testNaming() {
        L2Regression model = L2Regression.newModel();
        assertEquals("L2Regression", model.name());
        assertEquals("L2Regression{}", model.fullName());

        assertEquals("L2Regression{}; not fitted", model.toString());
        assertEquals("L2Regression{}; fitted values={Sales:14.0225,Radio:23.264}", model.fit(df, "Sales,Radio").toString());
        assertEquals("L2Regression{}; fitted values={x1:15,x2:15,x3:15,x4:15,x5:15,...}", model.fit(bigDf, "x1,x2,x3,x4,x5,x6,x7,x8").toString());

        assertEquals("""
                Regression predict summary
                =======================
                Model class: L2Regression
                Model instance: L2Regression{}
                > model is trained.
                > input variables:\s
                1. TV        dbl\s
                2. Radio     dbl\s
                3. Newspaper dbl\s
                > target variables:\s
                1. Sales dbl\s

                Fitted values:

                    Target Fitted value\s
                [0]  Sales   14.0225   \s

                """, model.fit(df, "Sales").toContent());

        assertEquals("""
                Regression predict summary
                =======================
                Model class: L2Regression
                Model instance: L2Regression{}
                > model is trained.
                > input variables:\s
                1. TV        dbl\s
                2. Radio     dbl\s
                3. Newspaper dbl\s
                > target variables:\s
                1. Sales dbl\s

                Fitted values:

                    Target Fitted value\s
                [0]  Sales   14.0225   \s

                """, model.fit(df, "Sales").toFullContent());

        assertEquals("""
                Regression predict summary
                =======================
                Model class: L2Regression
                Model instance: L2Regression{}
                > model is trained.
                > input variables:\s
                1. TV        dbl\s
                2. Radio     dbl\s
                3. Newspaper dbl\s
                > target variables:\s
                1. Sales dbl\s

                Fitted values:

                    Target Fitted value\s
                [0]  Sales   14.0225   \s

                """, model.fit(df, "Sales").toSummary());

        assertEquals("""
                Regression predict summary
                =======================
                Model class: L2Regression
                Model instance: L2Regression{}
                > model is trained.
                > input variables:\s
                1. x23 dbl\s
                2. x24 dbl\s
                3. x25 dbl\s
                4. x26 dbl\s
                5. x27 dbl\s
                6. x28 dbl\s
                7. x29 dbl\s
                8. x30 dbl\s
                > target variables:\s
                 1. x1  dbl\s
                 2. x2  dbl\s
                 3. x3  dbl\s
                 4. x4  dbl\s
                 5. x5  dbl\s
                 6. x6  dbl\s
                 7. x7  dbl\s
                 8. x8  dbl\s
                 9. x9  dbl\s
                10. x10 dbl\s
                11. x11 dbl\s
                12. x12 dbl\s
                13. x13 dbl\s
                14. x14 dbl\s
                15. x15 dbl\s
                16. x16 dbl\s
                17. x17 dbl\s
                18. x18 dbl\s
                19. x19 dbl\s
                20. x20 dbl\s
                21. x21 dbl\s
                22. x22 dbl\s

                Fitted values:

                     Target Fitted value      Target Fitted value      Target Fitted value\s
                 [0]     x1      15       [6]     x7      15      [18]    x19      15     \s
                 [1]     x2      15       [7]     x8      15      [19]    x20      15     \s
                 [2]     x3      15       [8]     x9      15      [20]    x21      15     \s
                 [3]     x4      15       [9]    x10      15      [21]    x22      15     \s
                 [4]     x5      15            ...       ...     \s
                 [5]     x6      15      [17]    x18      15     \s

                """, model.fit(bigDf, "x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14,x15,x16,x17,x18,x19,x20,x21,x22").toContent());
        assertEquals("""
                Regression predict summary
                =======================
                Model class: L2Regression
                Model instance: L2Regression{}
                > model is trained.
                > input variables:\s
                1. x23 dbl\s
                2. x24 dbl\s
                3. x25 dbl\s
                4. x26 dbl\s
                5. x27 dbl\s
                6. x28 dbl\s
                7. x29 dbl\s
                8. x30 dbl\s
                > target variables:\s
                 1. x1  dbl\s
                 2. x2  dbl\s
                 3. x3  dbl\s
                 4. x4  dbl\s
                 5. x5  dbl\s
                 6. x6  dbl\s
                 7. x7  dbl\s
                 8. x8  dbl\s
                 9. x9  dbl\s
                10. x10 dbl\s
                11. x11 dbl\s
                12. x12 dbl\s
                13. x13 dbl\s
                14. x14 dbl\s
                15. x15 dbl\s
                16. x16 dbl\s
                17. x17 dbl\s
                18. x18 dbl\s
                19. x19 dbl\s
                20. x20 dbl\s
                21. x21 dbl\s
                22. x22 dbl\s

                Fitted values:

                     Target Fitted value      Target Fitted value      Target Fitted value      Target Fitted value\s
                 [0]     x1      15       [6]     x7      15      [12]    x13      15      [18]    x19      15     \s
                 [1]     x2      15       [7]     x8      15      [13]    x14      15      [19]    x20      15     \s
                 [2]     x3      15       [8]     x9      15      [14]    x15      15      [20]    x21      15     \s
                 [3]     x4      15       [9]    x10      15      [15]    x16      15      [21]    x22      15     \s
                 [4]     x5      15      [10]    x11      15      [16]    x17      15     \s
                 [5]     x6      15      [11]    x12      15      [17]    x18      15     \s

                """, model.fit(bigDf, "x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14,x15,x16,x17,x18,x19,x20,x21,x22").toFullContent());

        assertEquals("""
                Regression predict summary
                =======================
                Model class: L2Regression
                Model instance: L2Regression{}
                > model is trained.
                > input variables:\s
                1. x23 dbl\s
                2. x24 dbl\s
                3. x25 dbl\s
                4. x26 dbl\s
                5. x27 dbl\s
                6. x28 dbl\s
                7. x29 dbl\s
                8. x30 dbl\s
                > target variables:\s
                 1. x1  dbl\s
                 2. x2  dbl\s
                 3. x3  dbl\s
                 4. x4  dbl\s
                 5. x5  dbl\s
                 6. x6  dbl\s
                 7. x7  dbl\s
                 8. x8  dbl\s
                 9. x9  dbl\s
                10. x10 dbl\s
                11. x11 dbl\s
                12. x12 dbl\s
                13. x13 dbl\s
                14. x14 dbl\s
                15. x15 dbl\s
                16. x16 dbl\s
                17. x17 dbl\s
                18. x18 dbl\s
                19. x19 dbl\s
                20. x20 dbl\s
                21. x21 dbl\s
                22. x22 dbl\s

                Fitted values:

                     Target Fitted value      Target Fitted value      Target Fitted value\s
                 [0]     x1      15       [6]     x7      15      [18]    x19      15     \s
                 [1]     x2      15       [7]     x8      15      [19]    x20      15     \s
                 [2]     x3      15       [8]     x9      15      [20]    x21      15     \s
                 [3]     x4      15       [9]    x10      15      [21]    x22      15     \s
                 [4]     x5      15            ...       ...     \s
                 [5]     x6      15      [17]    x18      15     \s

                """, model.fit(bigDf, "x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14,x15,x16,x17,x18,x19,x20,x21,x22").toSummary());
    }

    @Test
    void testPrediction() {
        L2Regression model = L2Regression.newModel().newInstance().fit(df, "Sales");
        var result = model.predict(df);
        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(model.getMeans()[0], result.firstPrediction().getDouble(i), TOL);
        }
    }
}
