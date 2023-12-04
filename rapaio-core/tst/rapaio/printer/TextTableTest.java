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

package rapaio.printer;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/25/18.
 */
public class TextTableTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(123);
    }

    @Test
    void testDotCentering() {
        TextTable tt = TextTable.empty(5, 1);
        tt.set(0, 0, "23343.345", 0, '.');
        tt.set(1, 0, "2342342323343.", 0, '.');
        tt.set(2, 0, "343.345", 0, '.');
        tt.set(3, 0, "343.3453424", 0, '.');
        tt.set(4, 0, "2.3454434", 0, '.');

        assertEquals("""
                        23343.345    \s
                2342342323343.       \s
                          343.345    \s
                          343.3453424\s
                            2.3454434\s
                """, tt.getRawText());
    }

    @Test
    void testDotMixt() {
        TextTable tt = TextTable.empty(5, 1);
        tt.set(0, 0, "23343.345", -1, '.');
        tt.set(1, 0, "2342342323343.", -1, '.');
        tt.set(2, 0, "343.345", 0, '.');
        tt.set(3, 0, "343.3453424", -1, '.');
        tt.set(4, 0, "2.3454434", 1, '.');

        assertEquals("""
                        23343.345    \s
                2342342323343.       \s
                          343.345    \s
                          343.3453424\s
                            2.3454434\s
                """, tt.getRawText());
    }

}
