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

package rapaio.experiment.core;

import static java.lang.Math.*;
import static rapaio.sys.WS.printf;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/9/17.
 */
public class AnDarl {

/*
A procedure for evaluating the limiting distribution of the
             Anderson-Darling statistic A_n=
-n-(1/n)[ln(x_1(1-x_n)+3ln(x_2(1-x_{n-1})+5ln(x_3(1-x_{n-2})+...+(2n-1)ln(x_n(1-x_1))]
    where x_1<x_2<...<x_n is an ordered set of purported uniform [0,1) variates.
The function is ADinf(z)=lim_{n->infty} Pr[A_n<z]. About 15 digit accuracy.
If you don't need that much accuracy, use the quick-and-easy adinf(z).
ADinf uses a two-term recursion for coefficients in series for which initial values
require the complementary normal integral, included as cPhi(z).
Otherwise, use erfc() if your C compiler has one with adequate accuracy.
*/

    double ADf(double z, int j) { /* called by ADinf(); see article. */
        double t, f, fnew, a, b, c, r;
        int i;
        t = (4 * j + 1) * (4 * j + 1) * 1.23370055013617 / z;
        if (t > 150.) return 0.;
        a = 2.22144146907918 * exp(-t) / sqrt(t);
        b = 3.93740248643060 * 2. * cPhi(sqrt(2 * t));/* initialization requires cPhi */
        /*if you have erfc(), replace 2*cPhi(sqrt(2*t)) with erfc(sqrt(t))*/
        r = z * .125;
        f = a + b * r;
        for (i = 1; i < 200; i++) {
            c = ((i - .5 - t) * b + t * a) / i;
            a = b;
            b = c;
            r *= z / (8 * i + 8);
            if (abs(r) < 1e-40 || abs(c) < 1.e-40) return f;
            fnew = f + c * r;
            if (f == fnew) return f;
            f = fnew;
        }
        return f;
    }

    double ADinf(double z) {
        int j;
        double ad, adnew, r;
        if (z < .01) return 0.; /* avoids exponent limits; ADinf(.01)=.528e-52 */
        r = 1. / z;
        ad = r * ADf(z, 0);
        for (j = 1; j < 100; j++) {
            r *= (.5 - j) / j;
            adnew = ad + (4 * j + 1) * r * ADf(z, j);
            if (ad == adnew) {
                return ad;
            }
            ad = adnew;
        }
        return ad;
    }

    /*
    Complementary normal distribution function
       cPhi(x) = integral from x to infinity of phi(x)=exp(-.5*t^2)/sqrt(2*pi)
     13-15 digit accuracy for abs(x)<16.
    Stores R(0),R(2),R(4),...,R(16), with cPhi(x)=R(x)*phi(x), phi normal density,
    then uses Taylor series for R(z+h)=R(z)+hR'(z)+(1/2)h^2R''(z)+...
    with -1<h<1,and R(z) one of R(0),R(2),R(4),...,R(16) stored as v[0],v[1],...v[8].
    Examples: cPhi(2.75) needs R(2.75) and 2.75=2+.75 so use h=.75 and R(2)=v[1],
              cPhi(3.3)  needs R(3.3) and 3.3=4-.7, so use h=-.7 and R(4)=v[2].
    */
    double cPhi(double x) {
        double v[] = {1.25331413731550025, .421369229288054473, .236652382913560671,
                .162377660896867462, .123131963257932296, .0990285964717319214,
                .0827662865013691773, .0710695805388521071, .0622586659950261958};
        double h, a, b, z, t, s, pwr;
        int i, j;
        j = (int) ((abs(x) + 1.) / 2.);
        a = v[j];
        z = 2 * j;
        h = abs(x) - z;
        b = z * a - 1;
        pwr = 1;
        s = a + h * b;
        for (i = 2; i < 100; i += 2) {/* begin i loop */
            a = (a + z * b) / i;
            b = (b + z * a) / (i + 1);
            pwr = pwr * h * h;
            t = s;
            s = s + pwr * (a + h * b);
            if (s == t) {
                s *= exp(-.5 * x * x - .91893853320467274178);
                return (x > 0) ? s : 1 - s;
            }
        }/* end i loop */
        return (x > 0) ? s : 1 - s;
    }


/*
    Anderson-Darling test for uniformity.   Given an ordered set
              x_1<x_2<...<x_n
 of purported uniform [0,1) variates,  compute
          a = -n-(1/n)*[ln(x_1*z_1)+3*ln(x_2*z_2+...+(2*n-1)*ln(x_n*z_n)]
 where z_1=1-x_n, z_2=1-x_(n-1)...z_n=1-x_1, then find
  v=adinf(a) and return  p=v+errfix(v), which should be uniform in [0,1),
  that is, the p-value associated with the observed x_1<x_2<...<x_n.
*/

    /* Short, practical version of full ADinf(z), z>0.   */
    double adinf(double z) {
        if (z < 2.) return exp(-1.2337141 / z) / sqrt(z) * (2.00012 + (.247105 -
                (.0649821 - (.0347962 - (.011672 - .00168691 * z) * z) * z) * z) * z);
        /* max |error| < .000002 for z<2, (p=.90816...) */
        return
                exp(-exp(1.0776 - (2.30695 - (.43424 - (.082433 - (.008056 - .0003146 * z) * z) * z) * z) * z));
        /* max |error|<.0000008 for 4<z<infinity */
    }

    /*
    The procedure  errfix(n,x)  corrects the error caused
    by using the asymptotic approximation, x=adinf(z).
    Thus x+errfix(n,x) is uniform in [0,1) for practical purposes;
    accuracy may be off at the 5th, rarely at the 4th, digit.
    */
    double errfix(int n, double x) {
        double c, t;
        if (x > .8) return
                (-130.2137 + (745.2337 - (1705.091 - (1950.646 - (1116.360 - 255.7844 * x) * x) * x) * x) * x) / n;
        c = .01265 + .1757 / n;
        if (x < c) {
            t = x / c;
            t = sqrt(t) * (1. - t) * (49 * t - 102);
            return t * (.0037 / (n * n) + .00078 / n + .00006) / n;
        }
        t = (x - c) / (.8 - c);
        t = -.00022633 + (6.54034 - (14.6538 - (14.458 - (8.259 - 1.91864 * t) * t) * t) * t) * t;
        return t * (.04213 + .01365 / n) / n;
    }

/* The function AD(n,z) returns Prob(A_n<z) where
    A_n = -n-(1/n)*[ln(x_1*z_1)+3*ln(x_2*z_2+...+(2*n-1)*ln(x_n*z_n)]
          z_1=1-x_n, z_2=1-x_(n-1)...z_n=1-x_1, and
    x_1<x_2<...<x_n is an ordered set of iid uniform [0,1) variates.
*/

    double AD(int n, double z) {
        double c, v, x;
        x = adinf(z);
        /* now x=adinf(z). Next, get v=errfix(n,x) and return x+v; */
        if (x > .8) {
            v = (-130.2137 + (745.2337 - (1705.091 - (1950.646 - (1116.360 - 255.7844 * x) * x) * x) * x) * x) / n;
            return x + v;
        }
        c = .01265 + .1757 / n;
        if (x < c) {
            v = x / c;
            v = sqrt(v) * (1. - v) * (49 * v - 102);
            return x + v * (.0037 / (n * n) + .00078 / n + .00006) / n;
        }
        v = (x - c) / (.8 - c);
        v = -.00022633 + (6.54034 - (14.6538 - (14.458 - (8.259 - 1.91864 * v) * v) * v) * v) * v;
        return x + v * (.04213 + .01365 / n) / n;
    }

/* You must give the ADtest(int n, double *x) routine a sorted array
       x[0]<=x[1]<=..<=x[n-1]
    that you are testing for uniformity.
   It will return the p-value associated
   with the Anderson-Darling test, using
    the above adinf() and errfix( ,   )
         Not well-suited for n<7,
     (accuracy could drop to 3 digits).
*/

    double ADtest(int n, double[] x) {
        int i;
        double t, z = 0;
        for (i = 0; i < n; i++) {
            t = x[i] * (1. - x[n - 1 - i]);
            z = z - (i + i + 1) * log(t);
        }
        return AD(n, -n + z / n);
    }


    void run() {

        double zz = 1.2;
        System.out.println(String.format("     lim n->infty Prob(A_n < %f) = %18.16f\n", zz, ADinf(zz)));

        int i, n;
        double x, z;
        double u[] = {
                .0392, .0884, .260, .310, .454, .644, .797, .813, .921, .960
        };
        double w[] = {
                .0015, .0078, .0676, .0961, .106, .107, .835, .861, .948, .992
        };
        printf("The AD test for uniformity of \n{ ");
        for (i = 0; i < 10; i++) printf("%4.3f,", u[i]);
        printf("\b}:\n      p = %8.5f\n", ADtest(10, u));

        printf("The AD test for uniformity of \n{ ");
        for (i = 0; i < 10; i++) printf("%4.3f,", w[i]);
        printf("\b}:\n      p = %8.5f\n", ADtest(10, w));
    }

    public static void main(String[] args) {
        new AnDarl().run();
    }
}


