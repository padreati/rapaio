package rapaio.data.mapping;

import org.junit.jupiter.api.Test;
import rapaio.data.Mapping;
import rapaio.data.VarInt;
import rapaio.util.IntIterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/26/18.
 */
public class ArrayMappingTest {

    @Test
    public void testBuilders() {

        testEquals(new ArrayMapping());
        testEquals(new ArrayMapping(0, 10), 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

        VarInt list = VarInt.wrap(1, 2, 3, 7, 8);
        testEquals(new ArrayMapping(list.elements(), 0, list.rowCount()), list.elements());
        testEquals(new ArrayMapping(list.elements(), 0, list.rowCount(), x -> x + 1), 2, 3, 4, 8, 9);
    }

    private void testEquals(Mapping map, int... values) {
        assertArrayEquals(map.stream().toArray(), values);
    }

    @Test
    void testAddRemoveClear() {

        ArrayMapping mapping = new ArrayMapping(10, 20);
        assertEquals(10, mapping.size());
        assertEquals(10, mapping.get(0));
        assertEquals(19, mapping.get(9));

        mapping.add(30);
        assertEquals(11, mapping.size());
        assertEquals(30, mapping.get(10));

        mapping.addAll(VarInt.wrap(100, 101).iterator());
        assertEquals(13, mapping.size());
        assertEquals(100, mapping.get(11));
        assertEquals(101, mapping.get(12));

        mapping.remove(10);
        assertEquals(12, mapping.size());
        assertEquals(100, mapping.get(10));
        assertEquals(101, mapping.get(11));

        mapping.removeAll(VarInt.wrap(10, 11).iterator());
        assertEquals(10, mapping.size());
        assertEquals(19, mapping.get(9));

        mapping.clear();
        assertEquals(0, mapping.size());
    }

    @Test
    void testStreamsAndCollections() {
        ArrayMapping mapping = new ArrayMapping(10, 20);

        int[] values1 = mapping.elements();
        int[] values2 = mapping.stream().toArray();
        assertArrayEquals(values1, values2);

        IntIterator it = mapping.iterator();
        int index = 0;
        while (it.hasNext()) {
            assertEquals(values1[index++], it.nextInt());
        }
    }
}
