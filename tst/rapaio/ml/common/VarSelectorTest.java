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

package rapaio.ml.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.tests.ChiSqGoodnessOfFit;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.math.linear.DVector;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class VarSelectorTest {

    private final String[] varNames = new String[] {"a", "b", "c", "d", "e", "f", "g", "h"};
    private Frame df;
    private String classColName;

    private Random random;

    @BeforeEach
    void setUp() {
        df = SolidFrame.byVars(
                0,
                VarInt.scalar(1).name("a"),
                VarInt.scalar(1).name("b"),
                VarInt.scalar(1).name("c"),
                VarInt.scalar(1).name("d"),
                VarInt.scalar(1).name("e"),
                VarInt.scalar(1).name("f"),
                VarInt.scalar(1).name("g"),
                VarInt.scalar(1).name("h"),
                VarInt.scalar(1).name("class"));
        classColName = "class";

        random = new Random(42);
    }

    @Test
    void testFixedVarSelector() {
        HashMap<String, Integer> counter = new HashMap<>();
        for (int i = 0; i < 1_000; i++) {
            int mcols = 5;
            VarSelector colSelector = VarSelector.fixed(mcols);
            colSelector.withVarNames(varNames);
            String[] selectedVarNames = colSelector.nextVarNames(random);
            assertEquals(mcols, selectedVarNames.length);
            for (String varName : selectedVarNames) {
                if (!counter.containsKey(varName)) {
                    counter.put(varName, 0);
                }
                counter.put(varName, counter.get(varName) + 1);
            }
        }
        final DVector freq = DVector.fill(counter.size(), 0);
        int pos = 0;
        for (int value : counter.values()) {
            freq.set(pos++, value);
        }

        VarDouble expectedProbability = VarDouble.fill(varNames.length, 1.0 / varNames.length);
        VarDouble counts = freq.dv();

        assertTrue(ChiSqGoodnessOfFit.from(counts, expectedProbability).pValue() >= 0.2); // no evidence at all
    }

    @Test
    void testAutoVarSelector() {
        HashMap<String, Integer> counter = new HashMap<>();
        for (int i = 0; i < 1_000; i++) {
            VarSelector colSelector = VarSelector.auto().withVarNames(varNames);
            String[] selectedVarNames = colSelector.nextVarNames(random);
            for (String varName : selectedVarNames) {
                if (!counter.containsKey(varName)) {
                    counter.put(varName, 0);
                }
                counter.put(varName, counter.get(varName) + 1);
            }
        }
        final DVector freq = DVector.fill(counter.size(), 0);
        int pos = 0;
        for (int value : counter.values()) {
            freq.set(pos++, value);
        }

        VarDouble expectedProbability = VarDouble.fill(varNames.length, 1.0 / varNames.length);
        VarDouble counts = freq.dv();

        assertTrue(ChiSqGoodnessOfFit.from(counts, expectedProbability).pValue() >= 0.2); // no evidence at all
    }

    @Test
    void testAllVarSelector() {
        HashMap<String, Integer> counter = new HashMap<>();
        for (int i = 0; i < 1_000; i++) {
            VarSelector colSelector = VarSelector.all();
            colSelector.withVarNames(varNames);
            String[] selectedVarNames = colSelector.nextVarNames(random);
            assertEquals(varNames.length, selectedVarNames.length);
            for (String varName : selectedVarNames) {
                if (!counter.containsKey(varName)) {
                    counter.put(varName, 0);
                }
                counter.put(varName, counter.get(varName) + 1);
            }
        }
        for (String varName : varNames) {
            assertEquals(1000, (int) counter.get(varName));
        }
    }

    @Test
    void testNames() {
        assertEquals("VarSelector[ALL]", VarSelector.all().name());
        assertEquals("VarSelector[4]", VarSelector.fixed(4).name());
        assertEquals("VarSelector[AUTO]", VarSelector.auto().name());
    }

    @Test
    void testMCount() {
        assertEquals(4, VarSelector.fixed(4).withVarNames(varNames).mCount());
    }

    @Test
    void testAddRemoveVars() {

        int len = varNames.length;

        VarSelector selector = VarSelector.all().withVarNames(varNames);

        for (int i = 0; i < len; i++) {
            selector.removeVarNames(Collections.singleton(varNames[i]));
            selector = selector.newInstance();
            assertEquals(len - i - 1, selector.mCount());
        }

        for (int i = 0; i < len; i++) {
            selector.addVarNames(Collections.singleton(varNames[i]));
            assertEquals(i + 1, selector.mCount());
        }
    }
}
