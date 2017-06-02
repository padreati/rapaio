/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.graphics.base;

import rapaio.sys.WS;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Reference:
 * [1] An Extension of Wilkinson's Algorithm for positioning Tick Labels on Axes
 * (Justin Talbot, Sharon Lin, Pat Hanrahan)
 * Ahmet Engin Karahan, revised implementation 20.04.2015
 */
public class XWilkinson {

    private XWilkinson(double[] Q, double base, double[] w, double eps) {
        this.w = w;
        this.Q = Q;
        this.base = base;
        this.eps = eps;
    }

    private XWilkinson(double[] Q, double base) {
        this(Q, base, new double[]{0.25, 0.2, 0.4, 0.05, 0.01}, 1e-10);
    }

    public static XWilkinson of(double[] Q, double base) {
        return new XWilkinson(Q, base);
    }

    public static XWilkinson base10() {
        return XWilkinson.of(new double[]{1, 5, 2, 2.5, 4, 3}, 10);
    }

    public static XWilkinson base2() {
        return XWilkinson.of(new double[]{1}, 2);
    }

    public static XWilkinson base16() {
        return XWilkinson.of(new double[]{1, 2, 4, 8}, 16);
    }

    //- Factory methods that may be useful for time-axis implementations
    public static XWilkinson forSeconds() {
        return XWilkinson.of(new double[]{1, 2, 3, 5, 10, 15, 20, 30}, 60);
    }

    public static XWilkinson forMinutes() {
        return XWilkinson.of(new double[]{1, 2, 3, 5, 10, 15, 20, 30}, 60);
    }

    public static XWilkinson forHours24() {
        return XWilkinson.of(new double[]{1, 2, 3, 4, 6, 8, 12}, 24);
    }

    public static XWilkinson forHours12() {
        return XWilkinson.of(new double[]{1, 2, 3, 4, 6}, 12);
    }

    public static XWilkinson forDays() {
        return XWilkinson.of(new double[]{1, 2}, 7);
    }

    public static XWilkinson forWeeks() {
        return XWilkinson.of(new double[]{1, 2, 4, 13, 26}, 52);
    }

    public static XWilkinson forMonths() {
        return XWilkinson.of(new double[]{1, 2, 3, 4, 6}, 12);
    }

    public static XWilkinson forYears() {
        return XWilkinson.of(new double[]{1, 2, 5}, 10);
    }

    // Loose flag
    public boolean loose = false;

    // scale-goodness weights for simplicity, coverage, density, legibility
    final private double w[];

    // calculation of scale-goodness
    private double w(double s, double c, double d, double l) {
        return w[0] * s + w[1] * c + w[2] * d + w[3] * l;
    }

    // Initial step sizes which we use as seed of generator
    final private double[] Q;

    // Number base used to calculate logarithms
    final private double base;

    private double logB(double a) {
        return Math.log(a) / Math.log(base);
    }

    /*
     * a mod b for float numbers (reminder of a/b)
     */
    private double flooredMod(double a, double n) {
        return a - n * Math.floor(a / n);
    }

    // can be injected via c'tor depending on your application, default is 1e-10
    final private double eps;

    private double v(double min, double max, double step) {
        return (flooredMod(min, step) < eps && min <= 0 && max >= 0) ? 1 : 0;
    }

    private double simplicity(int i, int j, double min, double max, double step) {
        if (Q.length > 1) {
            return 1 - (double) i / (Q.length - 1) - j + v(min, max, step);
        } else {
            return 1 - j + v(min, max, step);
        }
    }

    private double simplicity_max(int i, int j) {
        if (Q.length > 1) {
            return 1 - (double) i / (Q.length - 1) - j + 1.0;
        } else {
            return 1 - j + 1.0;
        }
    }

    private double coverage(double dmin, double dmax, double lmin, double lmax) {
        double a = dmax - lmax;
        double b = dmin - lmin;
        double c = 0.1 * (dmax - dmin);
        return 1 - 0.5 * ((a * a + b * b) / (c * c));
    }

    private double coverage_max(double dmin, double dmax, double span) {
        double range = dmax - dmin;
        if (span > range) {
            double half = (span - range) / 2;
            double r = 0.1 * range;
            return 1 - half * half / (r * r);
        } else {
            return 1.0;
        }
    }


    /*
     *
     * @param k		number of labels
     * @param m		number of desired labels
     * @param dmin	data range minimum
     * @param dmax	data range maximum
     * @param lmin	label range minimum
     * @param lmax	label range maximum
     * @return		density
     *
     * k-1 number of intervals between labels
     * m-1 number of intervals between desired number of labels
     * r   label interval length/label range
     * rt  desired label interval length/actual range
     */
    private double density(int k, int m, double dmin, double dmax, double lmin, double lmax) {
        double r = (k - 1) / (lmax - lmin);
        double rt = (m - 1) / (Math.max(lmax, dmax) - Math.min(lmin, dmin));
        return 2 - Math.max(r / rt, rt / r);   // return 1-Math.max(r/rt, rt/r); (paper is wrong)
    }

    private double density_max(int k, int m) {
        if (k >= m) {
            return 2 - (k - 1) / (m - 1);        // return 2-(k-1)/(m-1); (paper is wrong)
        } else {
            return 1;
        }
    }

    private double legibility(double min, double max, double step) {
        return 1; // Maybe later more...
    }

    public class Label implements Iterable<Double> {

        private double min, max, step, score;

        @Override
        public String toString() {
            String s = "(Score: " + WS.formatFlex(score) + ") ";
            for (double x = min; x <= max; x = x + step) {
                s += WS.formatFlex(x) + "\t";
            }
            return s;
        }

        @Override
        public Iterator<Double> iterator() {
            return getList().iterator();
        }

        public List<Double> getList() {
            List<Double> list = new ArrayList<>();
            for (double i = min; i <= max; i += step) {
                list.add(i);
            }
            return list;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        public double getStep() {
            return step;
        }

        public double getScore() {
            return score;
        }

    }

    /**
     * @param dmin data range min
     * @param dmax data range max
     * @param m    desired number of labels
     * @return XWilkinson.Label
     */
    public Label search(double dmin, double dmax, int m) {

        Label best = new Label();

        if (dmin == dmax) {
            best.min = dmin;
            best.max = dmax;
            best.step = 0;
            best.score = 0.0;
            return best;
        }

        double bestScore = -2;
        double sm, dm, cm, delta;
        int j = 1;

        main_loop:
        while (j < Integer.MAX_VALUE) {
            for (int _i = 0; _i < Q.length; _i++) {
                int i = _i + 1;
                double q = Q[_i];
                sm = simplicity_max(i, j);
                if (w(sm, 1, 1, 1) < bestScore) {
                    break main_loop;
                }
                int k = 2;
                while (k < Integer.MAX_VALUE) {
                    dm = density_max(k, m);
                    if (w(sm, 1, dm, 1) < bestScore) {
                        break;
                    }
                    delta = (dmax - dmin) / (k + 1) / (j * q);
                    int z = (int) Math.ceil(logB(delta));
                    while (z < Integer.MAX_VALUE) {
                        double step = j * q * Math.pow(base, z);
                        cm = coverage_max(dmin, dmax, step * (k - 1));
                        if (w(sm, cm, dm, 1) < bestScore) {
                            break;
                        }
                        int min_start = (int) (Math.floor(dmax / step - (k - 1)) * j);
                        int max_start = (int) (Math.ceil(dmin / step)) * j;

                        for (int start = min_start; start <= max_start; start++) {
                            double lmin = start * step / j;
                            double lmax = lmin + step * (k - 1);
                            double c = coverage(dmin, dmax, lmin, lmax);
                            double s = simplicity(i, j, lmin, lmax, step);
                            double d = density(k, m, dmin, dmax, lmin, lmax);
                            double l = legibility(lmin, lmax, step);
                            double score = w(s, c, d, l);

                            // later legibility logic can be implemented hier

                            if (score > bestScore && (!loose || (lmin <= dmin && lmax >= dmax))) {
                                best.min = lmin;
                                best.max = lmax;
                                best.step = step;
                                best.score = score;
                                bestScore = score;
                            }
                        }
                        z = z + 1;
                    }
                    k = k + 1;
                }
            }
            j = j + 1;
        }
        return best;
    }

    public Label searchBounded(double min, double max, int maxM) {
        Label best = null;
        for (int i = 2; i <= maxM; i++) {
            Label current = search(min, max, i);
            Label bounded = new Label();
            bounded.score = current.score;
            bounded.step = current.step;
            bounded.min = (current.min < min)
                    ? current.min + current.step
                    : current.min;
            bounded.max = (current.max > max)
                    ? current.max - current.step
                    : current.max;
            if (best == null || best.getList().size() < bounded.getList().size()) {
                best = bounded;
            }
        }
        return best;
    }


    // Demo for usage
    public static void main(String[] args) {

        XWilkinson x = XWilkinson.base10();

        WS.println(x.searchBounded(0.01323, 123444, 10).toString());
        System.exit(0);

        // First examples taken from the paper pg 6, Fig 4
        x.loose = true;
        System.out.println(x.search(-98.0, 18.0, 3).toString());
        x.loose = false;
        System.out.println(x.search(-98.0, 18.0, 3).toString());

        System.out.println();

        x.loose = true;
        System.out.println(x.search(-1.0, 200.0, 3).toString());
        x.loose = false;
        System.out.println(x.search(-1.0, 200.0, 3).toString());

        System.out.println();

        x.loose = true;
        System.out.println(x.search(119.0, 178.0, 3).toString());
        x.loose = false;
        System.out.println(x.search(119.0, 178.0, 3).toString());

        System.out.println();

        x.loose = true;
        System.out.println(x.search(-31.0, 27.0, 4).toString());
        x.loose = false;
        System.out.println(x.search(-31.0, 27.0, 3).toString());

        System.out.println();

        x.loose = true;
        System.out.println(x.search(-55.45, -49.99, 2).toString());
        x.loose = false;
        System.out.println(x.search(-55.45, -49.99, 3).toString());

        System.out.println();
        x.loose = false;
        System.out.println(x.search(0, 100, 2).toString());
        System.out.println(x.search(0, 100, 3).toString());
        System.out.println(x.search(0, 100, 4).toString());
        System.out.println(x.search(0, 100, 5).toString());
        System.out.println(x.search(0, 100, 6).toString());
        System.out.println(x.search(0, 100, 7).toString());
        System.out.println(x.search(0, 100, 8).toString());
        System.out.println(x.search(0, 100, 9).toString());
        System.out.println(x.search(0, 100, 10).toString());

        System.out.println("Some additional tests: Testing with base2");
        x = XWilkinson.base2();
        System.out.println(x.search(0, 32, 8).toString());

        System.out.println("Quick experiment with minutes: Check the logic");
        x = XWilkinson.forMinutes();
        System.out.println(x.search(0, 240, 16));
        System.out.println(x.search(0, 240, 9));

        System.out.println("Quick experiment with minutes: Convert values to HH:mm");
        LocalTime start = LocalTime.now();
        LocalTime end = start.plusMinutes(245); // add 4 hrs 5 mins (245 mins) to the start

        int dmin = start.toSecondOfDay() / 60;
        int dmax = end.toSecondOfDay() / 60;
        if (dmin > dmax) {
            // if adding 4 hrs exceeds the midnight simply swap the values this is just an
            // example...
            int swap = dmin;
            dmin = dmax;
            dmax = swap;
        }
        System.out.println("dmin: " + dmin + " dmax: " + dmax);
        XWilkinson.Label labels = x.search(dmin, dmax, 15);
        System.out.println("labels");
        for (double time = labels.getMin(); time < labels.getMax(); time += labels.getStep()) {
            LocalTime lt = LocalTime.ofSecondOfDay(Double.valueOf(time).intValue() * 60);
            System.out.println(lt);
        }

    }
}