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

package rapaio.data.string;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

import rapaio.data.Frame;
import rapaio.data.VarRange;
import rapaio.data.stream.FSpot;

/**
 * Utility collection which holds a map of string to strings and implements
 * comparable and equals properly on values.
 * <p>
 * The utility of this class comes from the fact that it can be build
 * from a row in a data frame mapping specified variables to their
 * label representation. This can also be built from {@link FSpot}.
 * <p>
 * This kind of object can be used to filter or identify rows in data frames
 * in a different way than by row numbers, but from a composed identifier.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/27/19.
 */
public class StringBag implements Comparable<StringBag> {

    public static StringBag of(Frame df, int row, VarRange varRange) {
        Map<String, String> map = new HashMap<>();
        for (String varName : varRange.parseVarNames(df)) {
            map.put(varName, df.getLabel(row, varName));
        }
        return new StringBag(map);
    }

    public static StringBag of(FSpot s, VarRange varRange) {
        Map<String, String> map = new HashMap<>();
        for (String varName : varRange.parseVarNames(s.df())) {
            map.put(varName, s.getLabel(varName));
        }
        return new StringBag(map);
    }

    public static StringBag of(Map<String, String> values) {
        return new StringBag(values);
    }

    private final Map<String, String> map;

    public StringBag(Map<String, String> values) {
        map = new HashMap<>(values);
    }

    @Override
    public int compareTo(StringBag o) {
        if (o == null) {
            return 1;
        }
        TreeSet<String> keys = new TreeSet<>(map.keySet());
        keys.addAll(o.map.keySet());
        for (String key : keys) {
            boolean b1 = map.containsKey(key);
            boolean b2 = o.map.containsKey(key);

            if (!b1) {
                return -1;
            }
            if (!b2) {
                return 1;
            }
            int cmp = map.get(key).compareTo(o.map.get(key));
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringBag stringBag = (StringBag) o;
        return Objects.equals(map, stringBag.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    @Override
    public String toString() {
        return "StringBag {" + map.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(",")) + "}";
    }
}
