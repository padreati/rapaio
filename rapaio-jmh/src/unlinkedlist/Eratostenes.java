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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import commons.Utils;

@BenchmarkMode( {Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class Eratostenes {

    @State(Scope.Benchmark)
    public static class MyState {
        @Param( {"100", "1000", "10000", "100000", "1000000", "10000000", "100000000", "1000000000"})
        private int n;

        @Setup(Level.Invocation)
        public void setup() {
        }
    }

    @Benchmark
    public void testSieveClassic(MyState s, Blackhole bh) {
        bh.consume(sieveClassic(s.n));
    }

    int[] sieveClassic(int n) {
        int len = 0;
        int[] numbers = new int[n + 1];
        for (int i = 0; i < n; i++) {
            numbers[i] = i;
        }
        for (int p = 2; p < n; p++) {
            if (numbers[p] == 0) {
                continue;
            }
            numbers[len++] = p;
            for (int j = p; j < n; j += p) {
                numbers[j] = 0;
            }
        }
        return Arrays.copyOf(numbers, len);
    }

    @Benchmark
    public void testSieveSkip2(MyState s, Blackhole bh) {
        bh.consume(sieveSkip2(s.n));
    }

    int[] sieveSkip2(int n) {
        int[] numbers = new int[n + 1];
        for (int i = 1; i < n; i += 2) {
            numbers[i] = i;
        }
        numbers[0] = 2;
        int len = 1;
        int lim = (int) Math.floor(Math.sqrt(n));
        for (int p = 3; p < n; p += 2) {
            if (numbers[p] == 0) {
                continue;
            }
            numbers[len++] = p;
            if (p < lim) {
                int step = 2 * p;
                for (int j = p * p; j < n; j += step) {
                    numbers[j] = 0;
                }
            }
        }
        return Arrays.copyOf(numbers, len);
    }

    @Benchmark
    public void testSieveSkip2Flags(MyState s, Blackhole bh) {
        bh.consume(sieveSkip2Flags(s.n));
    }

    int[] sieveSkip2Flags(int n) {
        int[] numbers = new int[n + 1];
        boolean[] flags = new boolean[n + 1];
        numbers[0] = 2;
        int len = 1;
        int lim = (int) Math.floor(Math.sqrt(n));
        for (int p = 3; p < n; p += 2) {
            if (flags[p]) {
                continue;
            }
            numbers[len++] = p;
            if (p < lim) {
                int step = 2 * p;
                for (int j = p * p; j < n; j += step) {
                    flags[j] = true;
                }
            }
        }
        return Arrays.copyOf(numbers, len);
    }

    @Benchmark
    public void testSieveSkip23Flags(MyState s, Blackhole bh) {
        bh.consume(sieveSkip23Flags(s.n));
    }

    int[] sieveSkip23Flags(int n) {
        int size = n / 3;
        int[] numbers = new int[size];
        boolean[] flags = new boolean[size];
        numbers[0] = 2;
        numbers[1] = 3;
        int len = 2;
        int lim = (int) Math.floor(Math.sqrt(n));
        int pos = 0;
        while (true) {
            while (pos < size && flags[pos]) {
                pos++;
            }
            if (pos == size) {
                break;
            }
            int p = posToPrime(pos);
            if (p >= n) {
                break;
            }
            numbers[len++] = p;
            if (p < lim) {
                int step = 2 * p;
                for (int j = p * p; j < n; j += step) {
                    int rest = j % 6;
                    if (rest == 1 || rest == 5) {
                        flags[primeToPos(j)] = true;
                    }
                }
            }
            pos++;
        }
        return Arrays.copyOf(numbers, len);
    }

    @Benchmark
    public void testSieveSkip32FlagsParallel(MyState s, Blackhole bh) {
        bh.consume(sieveSkip32FlagsParallel(s.n));
    }

    int[] sieveSkip32FlagsParallel(int n) {
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
                NextPrimes ps = addNextPrimes(numbers, len, flags, pos, n, size);
                if (ps.step == 0) {
                    break;
                }
                List<Callable<Object>> tasks = new LinkedList<>();
                for (int i = len; i < ps.len; i++) {
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
                len = ps.len;
                if (!tasks.isEmpty()) {
                    for (var task : tasks) {
                        s.fork(task);
                    }
                    s.join();
                }

                pos += ps.step;
            }
            s.shutdown();
        } catch (InterruptedException ignored) {
        }
        return Arrays.copyOf(numbers, len);
    }

    record NextPrimes(int step, int len) {
    }

    private NextPrimes addNextPrimes(int[] numbers, int len, boolean[] flags, int pos, int n, int size) {
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
        return new NextPrimes(i - pos, len);
    }

    private int posToPrime(int pos) {
        return ((pos & 1) == 0) ? 3 * pos + 5 : 3 * pos + 4;
    }

    private int primeToPos(int p) {
        int pp = (p + 1) / 3 - 2;
        return (3 * pp + 5 < p) ? pp + 1 : pp;
    }

    public static void main(String[] args) throws RunnerException {
        if (!new Eratostenes().run()) {
            System.exit(0);
        }
        Options opt = new OptionsBuilder()
                .include(Eratostenes.class.getSimpleName())
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(2)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(3)
                .forks(1)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(Eratostenes.class))
                .build();

        new Runner(opt).run();
    }

    private boolean arrayCompare(int[] a, int[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    boolean run() {

        int n = 10_000;
        int[] primes1 = sieveClassic(n);
        int[] primes2 = sieveSkip2(n);
        int[] primes3 = sieveSkip2Flags(n);
        int[] primes4 = sieveSkip23Flags(n);
        int[] primes5 = sieveSkip32FlagsParallel(n);
        System.out.println("Prime numbers count " + primes1.length);
        System.out.println("Prime numbers count " + primes2.length);
        System.out.println("Prime numbers count " + primes3.length);
        System.out.println("Prime numbers count " + primes4.length);
        System.out.println("Prime numbers count " + primes5.length);
        if (!arrayCompare(primes1, primes2)) {
            return false;
        }
        if (!arrayCompare(primes1, primes3)) {
            return false;
        }
        if (!arrayCompare(primes1, primes5)) {
            return false;
        }
        return arrayCompare(primes1, primes4);
    }
}

