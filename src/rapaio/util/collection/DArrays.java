package rapaio.util.collection;


import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.DoubleStream;

/**
 * Utility class to handle the manipulation of arrays of double 64 floating values.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/11/19.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DArrays {

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

    public static double[] newCopy(double[] array, int start, int len) {
        double[] data = new double[len];
        System.arraycopy(array, start, data, 0, len);
        return data;
    }

    public static DoubleStream stream(double[] array, int start, int len) {
        return Arrays.stream(array, start, start + len);
    }

    public static DoubleIterator iterator(double[] array, int start, int len) {
        return new DoubleIterator() {
            private int pos = start;

            @Override
            public boolean hasNext() {
                return pos < start + len;
            }

            @Override
            public double nextDouble() {
                if (pos >= start + len) {
                    throw new NoSuchElementException();
                }
                return array[pos++];
            }
        };
    }

    public static void add(double[] a, int aStart, double s, int len) {
        for (int i = 0; i < len; i++) {
            a[aStart++] += s;
        }
    }

    public static void addTo(double[] a, int aStart, double s, double[] to, int toStart, int len) {
        for (int i = 0; i < len; i++) {
            to[toStart++] = a[aStart++] + s;
        }
    }

    public static void add(double[] a, int aStart, double[] b, int bStart, int len) {
        for (int i = 0; i < len; i++) {
            a[aStart++] += b[bStart++];
        }
    }

    public static void addTo(double[] a, int aStart, double[] b, int bStart, double[] to, int toStart, int len) {
        for (int i = 0; i < len; i++) {
            to[toStart++] = a[aStart++] + b[bStart++];
        }
    }

    // minus

    public static void sub(double[] a, int aStart, double s, int len) {
        for (int i = 0; i < len; i++) {
            a[aStart++] -= s;
        }
    }

    public static void subTo(double[] a, int aStart, double s, double[] to, int toStart, int len) {
        for (int i = 0; i < len; i++) {
            to[toStart++] = a[aStart++] - s;
        }
    }

    public static void sub(double[] a, int aStart, double[] b, int bStart, int len) {
        for (int i = 0; i < len; i++) {
            a[aStart++] -= b[bStart++];
        }
    }

    public static void subTo(double[] a, int aStart, double[] b, int bStart, double[] to, int toStart, int len) {
        for (int i = 0; i < len; i++) {
            to[toStart++] = a[aStart++] - b[bStart++];
        }
    }

    // multiplication

    public static void mult(double[] a, int aStart, double s, int len) {
        for (int i = aStart; i < len + aStart; i++) {
            a[i] *= s;
        }
    }

    public static void multTo(double[] a, int aStart, double s, double[] to, int toStart, int len) {
        for (int i = 0; i < len; i++) {
            to[toStart++] = a[aStart++] * s;
        }
    }

    public static void mult(double[] a, int aStart, double[] b, int bStart, int len) {
        for (int i = 0; i < len; i++) {
            a[aStart++] *= b[bStart++];
        }
    }

    public static void multTo(double[] a, int aStart, double[] b, int bStart, double[] to, int toStart, int len) {
        for (int i = 0; i < len; i++) {
            to[toStart++] = a[aStart++] * b[bStart++];
        }
    }

    // div

    public static void div(double[] a, int aStart, double s, int len) {
        for (int i = aStart; i < len + aStart; i++) {
            a[i] /= s;
        }
    }

    public static void divTo(double[] a, int aStart, double s, double[] to, int toStart, int len) {
        for (int i = 0; i < len; i++) {
            to[toStart++] = a[aStart++] / s;
        }
    }

    public static void div(double[] a, int aStart, double[] b, int bStart, int len) {
        for (int i = 0; i < len; i++) {
            a[aStart++] /= b[bStart++];
        }
    }

    public static void divTo(double[] a, int aStart, double[] b, int bStart, double[] to, int toStart, int len) {
        for (int i = 0; i < len; i++) {
            to[toStart++] = a[aStart++] / b[bStart++];
        }
    }

    public static double sum(double[] a, int start, int len) {
        double sum = 0;
        for (int i = start; i < len + start; i++) {
            sum += a[i];
        }
        return sum;
    }

    public static double nanSum(double[] a, int start, int len) {
        double sum = 0;
        for (int i = start; i < len + start; i++) {
            if (Double.isNaN(a[i])) {
                continue;
            }
            sum += a[i];
        }
        return sum;
    }

    public static int nanCount(double[] a, int start, int len) {
        int count = 0;
        for (int i = start; i < start + len; i++) {
            if (Double.isNaN(a[i])) {
                continue;
            }
            count++;
        }
        return count;
    }

    public static double mean(double[] a, int start, int len) {
        return sum(a, start, len) / len;
    }

    public static double nanMean(double[] a, int start, int len) {
        double sum = 0;
        int count = 0;
        for (int i = start; i < len + start; i++) {
            if (Double.isNaN(a[i])) {
                continue;
            }
            sum += a[i];
            count++;
        }
        return sum / count;
    }

    public static double variance(double[] a, int start, int len) {
        if (len == 0) {
            return Double.NaN;
        }
        double mean = mean(a, start, len);
        double sum2 = 0;
        double sum3 = 0;
        for (int i = start; i < start + len; i++) {
            sum2 += Math.pow(a[i] - mean, 2);
            sum3 += a[i] - mean;
        }
        return (sum2 - Math.pow(sum3, 2) / len) / (len - 1.0);
    }

    public static double nanVariance(double[] a, int start, int len) {
        double mean = nanMean(a, start, len);
        int completeCount = nanCount(a, start, len);
        if (completeCount == 0) {
            return Double.NaN;
        }
        double sum2 = 0;
        double sum3 = 0;
        for (int i = start; i < len + start; i++) {
            if (Double.isNaN(a[i])) {
                continue;
            }
            sum2 += Math.pow(a[i] - mean, 2);
            sum3 += a[i] - mean;
        }
        return (sum2 - Math.pow(sum3, 2) / completeCount) / (completeCount - 1.0);
    }
}
