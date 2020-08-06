package rapaio.util.collection;

import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Variance;
import rapaio.data.VarDouble;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static rapaio.util.collection.DoubleArrayTools.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/18/19.
 */
public class DoubleArrayToolsTest {

    private static final double TOL = 1e-12;
    private final Normal normal = Normal.std();

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(1234);
    }

    @Test
    void buildersTest() {
        assertArrayEquals(new double[]{10., 10., 10.}, DoubleArrayTools.newFill(3, 10.));
        assertArrayEquals(new double[]{10., 11., 12.}, DoubleArrayTools.newSeq(10, 13));
        assertArrayEquals(new double[]{4., 9., 16.}, DoubleArrayTools.newFrom(new double[]{1, 2, 3, 4, 5}, 1, 4, x -> x * x));
        assertArrayEquals(new double[]{3., 5.}, DoubleArrayTools.newCopy(new double[]{1, 3, 5, 7}, 1, 3));
    }

    private void testEqualArrays(double[] actual, double... expected) {
        assertArrayEquals(expected, actual, TOL);
    }

    @Test
    void testIterator() {
        double[] array = DoubleArrayTools.newFrom(0, 100, row -> normal.sampleNext());
        DoubleIterator it1 = DoubleArrayTools.iterator(array, 0, 10);
        for (int i = 0; i < 10; i++) {
            assertEquals(array[i], it1.nextDouble(), TOL);
        }
        assertThrows(NoSuchElementException.class, it1::nextDouble);

        DoubleIterator it2 = DoubleArrayTools.iterator(array, 0, 100);
        for (int i = 0; i < 100; i++) {
            assertEquals(array[i], it2.nextDouble(), TOL);
        }
        assertThrows(NoSuchElementException.class, it2::nextDouble);
    }

    @Test
    void testCounts() {
        double[] array1 = DoubleArrayTools.newFrom(0, 100, row -> row % 7 == 0 ? Double.NaN : normal.sampleNext());
        assertEquals(4, nancount(array1, 0, 5));
        assertEquals(6, nancount(array1, 10, 17));
        assertEquals(85, nancount(array1, 0, 100));
    }

    @Test
    void testSums() {
        double[] array1 = DoubleArrayTools.newFrom(0, 100, row -> row % 7 == 0 ? Double.NaN : normal.sampleNext());
        double[] array2 = DoubleArrayTools.newFrom(0, 100, row -> normal.sampleNext());

        for (int i = 0; i < 1000; i++) {
            int start = RandomSource.nextInt(30);
            int mid = RandomSource.nextInt(30) + start;
            int end = RandomSource.nextInt(40) + mid;

            assertEquals(sum(array1, start, mid) + sum(array1, mid, end), sum(array1, start, end), TOL);
            assertEquals(nansum(array1, start, mid) + nansum(array1, mid, end), nansum(array1, start, end), TOL);

            assertEquals(sum(array2, start, mid) + sum(array2, mid, end), sum(array2, start, end), TOL);
            assertEquals(nansum(array2, start, mid) + nansum(array2, mid, end), nansum(array2, start, end), TOL);
        }
    }

    @Test
    void testMeans() {
        double[] array1 = DoubleArrayTools.newFrom(0, 100, row -> row % 7 == 0 ? Double.NaN : normal.sampleNext());
        double[] array2 = DoubleArrayTools.newFrom(0, 100, row -> normal.sampleNext());

        for (int i = 0; i < 1000; i++) {
            int start = RandomSource.nextInt(20) + 10;
            int mid = RandomSource.nextInt(19) + start + 10;
            int end = RandomSource.nextInt(29) + mid + 10;

            assertEquals(mean(array1, start, mid) * (mid - start) + mean(array1, mid, end) * (end - mid),
                    sum(array1, start, end) * (end - start), TOL);
            assertEquals(nanmean(array1, start, mid) * nancount(array1, start, mid) + nanmean(array1, mid, end) * nancount(array1, mid, end),
                    nanmean(array1, start, end) * nancount(array1, start, end), TOL);

            assertEquals(mean(array2, start, mid) * (mid - start) + mean(array2, mid, end) * (end - mid),
                    mean(array2, start, end) * (end - start), TOL);
            assertEquals(nanmean(array2, start, mid) * nancount(array2, start, mid) + nanmean(array2, mid, end) * nancount(array2, mid, end),
                    nanmean(array2, start, end) * nancount(array2, start, end), TOL);


            assertEquals(Double.NaN, mean(array1, 0, 0));
            assertEquals(Double.NaN, nanmean(array1, 10, 10));
        }
    }

    @Test
    void testVariances() {
        double[] array1 = DoubleArrayTools.newFrom(0, 100, row -> row % 7 == 0 ? Double.NaN : row);
        double[] array2 = DoubleArrayTools.newFrom(0, 100, row -> row);

        for (int i = 0; i < 1000; i++) {
            int start = RandomSource.nextInt(50);
            int end = RandomSource.nextInt(49) + 51;

            assertEquals(nanvariance(array1, start, end), Variance.of(VarDouble.wrap(Arrays.copyOfRange(array1, start, end))).value(), TOL);
            assertEquals(variance(array2, start, end), Variance.of(VarDouble.wrap(Arrays.copyOfRange(array2, start, end))).value(), TOL);
        }

        assertEquals(Double.NaN, variance(array1, 0, 0));
        assertEquals(Double.NaN, nanvariance(array1, 10, 10));
    }

    @Test
    void testPlus() {
        double[] array1 = DoubleArrayTools.newFrom(0, 100, row -> row);

        for (int i = 0; i < 100; i++) {
            int start = RandomSource.nextInt(50);
            int end = 50 + RandomSource.nextInt(50);

            double[] array2 = DoubleArrayTools.newCopy(array1, start, end);
            DoubleArrayTools.plus(array2, 10, 0, end - start);
            double[] array3 = DoubleArrayTools.plusc(array1, 10, start, end);

            assertArrayEquals(array2, array3, TOL);

            double[] array4 = DoubleArrayTools.newFill(end - start, 10);
            double[] array5 = DoubleArrayTools.plus(DoubleArrayTools.newCopy(array1, start, end), array4, 0, end - start);
            double[] array6 = DoubleArrayTools.plusc(DoubleArrayTools.newCopy(array1, start, end), array4, 0, end - start);

            assertArrayEquals(array5, array6);
        }
    }

    @Test
    void testMinus() {
        double[] array1 = DoubleArrayTools.newFrom(0, 100, row -> row);

        for (int i = 0; i < 100; i++) {
            int start = RandomSource.nextInt(50);
            int end = 50 + RandomSource.nextInt(50);

            double[] array2 = DoubleArrayTools.newCopy(array1, start, end);
            DoubleArrayTools.minus(array2, 10, 0, end - start);
            double[] array3 = DoubleArrayTools.minusc(array1, 10, start, end);

            assertArrayEquals(array2, array3, TOL);

            double[] array4 = DoubleArrayTools.newFill(end - start, 10);
            double[] array5 = DoubleArrayTools.minus(DoubleArrayTools.newCopy(array1, start, end), array4, 0, end - start);
            double[] array6 = DoubleArrayTools.minusc(DoubleArrayTools.newCopy(array1, start, end), array4, 0, end - start);

            assertArrayEquals(array5, array6);
        }
    }

    @Test
    void testDot() {
        double[] array1 = DoubleArrayTools.newFrom(0, 100, row -> row);

        for (int i = 0; i < 100; i++) {
            int start = RandomSource.nextInt(50);
            int end = 50 + RandomSource.nextInt(50);

            double[] array2 = DoubleArrayTools.newCopy(array1, start, end);
            DoubleArrayTools.times(array2, 10, 0, end - start);
            double[] array3 = DoubleArrayTools.timesc(array1, 10, start, end);

            assertArrayEquals(array2, array3, TOL);

            double[] array4 = DoubleArrayTools.newFill(end - start, 10);
            double[] array5 = DoubleArrayTools.times(DoubleArrayTools.newCopy(array1, start, end), array4, 0, end - start);
            double[] array6 = DoubleArrayTools.timesc(DoubleArrayTools.newCopy(array1, start, end), array4, 0, end - start);

            assertArrayEquals(array5, array6);
        }
    }

    @Test
    void testDiv() {
        double[] array1 = DoubleArrayTools.newFrom(0, 100, row -> row);

        for (int i = 0; i < 100; i++) {
            int start = RandomSource.nextInt(50);
            int end = 50 + RandomSource.nextInt(50);

            double[] array2 = DoubleArrayTools.newCopy(array1, start, end);
            DoubleArrayTools.div(array2, 10, 0, end - start);
            double[] array3 = DoubleArrayTools.divc(array1, 10, start, end);

            assertArrayEquals(array2, array3, TOL);

            double[] array4 = DoubleArrayTools.newFill(end - start, 10);
            double[] array5 = DoubleArrayTools.div(DoubleArrayTools.newCopy(array1, start, end), array4, 0, end - start);
            double[] array6 = DoubleArrayTools.divc(DoubleArrayTools.newCopy(array1, start, end), array4, 0, end - start);

            assertArrayEquals(array5, array6);
        }
    }
}
