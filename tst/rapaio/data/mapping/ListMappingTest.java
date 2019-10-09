package rapaio.data.mapping;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import org.junit.Test;
import rapaio.data.*;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/26/18.
 */
public class ListMappingTest {

    @Test
    public void testBuilders() {

        testEquals(new ListMapping());
        testEquals(new ListMapping(0, 10), 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        testEquals(new ListMapping(new int[]{0, 1, 2, 3}), 0, 1, 2, 3);

        IntArrayList list = new IntArrayList(new int[]{1, 2, 3, 7, 8});
        testEquals(new ListMapping(list, true), list.elements());
        testEquals(new ListMapping(list, x -> x + 1), 2, 3, 4, 8, 9);
    }

    private void testEquals(Mapping map, int... values) {
        assertArrayEquals(map.stream().toArray(), values);
    }

    @Test
    public void testAddRemoveClear() {

        ListMapping mapping = new ListMapping(10, 20);
        assertEquals(10, mapping.size());
        assertEquals(10, mapping.get(0));
        assertEquals(19, mapping.get(9));

        mapping.add(30);
        assertEquals(11, mapping.size());
        assertEquals(30, mapping.get(10));

        mapping.addAll(IntArrayList.wrap(new int[] {100, 101}));
        assertEquals(13, mapping.size());
        assertEquals(100, mapping.get(11));
        assertEquals(101, mapping.get(12));

        mapping.remove(10);
        assertEquals(12, mapping.size());
        assertEquals(100, mapping.get(10));
        assertEquals(101, mapping.get(11));

        mapping.removeAll(IntArrayList.wrap(new int[] {10, 11}));
        assertEquals(10, mapping.size());
        assertEquals(19, mapping.get(9));

        mapping.clear();
        assertEquals(0, mapping.size());
    }

    @Test
    public void testStreamsAndCollections() {
        ListMapping mapping = new ListMapping(10, 20);

        int[] values1 = mapping.toList().toIntArray();
        int[] values2 = mapping.stream().toArray();
        assertArrayEquals(values1, values2);

        IntListIterator it = mapping.iterator();
        while(it.hasNext()) {
            int index = it.nextIndex();
            assertEquals(values1[index], it.nextInt());
        }
    }
}
