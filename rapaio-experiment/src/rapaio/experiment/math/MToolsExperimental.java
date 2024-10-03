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

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/2/19.
 */
public class MToolsExperimental {

    public static long combinations(int n, int k) {
        long result = 1;
        if (k > n / 2) {
            k = n - k;
        }
        for (int d = 0; d < k; d++) {
            long oldResult = result;
            result = result * (n - d) / (d + 1);
            // check for overflow
            if (oldResult > result) {
                throw new StackOverflowError("long range exceeded");
            }
        }
        return result;
    }

    public static int[] computePrimes(int max) {
        boolean[] flag = new boolean[max + 1];
        int[] primes = new int[max + 1];
        int plen = 0;
        primes[plen++] = 1;
        for (int i = 2; i <= max; i++) {
            if (!flag[i]) {
                primes[plen++] = i;
                for (int j = i; j <= max; j += i) {
                    flag[j] = true;
                }
            }
        }
        int[] p = new int[plen];
        System.arraycopy(primes, 0, p, 0, plen);
        return p;
    }

    public static int[] factors(int n, int[] primes) {
        ArrayList<Integer> factors = new ArrayList<>();
        for (int i = 1; i < primes.length; i++) {
            if (n == 1)
                break;
            while (n % primes[i] == 0) {
                n = n / primes[i];
                factors.add(primes[i]);
            }
        }
        return factors.stream().mapToInt(i -> i).toArray();
    }

}
