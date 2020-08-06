package rapaio.util.collection;


import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.DoubleStream;

/**
 * Utility class to handle the manipulation of arrays of double 64 floating values.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/11/19.
 */
public final class DoubleArrayTools {

    private DoubleArrayTools() {
    }

    /**
     * Creates a double array filled with a given value
     *
     * @param size      size of the array
     * @param fillValue value to fill the array
     * @return new array instance
     */
    public static double[] newFill(int size, double fillValue) {
        double[] array = new double[size];
        if (fillValue != 0) {
            Arrays.fill(array, fillValue);
        }
        return array;
    }

    /**
     * Creates a new array filled with a sequence of values starting from
     * {@param start} (inclusive) and ending with {@param end} (exclusive)
     *
     * @param start sequence starting value (inclusive)
     * @param end   sequence ending value (exclusive)
     * @return array with sequence values
     */
    public static double[] newSeq(int start, int end) {
        double[] data = new double[end - start];
        for (int i = 0; i < end - start; i++) {
            data[i] = start + i;
        }
        return data;
    }

    /**
     * Builds a new double array with values from the given chunk transformed
     * with a function.
     *
     * @param source source array
     * @param start  starting position from source array (inclusive)
     * @param end    ending position from source array (exclusive)
     * @param fun    transforming function
     * @return transformed values array
     */
    public static double[] newFrom(double[] source, int start, int end, Double2DoubleFunction fun) {
        double[] data = new double[end - start];
        for (int i = start; i < end; i++) {
            data[i - start] = fun.applyAsDouble(source[i]);
        }
        return data;
    }

    /**
     * Builds a new double array with values from the given chunk transformed
     * with a function.
     *
     * @param start starting position from source array (inclusive)
     * @param end   ending position from source array (exclusive)
     * @param fun   transforming function
     * @return transformed values array
     */
    public static double[] newFrom(int start, int end, Int2DoubleFunction fun) {
        double[] data = new double[end - start];
        for (int i = start; i < end; i++) {
            data[i - start] = fun.applyAsDouble(i);
        }
        return data;
    }

    public static double[] newCopy(double[] array, int start, int end) {
        double[] data = new double[end - start];
        System.arraycopy(array, start, data, 0, end - start);
        return data;
    }

    public static DoubleStream stream(double[] array, int start, int end) {
        return Arrays.stream(array, start, end);
    }

    public static DoubleIterator iterator(double[] array, int start, int end) {
        return new DoubleIterator() {
            private int pos = start;

            @Override
            public boolean hasNext() {
                return pos < end;
            }

            @Override
            public double nextDouble() {
                if (pos >= end) {
                    throw new NoSuchElementException();
                }
                return array[pos++];
            }
        };
    }

    // plus

    public static double[] plus(double[] a, double s, int start, int end) {
        for (int i = start; i < end; i++) {
            a[i] += s;
        }
        return a;
    }

    public static double[] plusc(double[] a, double s, int start, int end) {
        double[] array = new double[end - start];
        for (int i = start; i < end; i++) {
            array[i - start] = a[i] + s;
        }
        return array;
    }

    public static double[] plus(double[] a, double[] b, int start, int end) {
        for (int i = start; i < end; i++) {
            a[i] += b[i];
        }
        return a;
    }

    public static double[] plusc(double[] a, double[] b, int start, int end) {
        double[] array = new double[end - start];
        for (int i = start; i < end; i++) {
            array[i - start] = a[i] + b[i];
        }
        return array;
    }

    // minus

    public static double[] minus(double[] a, double s, int start, int end) {
        for (int i = start; i < end; i++) {
            a[i] -= s;
        }
        return a;
    }

    public static double[] minusc(double[] a, double s, int start, int end) {
        double[] array = new double[end - start];
        for (int i = start; i < end; i++) {
            array[i - start] = a[i] - s;
        }
        return array;
    }

    public static double[] minus(double[] a, double[] b, int start, int end) {
        for (int i = start; i < end; i++) {
            a[i] -= b[i];
        }
        return a;
    }

    public static double[] minusc(double[] a, double[] b, int start, int end) {
        double[] array = new double[end - start];
        for (int i = start; i < end; i++) {
            array[i - start] = a[i] - b[i];
        }
        return array;
    }

    // dot

    public static double[] times(double[] a, double s, int start, int end) {
        for (int i = start; i < end; i++) {
            a[i] *= s;
        }
        return a;
    }

    public static double[] timesc(double[] a, double s, int start, int end) {
        double[] array = new double[end - start];
        for (int i = start; i < end; i++) {
            array[i - start] = a[i] * s;
        }
        return array;
    }

    public static double[] times(double[] a, double[] b, int start, int end) {
        for (int i = start; i < end; i++) {
            a[i] *= b[i];
        }
        return a;
    }

    public static double[] timesc(double[] a, double[] b, int start, int end) {
        double[] array = new double[end - start];
        for (int i = start; i < end; i++) {
            array[i - start] = a[i] * b[i];
        }
        return array;
    }

    // div

    public static double[] div(double[] a, double s, int start, int end) {
        for (int i = start; i < end; i++) {
            a[i] /= s;
        }
        return a;
    }

    public static double[] divc(double[] a, double s, int start, int end) {
        double[] array = new double[end - start];
        for (int i = start; i < end; i++) {
            array[i - start] = a[i] / s;
        }
        return array;
    }

    public static double[] div(double[] a, double[] b, int start, int end) {
        for (int i = start; i < end; i++) {
            a[i] /= b[i];
        }
        return a;
    }

    public static double[] divc(double[] a, double[] b, int start, int end) {
        double[] array = new double[end - start];
        for (int i = start; i < end; i++) {
            array[i - start] = a[i] / b[i];
        }
        return array;
    }

    public static double sum(double[] array, int start, int end) {
        double sum = 0;
        for (int i = start; i < end; i++) {
            sum += array[i];
        }
        return sum;
    }

    public static double nansum(double[] array, int start, int end) {
        double sum = 0;
        for (int i = start; i < end; i++) {
            if (Double.isNaN(array[i])) {
                continue;
            }
            sum += array[i];
        }
        return sum;
    }

    public static int nancount(double[] array, int start, int end) {
        int count = 0;
        for (int i = start; i < end; i++) {
            if (Double.isNaN(array[i])) {
                continue;
            }
            count++;
        }
        return count;
    }

    public static double mean(double[] array, int start, int end) {
        return sum(array, start, end) / (end - start);
    }

    public static double nanmean(double[] array, int start, int end) {
        double sum = 0;
        int count = 0;
        for (int i = start; i < end; i++) {
            if (Double.isNaN(array[i])) {
                continue;
            }
            sum += array[i];
            count++;
        }
        if (count == 0) {
            return Double.NaN;
        }
        return sum / count;
    }

    public static double variance(double[] array, int start, int end) {
        double mean = mean(array, start, end);
        int count = end - start;
        if (count == 0) {
            return Double.NaN;
        }
        double sum2 = 0;
        double sum3 = 0;
        for (int i = start; i < end; i++) {
            sum2 += Math.pow(array[i] - mean, 2);
            sum3 += array[i] - mean;
        }
        return (sum2 - Math.pow(sum3, 2) / count) / (count - 1.0);
    }

    public static double nanvariance(double[] array, int start, int end) {
        double mean = nanmean(array, start, end);
        int missingCount = 0;
        int completeCount = 0;
        for (int i = start; i < end; i++) {
            if (Double.isNaN(array[i])) {
                missingCount++;
            } else {
                completeCount++;
            }
        }
        if (completeCount == 0) {
            return Double.NaN;
        }
        double sum2 = 0;
        double sum3 = 0;
        for (int i = start; i < end; i++) {
            if (Double.isNaN(array[i])) {
                continue;
            }
            sum2 += Math.pow(array[i] - mean, 2);
            sum3 += array[i] - mean;
        }
        return (sum2 - Math.pow(sum3, 2) / completeCount) / (completeCount - 1.0);
    }
}
