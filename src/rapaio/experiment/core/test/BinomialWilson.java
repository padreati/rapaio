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

package rapaio.experiment.core.test;

import static rapaio.graphics.Plotter.*;

import rapaio.core.distributions.Normal;
import rapaio.data.VarDouble;
import rapaio.graphics.plot.Plot;
import rapaio.sys.WS;

public class BinomialWilson {

    private static double center(double phat, double z, double n) {
        return (phat + z * z / (2 * n)) / (1 + z * z / n);
    }

    private static double delta(double phat, double z, double n) {
        return z / (1 + z * z / n) * Math.sqrt(phat * (1 - phat) / n + z * z / (4 * n * n));
    }

    public static double lbound(double q, double k, double n) {
        double z = Normal.std().quantile(q);
        double phat = k / n;
        return center(phat, z, n) - delta(phat, z, n);
    }

    public static double ubound(double q, double k, double n) {
        double z = Normal.std().quantile(q);
        double phat = k / n;
        return center(phat, z, n) + delta(phat, z, n);
    }

    public static void main(String[] args) {
        int nmax = 100;
        double q = 0.997;

        double min = 0.05;

        VarDouble ns = VarDouble.empty();
        VarDouble ks = VarDouble.empty();
        for (int n = 1; n < nmax; n++) {
            ns.addDouble(n);
            for (int k = 0; k < n+1; k++) {
                double lbound = lbound(q, k, n);
                if(lbound>=min) {
                    ks.addDouble(k);
                    System.out.printf("n: %d, k: %d, lbound:%f\n", n, k, lbound);
                    break;
                }
            }
        }
        WS.draw(points(ns, ks, sz(2)));
    }

    public static void main1(String[] args) {
        int nmax = 300;
        double q = 0.997;

        int lwd = 2;

        Plot p = plot();

        for (int n = 1; n < nmax; n++) {
            VarDouble lbound = VarDouble.empty();
            for (int k = 0; k < n + 1; k++) {
                lbound.addDouble(lbound(q, k, n));
            }
            VarDouble ubound = VarDouble.empty();
            for(int k=0; k<n+1; k++) {
                ubound.addDouble(ubound(q, k, n));
            }

//            VarDouble x = VarDouble.seq(0, 1, 1.0/n);
            p.lines( lbound, color(n), lwd(lwd));
            p.lines( ubound, color(n), lwd(lwd));
        }
        WS.draw(p);
    }
}
