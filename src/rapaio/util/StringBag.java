package rapaio.util;

import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.stream.FSpot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

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
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/27/19.
 */
public class StringBag implements Comparable<StringBag> {

    public static StringBag of(Frame df, int row, VRange vRange) {
        Map<String, String> map = new HashMap<>();
        for (String varName : vRange.parseVarNames(df)) {
            map.put(varName, df.getLabel(row, varName));
        }
        return new StringBag(map);
    }

    public static StringBag of(FSpot s, VRange vRange) {
        Map<String, String> map = new HashMap<>();
        for (String varName : vRange.parseVarNames(s.frame())) {
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
}
