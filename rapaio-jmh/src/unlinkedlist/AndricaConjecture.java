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

package unlinkedlist;

public class AndricaConjecture {

    public static void main(String[] args) {
        new AndricaConjecture().run();
    }

    void run() {
        long[] primes = primes(10000000);
        for(int i=0; i<primes.length; i++) {
            System.out.println((i+1) + ": " + primes[i]);
        }
    }

    long[] primes(final int max) {
        int len = 0;
        long[] primes = new long[max];
        long p = 2;
        while (len < max) {
            long sqrtp = (long) Math.floor(Math.sqrt(p));
            boolean found = true;
            for (int i = 0; i < len; i++) {
                if (primes[i] > sqrtp) {
                    break;
                }
                if (p % primes[i] == 0) {
                    found = false;
                    break;
                }
            }
            if (found) {
                primes[len++] = p;
            }
            p++;
        }
        return primes;
    }
}
