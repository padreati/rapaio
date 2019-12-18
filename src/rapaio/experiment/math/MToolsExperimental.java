package rapaio.experiment.math;

import java.util.ArrayList;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/2/19.
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
