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

package unlinkedlist;

import static rapaio.graphics.Plotter.points;
import static rapaio.graphics.opt.GOptions.pch;
import static rapaio.graphics.opt.GOptions.sz;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;

import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.sys.WS;

public class PlotPrimes {

    int[] sieve(int n) {
        int size = n / 3;
        int[] numbers = new int[size + 1];
        boolean[] flags = new boolean[size + 1];
        numbers[0] = 2;
        numbers[1] = 3;
        int len = 2;
        int lim = (int) Math.floor(Math.sqrt(n));
        int pos = 0;
        try (StructuredTaskScope<Object> s = new StructuredTaskScope<>()) {
            while (true) {
                while (pos < size && flags[pos]) {
                    pos++;
                }
                if (pos == size) {
                    break;
                }
                Eratostenes.NextPrimes ps = addNextPrimes(numbers, len, flags, pos, n, size);
                if (ps.step() == 0) {
                    break;
                }
                List<Callable<Object>> tasks = new LinkedList<>();
                for (int i = len; i < ps.len(); i++) {
                    int pp = numbers[i];
                    if (pp < lim) {
                        tasks.add(() -> {
                            int step = 2 * pp;
                            for (int j = pp * pp; j < n; j += step) {
                                int rest = j % 6;
                                if (rest == 1 || rest == 5) {
                                    flags[primeToPos(j)] = true;
                                }
                            }
                            return null;
                        });
                    }
                }
                len = ps.len();
                if (!tasks.isEmpty()) {
                    for (var task : tasks) {
                        s.fork(task);
                    }
                    s.join();
                }

                pos += ps.step();
            }
            s.shutdown();
        } catch (InterruptedException ignored) {
        }
        return Arrays.copyOf(numbers, len);
    }

    record NextPrimes(int step, int len) {
    }

    private Eratostenes.NextPrimes addNextPrimes(int[] numbers, int len, boolean[] flags, int pos, int n, int size) {
        int p = posToPrime(pos);
        int square = Math.min(p * p, n);
        int i = pos;
        while (i < size) {
            if (flags[i]) {
                i++;
                continue;
            }
            int pp = posToPrime(i);
            if (pp >= square) {
                break;
            }
            numbers[len++] = pp;
            i++;
        }
        return new Eratostenes.NextPrimes(i - pos, len);
    }

    private int posToPrime(int pos) {
        return ((pos & 1) == 0) ? 3 * pos + 5 : 3 * pos + 4;
    }

    private int primeToPos(int p) {
        int pp = (p + 1) / 3 - 2;
        return (3 * pp + 5 < p) ? pp + 1 : pp;
    }

    public static void main(String[] args) {
        new PlotPrimes().run();
    }

    //    private static final int n = 1_000_000_000;
    private static final int n = 100_000_000;

    void run() {
        int[] primes = sieve(n);

        Var r = VarDouble.from(primes.length, i -> (double)primes[i]);
        Var theta = VarDouble.from(primes.length, i -> (double) primes[i]);

        Var x = VarDouble.from(primes.length, i -> r.getDouble(i) * Math.cos(theta.getDouble(i))).name("x");
        Var y = VarDouble.from(primes.length, i -> r.getDouble(i) * Math.sin(theta.getDouble(i))).name("y");

        WS.draw(points(x, y, pch.circleFull(), sz(0.5)));
    }
}
