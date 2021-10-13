/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

import rapaio.core.distributions.Normal;

/**
 * Utility class which simplifies access to common java math utilities and also
 * enrich the mathematical operations set.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class MathTools {

    /**
     * sqrt(2)
     */
    public static final double SQRT_2 =
            1.41421356237309504880168872420969807856967187537694807317667973799073247846210703885038753432764157273501384623091229702;

    /**
     * 1/sqrt(2)
     */
    public static final double INV_SQRT_2 =
            0.70710678118654752440084436210484903928483593768847403658833986899536623923105351942519376716382078636750692311545614851;
    /**
     * sqrt(32)
     */
    public static final double SQRT_32 =
            5.65685424949238019520675489683879231427868750150779229270671895196292991384842815540155013731056629094005538492364918809;

    /**
     * ln(2)
     */
    public static final double LN_2 =
            0.69314718055994530941723212145817656807550013436025525412068000949339362196969471560586332699641868754200148102057068573;

    /**
     * ln(10)
     */
    public static final double LN_10 =
            2.30258509299404568401799145468436420760110148862877297603332790096757260967735248023599720508959829834196778404228624863;

    /**
     * log_10(2)
     */
    public static final double LOG10_2 =
            0.30102999566398119521373889472449302676818988146210854131042746112710818927442450948692725211818617204068447719143099537;

    public static final double E = Math.E;

    public static final double PI =
            3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803482534211706798214808651328230664;

    public static final double DOUBLE_PI =
            6.28318530717958647692528676655900576839433879875021164194988918461563281257241799725606965068423413596429617302656461329;

    public static final double LN_PI =
            1.14472988584940017414342735135305871164729481291531157151362307147213776988482607978362327027548970770200981222869798915;

    /* 1/pi */
    public static final double INV_PI =
            0.31830988618379067153776752674502872406891929148091289749533468811779359526845307018022760553250617191214568545351591607;

    /* pi/2 */
    public static final double HALF_PI =
            1.57079632679489661923132169163975144209858469968755291048747229615390820314310449931401741267105853399107404325664115332;

    public static final double LN_2PI =
            1.83787706640934548356065947281123527972279494727556682563430308096553139185452079538948659727190839524401129324926867489;

    /* sqrt(pi),  1/sqrt(2pi),  sqrt(2/pi) : */
    public static final double SQRT_PI =
            1.77245385090551602729816748334114518279754945612238712821380778985291128459103218137495065673854466541622682362428257066;
    public static final double INV_SQRT_2PI =
            0.39894228040143267793994605993438186847585863116493465766592582967065792589930183850125233390730693643030255886263518268;
    public static final double M_SQRT_2dPI =
            0.79788456080286535587989211986876373695171726232986931533185165934131585179860367700250466781461387286060511772527036537;

    /* log(sqrt(pi)) = log(pi)/2 : */
    public static final double LN_SQRT_PI =
            0.57236494292470008707171367567652935582364740645765578575681153573606888494241303989181163513774485385100490611434899457;
    /* log(sqrt(2*pi)) = log(2*pi)/2 : */
    public static final double LN_SQRT_2PI =
            0.91893853320467274178032973640561763986139747363778341281715154048276569592726039769474329863595419762200564662463433744;
    /* log(sqrt(pi/2)) = log(pi/2)/2 : */
    public static final double HALF_LN_SQRT_HALF_PI =
            0.22579135264472743236309761494744107178589733927752815869647153098937207395756568208887997163953551008000416560406365171;

    /* constants taken from float.h for gcc 2.90.29 for Linux 2.0 i386  */
    /* -- should match Java since both are supposed to be IEEE 754 compliant */

    /* Difference between 1.0 and the minimum float/double greater than 1.0 */
    public static final double FLT_EPSILON = 1.19209290e-07F;
    public static final double DBL_EPSILON = 2.2204460492503131e-16;
    public static final double DBL_MIN = 2.22507385850720138309e-308;
    public static final double DBL_MAX = 1.797693134862315708145e+308;
    public static final double SQRT_DBL_EPSILON = Math.sqrt(DBL_EPSILON);

    /*
     * machine constants
     */
    public static final double MACHEP = 1.11022302462515654042E-16;

    public static final double MAXLOG = 7.09782712893383996732E2;

    public static final double MINLOG = -7.451332191019412076235E2;

    public static final double MAXGAM = 171.624376956302725;

    public static final double SQTPI = 2.50662827463100050242E0;

    public static final double SQRTH = 7.07106781186547524401E-1;

    public static final double LOGPI = 1.14472988584940017414;


    public static final double biginv = 2.22044604925031308085e-16;

    /**
     * This is the squared inverse of the golden ratio ((3 - sqrt(5.0))/ 2).
     * Used in golden-ratio search.
     * Somehow inputting the number directly improves accuracy
     */
    public static final double kInvGoldRatio =
            0.38196601125010515179541316563436188227969082019423713786455137729473953718109755029279279581060886251524591192461310824;

    public static final double TWO_PI = 6.283185307179586476925286;
    public static final double SMALL_ERR = 1e-10;
    private static final double
            lng_a0 = 7.72156649015328655494e-02,
            lng_a1 = 3.22467033424113591611e-01,
            lng_a2 = 6.73523010531292681824e-02,
            lng_a3 = 2.05808084325167332806e-02,
            lng_a4 = 7.38555086081402883957e-03,
            lng_a5 = 2.89051383673415629091e-03,
            lng_a6 = 1.19270763183362067845e-03,
            lng_a7 = 5.10069792153511336608e-04,
            lng_a8 = 2.20862790713908385557e-04,
            lng_a9 = 1.08011567247583939954e-04,
            lng_a10 = 2.52144565451257326939e-05,
            lng_a11 = 4.48640949618915160150e-05,
            lng_tc = 1.46163214496836224576e+00,
            lng_tf = -1.21486290535849611461e-01,
            lng_tt = -3.63867699703950536541e-18,
            lng_t0 = 4.83836122723810047042e-01,
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
    /**
     * 1/2 * log(2 &#960;).
     */
    private static final double HALF_LOG_2_PI = 0.5 * LN_2PI;
    /**
     * exact Stirling expansion error for certain values.
     */
    private static final double[] EXACT_STIRLING_ERRORS = {0.0, /* 0.0 */
            0.1534264097200273452913848, /* 0.5 */
            0.0810614667953272582196702, /* 1.0 */
            0.0548141210519176538961390, /* 1.5 */
            0.0413406959554092940938221, /* 2.0 */
            0.03316287351993628748511048, /* 2.5 */
            0.02767792568499833914878929, /* 3.0 */
            0.02374616365629749597132920, /* 3.5 */
            0.02079067210376509311152277, /* 4.0 */
            0.01848845053267318523077934, /* 4.5 */
            0.01664469118982119216319487, /* 5.0 */
            0.01513497322191737887351255, /* 5.5 */
            0.01387612882307074799874573, /* 6.0 */
            0.01281046524292022692424986, /* 6.5 */
            0.01189670994589177009505572, /* 7.0 */
            0.01110455975820691732662991, /* 7.5 */
            0.010411265261972096497478567, /* 8.0 */
            0.009799416126158803298389475, /* 8.5 */
            0.009255462182712732917728637, /* 9.0 */
            0.008768700134139385462952823, /* 9.5 */
            0.008330563433362871256469318, /* 10.0 */
            0.007934114564314020547248100, /* 10.5 */
            0.007573675487951840794972024, /* 11.0 */
            0.007244554301320383179543912, /* 11.5 */
            0.006942840107209529865664152, /* 12.0 */
            0.006665247032707682442354394, /* 12.5 */
            0.006408994188004207068439631, /* 13.0 */
            0.006171712263039457647532867, /* 13.5 */
            0.005951370112758847735624416, /* 14.0 */
            0.005746216513010115682023589, /* 14.5 */
            0.005554733551962801371038690 /* 15.0 */
    };

    public static double cut(double x, double min, double max) {
        if (x < min) {
            return min;
        }
        return Math.min(x, max);
    }

    public static int cut(int x, int min, int max) {
        if (x < min) {
            return min;
        }
        return Math.min(x, max);
    }

    /**
     * Returns the base 2 logarithm of a {@code double} value.
     *
     * @param x the number from which we take base 2 logarithm
     * @return the base 2 logarithm of input value
     */
    public static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    /**
     * Returns the logarithm of value in a given base.
     *
     * @param x    value
     * @param base logarithm's base
     * @return logarithm of value in specified base
     */
    public static double logBase(double x, double base) {
        return Math.log(x) / Math.log(base);
    }

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
        if (ix < 0x3b900000) {    /*
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
            if (ix <= 0x3feccccc) {    /*
             * lgamma(x) = lgamma(x+1)-log(x)
             */

                r = -Math.log(x);
                if (ix >= 0x3FE76944) {
                    y = 1 - x;
                    i = 0;
                } else if (ix >= 0x3FCDA661) {
                    y = x - (lng_tc - 1);
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
                    y = x - lng_tc;
                    i = 1;
                } /*
                 * [1.23,1.73]
                 */ else {
                    y = x - 1;
                    i = 2;
                }
            }

            switch (i) {
                case 0 -> {
                    z = y * y;
                    p1 = lng_a0 + z * (lng_a2 + z * (lng_a4 + z * (lng_a6 + z * (lng_a8 + z * lng_a10))));
                    p2 = z * (lng_a1 + z * (lng_a3 + z * (lng_a5 + z * (lng_a7 + z * (lng_a9 + z * lng_a11)))));
                    p = y * p1 + p2;
                    r += (p - 0.5 * y);
                }
                case 1 -> {
                    z = y * y;
                    w = z * y;
                    p1 = lng_t0 + w * (t3 + w * (t6 + w * (t9 + w * t12)));    /*
                     * parallel comp
                     */
                    p2 = t1 + w * (t4 + w * (t7 + w * (t10 + w * t13)));
                    p3 = t2 + w * (t5 + w * (t8 + w * (t11 + w * t14)));
                    p = z * p1 - (lng_tt - w * (p2 + y * p3));
                    r += (lng_tf + p);
                }
                case 2 -> {
                    p1 = y * (u0 + y * (u1 + y * (u2 + y * (u3 + y * (u4 + y * u5)))));
                    p2 = 1 + y * (v1 + y * (v2 + y * (v3 + y * (v4 + y * v5))));
                    r += (-0.5 * y + p1 / p2);
                }
            }
        } else if (ix < 0x40200000) {            /*
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
     * Error function of a {@code double} value.
     * <p>
     * erf(x) = 2 * cdf(x sqrt(2)) -1
     * <p>
     * where cdf is the cdf of the gaussian densities
     * <p>
     * http://en.wikipedia.org/wiki/Error_function
     *
     * @param x the number for which we compute erf
     * @return the erf of x
     */
    public static double erf(double x) {
        return 2 * Normal.std().cdf(x * Math.sqrt(2.0)) - 1;
    }

    /**
     * Inverse error function of a {@code double} value.
     * <p>
     * inverf(x) = invcdf(x/2+1/2)/sqrt(2)
     * <p>
     * where invcdf is the inverse cdf of the gaussian densities
     * <p>
     * http://en.wikipedia.org/wiki/Error_function
     *
     * @param x the number for which we compute invErf
     * @return the invErf of x
     */
    public static double inverf(double x) {
        return Normal.std().quantile(x / 2 + 0.5) / Math.sqrt(2.0);
    }

    /**
     * Complementary error function of a {@code double} value.
     * <p>
     * erfc(x) = 1 - erf(x)
     * <p>
     * http://en.wikipedia.org/wiki/Error_function
     *
     * @param x the number for which we compute erf
     * @return the erf of x
     */
    public static double erfc(double x) {
        return 2 * Normal.std().cdf(-x * Math.sqrt(2.0));
    }

    /**
     * Inverse of complementary error function of a {@code double} value.
     * <p>
     * inverfc(x) = invcdf(x/2)/-sqrt(2)
     * <p>
     * where invcdf is the inverse cdf of the gaussian densities
     * <p>
     * http://en.wikipedia.org/wiki/Error_function
     *
     * @param x the number for which we compute invErf
     * @return the invErf of x
     */
    public static double inverfc(double x) {
        return Normal.std().quantile(x / 2) / -Math.sqrt(2.0);
    }

    /**
     * Computes the Beta function B(z,w).
     * <p>
     * http://en.wikipedia.org/wiki/Beta_function
     *
     * @param z first argument value >= 0
     * @param w second argument value >= 0
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
     * @param z first argument value >= 0
     * @param w second argument value >= 0
     * @return lnBeta function of z and w
     */
    public static double lnBeta(double z, double w) {
        return lnGamma(z) + lnGamma(w) - lnGamma(z + w);
    }

    /**
     * Computes the regularized incomplete beta function, I<sub>x</sub>(a, b).
     * The result of which is always in the range [0, 1]
     *
     * @param x any value in the range [0, 1]
     * @param a any value >= 0
     * @param b any value >= 0
     * @return the result in a range of [0,1]
     */
    public static double betaIncReg(double x, double a, double b) {
        boolean outOfRange = (a < 0 || b < 0);
        if (outOfRange) {
            throw new IllegalArgumentException("a and b must be positive or zero");
        }
        boolean xIsZeroOrOne = (x == 0 || x == 1);
        boolean xIsOutOfRange = (x < 0 || x > 1);
        if (xIsZeroOrOne) {
            return x;
        } else if (xIsOutOfRange) {
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

    private static double betaIncRegFunc(double p, double a, double b, double c) {
        return betaIncReg(p, a, b) - c;
    }

    /**
     * Computes the inverse of the incomplete beta function,
     * I<sub>p</sub><sup>-1</sup>(a,b), such that {@link #betaIncReg(double, double, double) I<sub>x</sub>(a, b)
     * } = <tt>p</tt>. The returned value, x, will always be in the range [0,1].
     * The input <tt>p</tt>, must also be in the range [0,1].
     *
     * @param p any value in the range [0,1]
     * @param a any value >= 0
     * @param b any value >= 0
     * @return the value x, such that {@link #betaIncReg(double, double, double) I<sub>x</sub>(a, b)
     * } will return p.
     */
    public static double invBetaIncReg(double p, double a, double b) {
        if (p < 0 || p > 1) {
            throw new ArithmeticException("The value p must be in the range [0,1], not" + p);
        }

        double eps = 1e-15;
        int maxIterations = 1000;
        double x1 = 0;
        double x2 = 1;
        double fx1 = betaIncRegFunc(x1, a, b, p);
        double fx2 = betaIncRegFunc(x2, a, b, p);
        double halfEps = eps * 0.5;

        if (fx1 * fx2 >= 0) {
            throw new ArithmeticException("The given interval does not appear to bracket the root");
        }

        double dif;//Measure the change interface values
        while (Math.abs(x1 - x2) > eps && maxIterations-- > 0) {
            double x3 = (x1 + x2) * 0.5;
            double fx3 = betaIncRegFunc(x3, a, b, p);
            double x4 = x3 + (x3 - x1) * Math.signum(fx1 - fx2) * fx3 / Math.sqrt(fx3 * fx3 - fx1 * fx2);
            double fx4 = betaIncRegFunc(x4, a, b, p);
            if (fx3 * fx4 < 0) {
                x1 = x3;
                fx1 = fx3;
                x2 = x4;
                fx2 = fx4;
            } else if (fx1 * fx4 < 0) {
                dif = Math.abs(x4 - x2);
                if (dif <= halfEps)//WE are no longer updating, return the value
                {
                    return x4;
                }
                x2 = x4;
                fx2 = fx4;
            } else {
                dif = Math.abs(x4 - x1);
                if (dif <= halfEps)//WE are no longer updating, return the value
                {
                    return x4;
                }
                x1 = x4;
                fx1 = fx4;
            }

        }
        return x2;
    }

    /**
     * Returns the Incomplete Gamma function; formerly named <tt>igamma</tt>.
     *
     * @param a the parameter of the gamma distribution.
     * @param x the integration end point.
     */
    static public double incompleteGamma(double a, double x) throws ArithmeticException {

        double ans, ax, c, r;

        if (x <= 0 || a <= 0) {
            return 0.0;
        }

        if (x > 1.0 && x > a) {
            return 1.0 - incompleteGammaComplement(a, x);
        }

        /* Compute x**a * exp(-x) / gamma(a) */
        ax = a * Math.log(x) - x - lnGamma(a);
        if (ax < -MAXLOG) {
            return (0.0);
        }

        ax = Math.exp(ax);

        /* power series */
        r = a;
        c = 1.0;
        ans = 1.0;

        do {
            r += 1.0;
            c *= x / r;
            ans += c;
        } while (c / ans > MACHEP);
        return (ans * ax / a);
    }

    /**
     * Returns the Complemented Incomplete Gamma function; formerly named
     * <tt>igamc</tt>.
     *
     * @param a the parameter of the gamma distribution.
     * @param x the integration start point.
     */
    static public double incompleteGammaComplement(double a, double x) throws ArithmeticException {
        final double big = 4.503599627370496e15;

        double ans, ax, c, yc, r, t, y, z;
        double pk, pkm1, pkm2, qk, qkm1, qkm2;

        if (x <= 0 || a <= 0) {
            return 1.0;
        }

        if (x < 1.0 || x < a) {
            return 1.0 - incompleteGamma(a, x);
        }

        ax = a * Math.log(x) - x - lnGamma(a);
        if (ax < -MAXLOG) {
            return 0.0;
        }

        ax = Math.exp(ax);

        /* continued fraction */
        y = 1.0 - a;
        z = x + y + 1.0;
        c = 0.0;
        pkm2 = 1.0;
        qkm2 = x;
        pkm1 = x + 1.0;
        qkm1 = z * x;
        ans = pkm1 / qkm1;

        do {
            c += 1.0;
            y += 1.0;
            z += 2.0;
            yc = y * c;
            pk = pkm1 * z - pkm2 * yc;
            qk = qkm1 * z - qkm2 * yc;
            if (qk != 0) {
                r = pk / qk;
                t = Math.abs((ans - r) / r);
                ans = r;
            } else {
                t = 1.0;
            }

            pkm2 = pkm1;
            pkm1 = pk;
            qkm2 = qkm1;
            qkm1 = qk;
            if (Math.abs(pk) > big) {
                pkm2 *= biginv;
                pkm1 *= biginv;
                qkm2 *= biginv;
                qkm1 *= biginv;
            }
        } while (t > MACHEP);

        return ans * ax;
    }

    /**
     * Compute the error of Stirling's series at the given value.
     * <p>
     * References:
     * <ol>
     * <li>Eric W. Weisstein. "Stirling's Series." From MathWorld--A Wolfram Web
     * Resource. <a target="_blank"
     * href="http://mathworld.wolfram.com/StirlingsSeries.html">
     * http://mathworld.wolfram.com/StirlingsSeries.html</a></li>
     * </ol>
     * </p>
     *
     * @param z the value.
     * @return the Striling's series error.
     */
    static double getStirlingError(double z) {
        double ret;
        if (z < 15.0) {
            double z2 = 2.0 * z;
            if (Math.abs(Math.floor(z2) - z2) > 0) {
                ret = lnGamma(z + 1.0) - (z + 0.5) * Math.log(z) + z - HALF_LOG_2_PI;
            } else {
                ret = EXACT_STIRLING_ERRORS[(int) z2];
            }
        } else {
            double z2 = z * z;
            ret = (0.083333333333333333333 -
                    (0.00277777777777777777778 -
                            (0.00079365079365079365079365 -
                                    (0.000595238095238095238095238 -
                                            0.0008417508417508417508417508 /
                                                    z2) / z2) / z2) / z2) / z;
        }
        return ret;
    }

    /**
     * A part of the deviance portion of the saddle point approximation.
     * <p>
     * References:
     * <ol>
     * <li>Catherine Loader (2000). "Fast and Accurate Computation of Binomial
     * Probabilities.". <a target="_blank"
     * href="http://www.herine.net/stat/papers/dbinom.pdf">
     * http://www.herine.net/stat/papers/dbinom.pdf</a></li>
     * </ol>
     * </p>
     *
     * @param x  the x value.
     * @param mu the average.
     * @return a part of the deviance.
     */
    static double getDeviancePart(double x, double mu) {
        double ret;
        if (Math.abs(x - mu) < 0.1 * (x + mu)) {
            double d = x - mu;
            double v = d / (x + mu);
            double s1 = v * d;
            double s = Double.NaN;
            double ej = 2.0 * x * v;
            v *= v;
            int j = 1;
            while (Double.isNaN(s) || Math.abs(s1 - s) >= 1e-100) {
                s = s1;
                ej *= v;
                s1 = s + ej / ((j * 2) + 1);
                ++j;
            }
            ret = s1;
        } else {
            ret = x * Math.log(x / mu) + mu - x;
        }
        return ret;
    }

    /**
     * Compute the logarithm of the PMF for a binomial densities
     * using the saddle point expansion.
     *
     * @param x the value at which the probability is evaluated.
     * @param n the number of trials.
     * @param p the probability of success.
     * @return log p(x)
     */
    public static double logBinomial(double x, double n, double p) {
        final double q = 1 - p;
        double ret;
        if (x == 0) {
            if (p < 0.1) {
                ret = -getDeviancePart(n, n * q) - n * p;
            } else {
                ret = n * Math.log(q);
            }
        } else if (x == n) {
            if (q < 0.1) {
                ret = -getDeviancePart(n, n * p) - n * q;
            } else {
                ret = n * Math.log(p);
            }
        } else {
            ret = getStirlingError(n) - getStirlingError(x) -
                    getStirlingError(n - x) - getDeviancePart(x, n * p) -
                    getDeviancePart(n - x, n * q);
            double f = (TWO_PI * x * (n - x)) / n;
            ret = -0.5 * Math.log(f) + ret;
        }
        return ret;
    }

    public static double pdfPois(double x, double lb) {
        if (lb == 0) {
            return (x == 0) ? 1.0 : 0.0;
        }
        if (x == 0) {
            return Math.exp(-lb);
        }
        return Math.exp(-getStirlingError(x) - getDeviancePart(x, lb)) / Math.sqrt(TWO_PI * x);
    }

    public static double sqrt(double x) {
        return Math.sqrt(x);
    }

    public static double pow(double x, double power) {
        return Math.pow(x, power);
    }

    public static double log(double x) {
        return Math.log(x);
    }

    public static double exp(double x) {
        return Math.exp(x);
    }

    public static double expm1(double x) {
        double y, a = Math.abs(x);

        if (a < DBL_EPSILON) {
            return x;
        }
        if (a > 0.697) {
            return Math.exp(x) - 1;  /* negligible cancellation */
        }

        if (a > 1e-8) {
            y = Math.exp(x) - 1;
        } else /* Taylor expansion, more accurate in this range */ {
            y = (x / 2 + 1) * x;
        }

        /* Newton step for solving   log(1 + y) = x   for y : */
        /* WARNING: does not work for y ~ -1: bug in 1.5.0 */
        y -= (1 + y) * (Math.log1p(y) - x);
        return y;
    }

    public static double min(double x, double y) {
        return Math.min(x, y);
    }

    public static int min(int x, int y) {
        return Math.min(x, y);
    }

    public static double max(double x, double y) {
        return Math.max(x, y);
    }

    public static int max(int x, int y) {
        return Math.max(x, y);
    }

    public static double abs(double x) {
        return Math.abs(x);
    }

    public static double floor(double x) {
        return Math.floor(x);
    }

    public static int floorDiv(int x, int y) {
        return Math.floorDiv(x, y);
    }

    public static double rint(double x) {
        return Math.rint(x);
    }

    /**
     * Tests if the double values are approximately equal
     *
     * @param a first value
     * @param b second value
     * @return true if equals, false otherwise
     */
    public static boolean eq(double a, double b) {
        return (a - b < SMALL_ERR) && (b - a < SMALL_ERR);
    }

    public static boolean eq(double a, double b, double err) {
        return (a - b < err) && (b - a < err);
    }

    public static double fdist(double x, double d1, double d2) {
        if (x <= 0.0) {
            return 0.0;
        }
        return 1 - betaIncReg(d1 * x / (d1 * x + d2), d1 / 2, d2 / 2);
    }

    public static double log1pExp(double x) {
        if (x > 0) {
            return x + Math.log1p(Math.exp(-x));
        } else {
            return Math.log1p(Math.exp(x));
        }
    }

    public static int significantDigits(double x) {
        return BigDecimal.valueOf(x).scale();
    }

    public static double round(double value, int digits) {
        return BigDecimal.valueOf(value).setScale(digits, RoundingMode.HALF_UP).doubleValue();
    }

    public static double floorMod(double a, double n) {
        return a - n * Math.floor(a / n);
    }

    public static int gcd(int a, int b) {
        while (a != b) {
            if (a > b) {
                a -= b;
            } else {
                b -= a;
            }
        }
        return a;
    }
}
