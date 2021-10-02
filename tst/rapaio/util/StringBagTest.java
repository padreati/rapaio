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

package rapaio.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import rapaio.data.Mapping;
import rapaio.data.VarRange;
import rapaio.datasets.Datasets;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/27/19.
 */
public class StringBagTest {

    @Test
    void testBuildersAndSort() {

        var df = Datasets.loadRandom();

        Mapping mapping = Mapping.range(0, 30);
        VarRange varRange = VarRange.of("boolean,nominal");

        Set<StringBag> set1 = new HashSet<>();
        Set<StringBag> set2 = new HashSet<>();
        for (int row : mapping) {
            set1.add(StringBag.of(df, row, varRange));

            Map<String, String> bagMap = new HashMap<>();
            bagMap.put("boolean", df.getLabel(row, "boolean"));
            bagMap.put("nominal", df.getLabel(row, "nominal"));

            set2.add(StringBag.of(bagMap));
        }

        Set<StringBag> set3 = df.mapRows(mapping).stream().map(s -> StringBag.of(s, varRange)).collect(Collectors.toSet());

        assertEquals(set1, set2);
        assertEquals(set1, set3);

        List<StringBag> list1 = new ArrayList<>(set1);
        List<StringBag> list2 = new ArrayList<>(set2);
        List<StringBag> list3 = new ArrayList<>(set3);

        assertEquals(list1.size(), list2.size());
        assertEquals(list1.size(), list3.size());

        list1.sort(StringBag::compareTo);
        list2.sort(StringBag::compareTo);
        list3.sort(StringBag::compareTo);

        for (int i = 0; i < list1.size(); i++) {
            assertEquals(list1.get(i), list2.get(i));
            assertEquals(list1.get(i), list3.get(i));
        }
    }

    @Test
    void testCompareAndEquals() {
        StringBag sb1 = StringBag.of(Map.of("k1", "v1", "k2", "v2"));
        StringBag sb2 = StringBag.of(Map.of("k1", "v1", "k2", "v2"));

        StringBag lower1 = StringBag.of(Map.of("k2", "v2", "k3", "v3"));
        StringBag lower2 = StringBag.of(Map.of("k1", "v0"));

        StringBag higher1 = StringBag.of(Map.of("k0", "v0"));
        StringBag higher2 = StringBag.of(Map.of("k1", "v2"));

        assertEquals(0, sb1.compareTo(sb2));
        assertEquals(sb1, sb2);

        assertEquals(-1, lower1.compareTo(sb1));
        assertEquals(-1, lower2.compareTo(sb2));

        assertEquals(1, higher1.compareTo(sb1));
        assertEquals(1, higher2.compareTo(sb1));
    }
}
