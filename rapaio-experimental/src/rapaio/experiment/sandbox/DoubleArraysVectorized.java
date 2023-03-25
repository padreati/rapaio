/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.experiment.sandbox;

public class DoubleArraysVectorized {

    private static final int QUICKSORT_NO_REC = 16;
    private static final int QUICKSORT_MEDIAN_OF_9 = 128;

    private static int compare(double a, double b, boolean asc) {
        if (a < b) {
            return asc ? -1 : 1;
        }
        if (a > b) {
            return asc ? 1 : -1;
        }
        return 0;
    }

    /**
     * Swaps two elements of an array.
     *
     * @param x an array.
     * @param a a position in {@code x}.
     * @param b another position in {@code x}.
     */
    public static void swap(final double[] x, final int a, final int b) {
        final double t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    /**
     * Swaps two sequences of elements of an array.
     *
     * @param x an array.
     * @param a a position in {@code x}.
     * @param b another position in {@code x}.
     * @param n the number of elements to exchange starting at {@code a} and {@code b}.
     */
    public static void swap(final double[] x, int a, int b, final int n) {
        for (int i = 0; i < n; i++, a++, b++) {
            swap(x, a, b);
        }
    }

    private static int med3(final double[] x, final int a, final int b, final int c, boolean asc) {
        final int ab = compare(x[a], x[b], asc);
        final int ac = compare(x[a], x[c], asc);
        final int bc = compare(x[b], x[c], asc);
        return (ab < 0 ? (bc < 0 ? b : ac < 0 ? c : a) : (bc > 0 ? b : ac > 0 ? c : a));
    }

    private static void selectionSort(final double[] a, final int from, final int to, final boolean asc) {
        for (int i = from; i < to - 1; i++) {
            int m = i;
            for (int j = i + 1; j < to; j++) {
                if (compare(a[j], a[m], asc) < 0) {
                    m = j;
                }
            }
            if (m != i) {
                final double u = a[i];
                a[i] = a[m];
                a[m] = u;
            }
        }
    }

    private static double selectMedian(final double[] x, int from, int to, boolean asc) {
        // Choose a partition element, v
        int len = to - from;
        int m = from + len / 2;
        int l = from;
        int n = to - 1;
        if (len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseudomedian of 9
            int s = len / 8;
            l = med3(x, l, l + s, l + 2 * s, asc);
            m = med3(x, m - s, m, m + s, asc);
            n = med3(x, n - 2 * s, n - s, n, asc);
        }
        m = med3(x, l, m, n, asc); // Mid-size, med of 3
        return x[m];
    }

    public static void quickSort(final double[] x, final int from, final int to, final boolean asc) {
        final int len = to - from;
        // Selection sort on smallest arrays
        if (len < QUICKSORT_NO_REC) {
            selectionSort(x, from, to, asc);
            return;
        }
        double v = selectMedian(x, from, to, asc);
        // Establish Invariant: v* (<v)* (>v)* v*
        int a = from, b = a, c = to - 1, d = c;
        while (true) {
            int comparison;
            while (b <= c && (comparison = compare(x[b], v, asc)) <= 0) {
                if (comparison == 0) {
                    swap(x, a++, b);
                }
                b++;
            }
            while (c >= b && (comparison = compare(x[c], v, asc)) >= 0) {
                if (comparison == 0) {
                    swap(x, c, d--);
                }
                c--;
            }
            if (b > c) {
                break;
            }
            swap(x, b++, c--);
        }
        // Swap partition elements back to middle
        int s;
        s = Math.min(a - from, b - a);
        swap(x, from, b - s, s);
        s = Math.min(d - c, to - d - 1);
        swap(x, b, to - s, s);
        // Recursively sort non-partition-elements
        if ((s = b - a) > 1) {
            quickSort(x, from, from + s, asc);
        }
        if ((s = d - c) > 1) {
            quickSort(x, to - s, to, asc);
        }
    }

    public static void quickSortVectorized(final double[] x, final int from, final int to, final boolean asc) {
        final int len = to - from;
        // Selection sort on smallest arrays
        if (len < QUICKSORT_NO_REC) {
            selectionSort(x, from, to, asc);
            return;
        }
        // Choose a partition element, v
        final double v = selectMedian(x, from, to, asc);
        // Establish Invariant: v* (<v)* (>v)* v*
        int a = from, b = a, c = to - 1, d = c;
        while (true) {
            int comparison;
            while (b <= c && (comparison = compare(x[b], v, asc)) <= 0) {
                if (comparison == 0) {
                    swap(x, a++, b);
                }
                b++;
            }
            while (c >= b && (comparison = compare(x[c], v, asc)) >= 0) {
                if (comparison == 0) {
                    swap(x, c, d--);
                }
                c--;
            }
            if (b > c) {
                break;
            }
            swap(x, b++, c--);
        }
        // Swap partition elements back to middle
        int s;
        s = Math.min(a - from, b - a);
        swap(x, from, b - s, s);
        s = Math.min(d - c, to - d - 1);
        swap(x, b, to - s, s);
        // Recursively sort non-partition-elements
        if ((s = b - a) > 1) {
            quickSortVectorized(x, from, from + s, asc);
        }
        if ((s = d - c) > 1) {
            quickSortVectorized(x, to - s, to, asc);
        }
    }


}
