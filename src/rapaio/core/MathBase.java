/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.core;

import rapaio.core.distributions.Normal;

/**
 * Utility class which simplifies access to common java math utilities and also
 * enrich the mathematical operations set.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class MathBase {


    /**
     * Returns the base 2 logarithm of a {@code double} getValue.
     *
     * @param x the number from which we take base 2 logarithm
     * @return the base 2 logarithm of input getValue
     */
    public static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    /**
     * Returns {@code boolean} getValue indicating if the number if finite and different than {@code Double.NaN}.
     * <p>
     * This function is used to check if a computation can produce finite results or not.
     * Another situation where is useful is when we test for a default numeric getValue which is usually set to {@code Double.NaN}.
     *
     * @param x the number which needs to be verified
     * @return true if the number is finite and different than {@code Double.NaN}
     */
    public static boolean validNumber(double x) {
        return x == x && !Double.isInfinite(x);
    }

    private static final double
            a0 = 7.72156649015328655494e-02,
            a1 = 3.22467033424113591611e-01,
            a2 = 6.73523010531292681824e-02,
            a3 = 2.05808084325167332806e-02,
            a4 = 7.38555086081402883957e-03,
            a5 = 2.89051383673415629091e-03,
            a6 = 1.19270763183362067845e-03,
            a7 = 5.10069792153511336608e-04,
            a8 = 2.20862790713908385557e-04,
            a9 = 1.08011567247583939954e-04,
            a10 = 2.52144565451257326939e-05,
            a11 = 4.48640949618915160150e-05,
            tc = 1.46163214496836224576e+00,
            tf = -1.21486290535849611461e-01,
            tt = -3.63867699703950536541e-18,
            t0 = 4.83836122723810047042e-01,
            t1 = -1.47587722994593911752e-01,
            t2 = 6.46249402391333854778e-02,
            t3 = -3.27885410759859649565e-02,
            t4 = 1.79706750811820387126e-02,
            t5 = -1.03142241298341437450e-02,
            t6 = 6.10053870246291332635e-03,
            t7 = -3.68452016781138256760e-03,
            t8 = 2.25964780900612472250e-03,
            t9 = -1.40346469989232843813e-03,
            t10 = 8.81081882437654011382e-04,
            t11 = -5.38595305356740546715e-04,
            t12 = 3.15632070903625950361e-04,
            t13 = -3.12754168375120860518e-04,
            t14 = 3.35529192635519073543e-04,
            u0 = -7.72156649015328655494e-02,
            u1 = 6.32827064025093366517e-01,
            u2 = 1.45492250137234768737e+00,
            u3 = 9.77717527963372745603e-01,
            u4 = 2.28963728064692451092e-01,
            u5 = 1.33810918536787660377e-02,
            v1 = 2.45597793713041134822e+00,
            v2 = 2.12848976379893395361e+00,
            v3 = 7.69285150456672783825e-01,
            v4 = 1.04222645593369134254e-01,
            v5 = 3.21709242282423911810e-03,
            s0 = -7.72156649015328655494e-02,
            s1 = 2.14982415960608852501e-01,
            s2 = 3.25778796408930981787e-01,
            s3 = 1.46350472652464452805e-01,
            s4 = 2.66422703033638609560e-02,
            s5 = 1.84028451407337715652e-03,
            s6 = 3.19475326584100867617e-05,
            r1 = 1.39200533467621045958e+00,
            r2 = 7.21935547567138069525e-01,
            r3 = 1.71933865632803078993e-01,
            r4 = 1.86459191715652901344e-02,
            r5 = 7.77942496381893596434e-04,
            r6 = 7.32668430744625636189e-06,
            w0 = 4.18938533204672725052e-01,
            w1 = 8.33333333333329678849e-02,
            w2 = -2.77777777728775536470e-03,
            w3 = 7.93650558643019558500e-04,
            w4 = -5.95187557450339963135e-04,
            w5 = 8.36339918996282139126e-04,
            w6 = -1.63092934096575273989e-03;

    /*
     * Computes ln(gamma) function.
     *
     * Author Mihai Preda, 2006. The author disclaims copyright to this source
     * code.
     *
     * The method lgamma() is adapted from FDLIBM 5.3
     * (http://www.netlib.org/fdlibm/), which comes with this copyright notice:
     * ==================================================== Copyright (C) 1993
     * by Sun Microsystems, Inc. All rights reserved.
     *
     * Developed at SunSoft, a Sun Microsystems, Inc. business. Permission to
     * use, copy, modify, and distribute this software is freely granted,
     * provided that this notice is preserved.
     * ====================================================
     *
     * The Lanczos and Stirling approximations are based on:
     * http://en.wikipedia.org/wiki/Lanczos_approximation
     * http://en.wikipedia.org/wiki/Stirling%27s_approximation
     * http://www.gnu.org/software/gsl/ http://jakarta.apache.org/commons/math/
     * http://my.learn.edu/~gabdo/gamma.txt
     */
    public static double lnGamma(double x) {
        double t, y, z, p, p1, p2, p3, q, r, w;
        int i;

        int hx = (int) (Double.doubleToLongBits(x) >> 32);
        int lx = (int) Double.doubleToLongBits(x);

        /*
         * purge off +-inf, NaN, +-0, and negative arguments
         */
        int ix = hx & 0x7fffffff;
        if (ix >= 0x7ff00000) {
            return Double.POSITIVE_INFINITY;
        }
        if ((ix | lx) == 0 || hx < 0) {
            return Double.NaN;
        }
        if (ix < 0x3b900000) {	/*
			 * |x|<2**-70, return -log(|x|)
             */

            return -Math.log(x);
        }

        /*
		 * purge off 1 and 2
         */
        if ((((ix - 0x3ff00000) | lx) == 0) || (((ix - 0x40000000) | lx) == 0)) {
            r = 0;
        } /*
		 * for x < 2.0
         */ else if (ix < 0x40000000) {
            if (ix <= 0x3feccccc) { 	/*
                 * lgamma(x) = lgamma(x+1)-log(x)
                 */

                r = -Math.log(x);
                if (ix >= 0x3FE76944) {
                    y = 1 - x;
                    i = 0;
                } else if (ix >= 0x3FCDA661) {
                    y = x - (tc - 1);
                    i = 1;
                } else {
                    y = x;
                    i = 2;
                }
            } else {
                r = 0;
                if (ix >= 0x3FFBB4C3) {
                    y = 2.0 - x;
                    i = 0;
                } /*
                 * [1.7316,2]
                 */ else if (ix >= 0x3FF3B4C4) {
                    y = x - tc;
                    i = 1;
                } /*
                 * [1.23,1.73]
                 */ else {
                    y = x - 1;
                    i = 2;
                }
            }

            switch (i) {
                case 0:
                    z = y * y;
                    p1 = a0 + z * (a2 + z * (a4 + z * (a6 + z * (a8 + z * a10))));
                    p2 = z * (a1 + z * (a3 + z * (a5 + z * (a7 + z * (a9 + z * a11)))));
                    p = y * p1 + p2;
                    r += (p - 0.5 * y);
                    break;
                case 1:
                    z = y * y;
                    w = z * y;
                    p1 = t0 + w * (t3 + w * (t6 + w * (t9 + w * t12)));	/*
                     * parallel comp
                     */

                    p2 = t1 + w * (t4 + w * (t7 + w * (t10 + w * t13)));
                    p3 = t2 + w * (t5 + w * (t8 + w * (t11 + w * t14)));
                    p = z * p1 - (tt - w * (p2 + y * p3));
                    r += (tf + p);
                    break;
                case 2:
                    p1 = y * (u0 + y * (u1 + y * (u2 + y * (u3 + y * (u4 + y * u5)))));
                    p2 = 1 + y * (v1 + y * (v2 + y * (v3 + y * (v4 + y * v5))));
                    r += (-0.5 * y + p1 / p2);
            }
        } else if (ix < 0x40200000) { 			/*
             * x < 8.0
             */

            i = (int) x;
            y = x - (double) i;
            p = y * (s0 + y * (s1 + y * (s2 + y * (s3 + y * (s4 + y * (s5 + y * s6))))));
            q = 1 + y * (r1 + y * (r2 + y * (r3 + y * (r4 + y * (r5 + y * r6)))));
            r = 0.5 * y + p / q;
            z = 1;
            switch (i) {
                case 7:
                    z *= (y + 6.0);
                case 6:
                    z *= (y + 5.0);
                case 5:
                    z *= (y + 4.0);
                case 4:
                    z *= (y + 3.0);
                case 3:
                    z *= (y + 2.0);
                    r += Math.log(z);
                    break;
            }
            /*
             * 8.0 <= x < 2**58
             */
        } else if (ix < 0x43900000) {
            t = Math.log(x);
            z = 1 / x;
            y = z * z;
            w = w0 + z * (w1 + y * (w2 + y * (w3 + y * (w4 + y * (w5 + y * w6)))));
            r = (x - 0.5) * (t - 1) + w;
        } else /*
         * 2**58 <= x <= inf
         */ {
            r = x * (Math.log(x) - 1);
        }
        return r;
    }

    /**
     * Error function of a {@code double} getValue.
     * <p>
     * erf(x) = 2 * cdf(x sqrt(2)) -1
     * <p>
     * where cdf is the cdf of the normal distribution
     * <p>
     * http://en.wikipedia.org/wiki/Error_function
     *
     * @param x the number for which we compute erf
     * @return the erf of x
     */
    public static double erf(double x) {
        return 2 * new Normal(0, 1).cdf(x * Math.sqrt(2.0)) - 1;
    }

    /**
     * Inverse error function of a {@code double} getValue.
     * <p>
     * inverf(x) = invcdf(x/2+1/2)/sqrt(2)
     * <p>
     * where invcdf is the inverse cdf of the normal distribution
     * <p>
     * http://en.wikipedia.org/wiki/Error_function
     *
     * @param x the number for which we compute invErf
     * @return the invErf of x
     */
    public static double inverf(double x) {
        return new Normal(0, 1).quantile(x / 2 + 0.5) / Math.sqrt(2.0);
    }

    /**
     * Complementary error function of a {@code double} getValue.
     * <p>
     * erfc(x) = 1 - erf(x)
     * <p>
     * http://en.wikipedia.org/wiki/Error_function
     *
     * @param x the number for which we compute erf
     * @return the erf of x
     */
    public static double erfc(double x) {
        return 2 * new Normal(0, 1).cdf(-x * Math.sqrt(2.0));
    }

    /**
     * Inverse of complementary error function of a {@code double} getValue.
     * <p>
     * inverfc(x) = invcdf(x/2)/-sqrt(2)
     * <p>
     * where invcdf is the inverse cdf of the normal distribution
     * <p>
     * http://en.wikipedia.org/wiki/Error_function
     *
     * @param x the number for which we compute invErf
     * @return the invErf of x
     */
    public static double inverfc(double x) {
        return new Normal(0, 1).quantile(x / 2) / -Math.sqrt(2.0);
    }

    /**
     * Computes the Beta function B(z,w).
     * <p>
     * http://en.wikipedia.org/wiki/Beta_function
     *
     * @param z first argument getValue >= 0
     * @param w second argument getValue >= 0
     * @return beta function of z and w
     */
    public static double beta(double z, double w) {
        return Math.exp(lnBeta(z, w));
    }

    /**
     * Computes natural logarithm of Beta function B(z, w).
     * <p>
     * http://en.wikipedia.org/wiki/Beta_function
     *
     * @param z first argument getValue >= 0
     * @param w second argument getValue >= 0
     * @return lnBeta function of z and w
     */
    public static double lnBeta(double z, double w) {
        return lnGamma(z) + lnGamma(w) - lnGamma(z + w);
    }

    /**
     * Computes the regularized incomplete beta function, I<sub>x</sub>(a, b).
     * The result of which is always in the range [0, 1]
     *
     * @param x any getValue in the range [0, 1]
     * @param a any getValue >= 0
     * @param b any getValue >= 0
     * @return the result in a range of [0,1]
     */
    public static double betaIncReg(double x, double a, double b) {
        if (a <= 0 || b <= 0) {
            throw new IllegalArgumentException("a and b must be positive");
        }
        if (x == 0 || x == 1) {
            return x;
        } else if (x < 0 || x > 1) {
            throw new IllegalArgumentException("x must be in the range [0,1]");
        }

        //We use this identity to make sure that our continued fraction is always in a range for which it converges faster
        if (x > (a + 1) / (a + b + 2) || (1 - x) < (b + 1) / (a + b + 2)) {
            return 1 - betaIncReg(1 - x, b, a);
        }


        /*
         * All values are from x = 0 to x = 1, in 0.025 increments a = 0.5, b =
         * 0.5: max rel error ~ 2.2e-15 a = 0.5, b = 5: max rel error ~ 2e-15 a
         * = 5, b = 0.5: max rel error ~ 1.5e-14 @ x ~= 7.75, otherwise rel
         * error ~ 2e-15 a = 8, b = 10: max rel error ~ 9e-15, rel error is
         * clearly not uniform but always small a = 80, b = 100: max rel error ~
         * 1.2e-14, rel error is clearly not uniform but always small
         */
        double numer = a * Math.log(x) + b * Math.log(1 - x) - (Math.log(a) + lnBeta(a, b));

        return Math.exp(numer) / lentz(x, a, b);
    }

    private static double lentzA(int pos, double... args) {
        if (pos % 2 == 0) {
            pos /= 2;
            return pos * (args[2] - pos) * args[0] / ((args[1] + 2 * pos - 1) * (args[1] + 2 * pos));
        }

        pos = (pos - 1) / 2;

        double numer = -(args[1] + pos) * (args[1] + args[2] + pos) * args[0];
        double denom = (args[1] + 2 * pos) * (args[1] + 1 + 2 * pos);

        return numer / denom;
    }

    private static double lentz(double... args) {
        double f_n = 1.0;
        double c_n, c_0 = f_n;
        double d_n, d_0 = 0;

        double delta = 0;

        int j = 0;
        while (Math.abs(delta - 1) > 1e-15) {

            j++;
            d_n = 1.0 + lentzA(j, args) * d_0;
            if (d_n == 0.0) {
                d_n = 1e-30;
            }

            c_n = 1.0 + lentzA(j, args) / c_0;
            if (c_n == 0.0) {
                c_n = 1e-30;
            }

            d_n = 1 / d_n;
            delta = c_n * d_n;
            f_n *= delta;

            d_0 = d_n;
            c_0 = c_n;
        }

        return f_n;
    }

    private static double betaIncRegFunc(double... x) {
        return betaIncReg(x[0], x[1], x[2]) - x[3];
    }

    /**
     * Computes the inverse of the incomplete beta function,
     * I<sub>p</sub><sup>-1</sup>(a,b), such that {@link #betaIncReg(double, double, double) I<sub>x</sub>(a, b)
     * } = <tt>p</tt>. The returned getValue, x, will always be in the range [0,1].
     * The input <tt>p</tt>, must also be in the range [0,1].
     *
     * @param p any getValue in the range [0,1]
     * @param a any getValue >= 0
     * @param b any getValue >= 0
     * @return the getValue x, such that {@link #betaIncReg(double, double, double) I<sub>x</sub>(a, b)
     * } will return p.
     */
    public static double invBetaIncReg(double p, double a, double b) {
        if (p < 0 || p > 1) {
            throw new ArithmeticException("The getValue p must be in the range [0,1], not" + p);
        }

        double eps = 1e-15;
        int maxIterations = 1000;
        double x1 = 0;
        double x2 = 1;
        double[] args = new double[]{p, a, b, p};

        args[0] = x1;
        double fx1 = betaIncRegFunc(args);
        args[0] = x2;
        double fx2 = betaIncRegFunc(args);
        double halfEps = eps * 0.5;

        if (fx1 * fx2 >= 0) {
            throw new ArithmeticException("The given interval does not appear to bracket the root");
        }

        double dif = 1;//Measure the change interface values
        while (Math.abs(x1 - x2) > eps && maxIterations-- > 0) {
            double x3 = (x1 + x2) * 0.5;

            args[0] = x3;
            double fx3 = betaIncRegFunc(args);

            double x4 = x3 + (x3 - x1) * Math.signum(fx1 - fx2) * fx3 / Math.sqrt(fx3 * fx3 - fx1 * fx2);

            args[0] = x4;
            double fx4 = betaIncRegFunc(args);
            if (fx3 * fx4 < 0) {
                x1 = x3;
                fx1 = fx3;
                x2 = x4;
                fx2 = fx4;
            } else if (fx1 * fx4 < 0) {
                dif = Math.abs(x4 - x2);
                if (dif <= halfEps)//WE are no longer updating, return the getValue
                {
                    return x4;
                }
                x2 = x4;
                fx2 = fx4;
            } else {
                dif = Math.abs(x4 - x1);
                if (dif <= halfEps)//WE are no longer updating, return the getValue
                {
                    return x4;
                }
                x1 = x4;
                fx1 = fx4;
            }

        }
        return x2;
    }
}
