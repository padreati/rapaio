/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.experiment.math;

import java.util.ArrayList;
import java.util.Arrays;

import rapaio.data.VarDouble;
import rapaio.graphics.Plotter;
import rapaio.sys.WS;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/2/19.
 */
public class MToolsExperimental {


    public static int[] primes(int max) {
        boolean[] flag = new boolean[max + 1];
        int[] primes = new int[max + 1];
        int len = 0;
        primes[len++] = 1;
        int i = 2;
        for (; i * i <= max && i * i > 0; i++) {
            if (!flag[i]) {
                primes[len++] = i;
                int j = i * i;
                if (j < 0) {
                    continue;
                }
                for (; j <= max; j += i) {
                    flag[j] = true;
                }
            }
        }
        for (; i <= max; i++) {
            if (!flag[i]) {
                primes[len++] = i;
            }
        }
        return Arrays.copyOf(primes, len);
    }

    public static int[] factors(int n, int[] primes) {
        ArrayList<Integer> factors = new ArrayList<>();
        for (int i = 1; i < primes.length; i++) {
            if (n == 1) {
                break;
            }
            while (n % primes[i] == 0) {
                n = n / primes[i];
                factors.add(primes[i]);
            }
        }
        return factors.stream().mapToInt(i -> i).toArray();
    }


    public static void main(String[] args) {
        int n = 1_000_000;
        int[] primes = primes(n);
        VarDouble x = VarDouble.empty().name("x");
        VarDouble y = VarDouble.empty().name("y");
        for (int i = 0; i < primes.length - 1; i++) {
//                System.out.println(primes[i] + " " + primes[i + 1]);
            x.addDouble(Math.log(primes[i] / (1. * i)));
            y.addDouble(i);
        }
        WS.draw(Plotter.lines(y, x).hLine(Math.E));
    }
}
