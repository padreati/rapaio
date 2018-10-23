package rapaio.data.mapping;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import org.junit.Test;
import rapaio.data.Mapping;

import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/26/18.
 */
public class IntervalMappingTest {

    @Test
    public void testBuilders() {
        Mapping mapping1 = new IntervalMapping(10, 20);
        Mapping mapping2 = new IntervalMapping(10, 19);
        mapping2.add(19);
        Mapping mapping3 = new IntervalMapping(10, 15);
        mapping3.addAll(IntArrayList.wrap(new int[]{15, 16, 17}));
        mapping3.addAll(IntArrayList.wrap(new int[]{18, 19}));

        assertEquals(10, mapping1.size());
        assertEquals(10, mapping2.size());
        assertArrayEquals(mapping1.toList().toArray(), mapping2.toList().toArray());
        assertArrayEquals(mapping1.toList().toArray(), mapping3.toList().toArray());

        assertEquals(mapping1.get(0), mapping2.get(0));


        mapping1.remove(9);
        mapping2.remove(9);
        mapping3.remove(9);
        assertArrayEquals(mapping1.toList().toArray(), mapping2.toList().toArray());
        assertArrayEquals(mapping1.toList().toArray(), mapping3.toList().toArray());

        mapping1 = new IntervalMapping(10, 20);
        mapping1.removeAll(IntArrayList.wrap(new int[]{7, 8, 9}));
        mapping2.removeAll(IntArrayList.wrap(new int[]{7, 8}));
        mapping3.removeAll(IntArrayList.wrap(new int[]{7, 8}));
        assertArrayEquals(mapping1.toList().toArray(), mapping2.toList().toArray());
        assertArrayEquals(mapping1.toList().toArray(), mapping3.toList().toArray());

        int[] arr = mapping1.stream().toArray();
        assertArrayEquals(arr, mapping2.toList().toIntArray());

        mapping1 = new IntervalMapping(10, 20);
        mapping1.clear();
        mapping2.clear();
        assertEquals(0, mapping1.size());
    }

    @Test
    public void testCollector() {

        Mapping mapping = IntStream.range(0, 100).boxed().collect(Mapping.collector());
        assertEquals(100, mapping.size());

        double sum = IntStream.range(0, 100).boxed().parallel().collect(Mapping.collector()).stream().mapToDouble(x->x).sum();
        assertEquals(4950, sum, 1e-20);

        IntListIterator it = mapping.iterator();
        assertArrayEquals(new IntArrayList(it).toIntArray(), mapping.toList().toIntArray());
        assertArrayEquals(new IntArrayList(new IntervalMapping(0, 100).iterator()).toIntArray(), mapping.toList().toIntArray());

        it = new IntervalMapping(0, 100).iterator();

        assertEquals(0, it.nextIndex());
        assertEquals(0, it.nextInt());
        assertEquals(1, it.nextIndex());
        assertEquals(1, it.nextInt());

        assertEquals(1, it.previousIndex());
        assertEquals(1, it.previousInt());
        assertTrue(it.hasPrevious());
    }
}
