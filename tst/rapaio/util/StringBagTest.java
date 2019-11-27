package rapaio.util;

import org.junit.Test;
import rapaio.data.Mapping;
import rapaio.data.VRange;
import rapaio.datasets.Datasets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/27/19.
 */
public class StringBagTest {

    @Test
    public void testBuildersAndSort() {

        var df = Datasets.loadRandom();

        Mapping mapping = Mapping.range(0, 30);
        VRange vRange = VRange.of("boolean,nominal");

        Set<StringBag> set1 = new HashSet<>();
        Set<StringBag> set2 = new HashSet<>();
        for (int row : mapping) {
            set1.add(StringBag.of(df, row, vRange));

            Map<String, String> bagMap = new HashMap<>();
            bagMap.put("boolean", df.getLabel(row, "boolean"));
            bagMap.put("nominal", df.getLabel(row, "nominal"));

            set2.add(StringBag.of(bagMap));
        }

        Set<StringBag> set3 = df.mapRows(mapping).stream().map(s -> StringBag.of(s, vRange)).collect(Collectors.toSet());

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
    public void testCompareAndEquals() {
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
