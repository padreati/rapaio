/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

import java.util.Random;

/**
 * Random number producer used by Rapaio facilities.
 * <p/>
 * For now the implementation uses the standard Java {@link Random}.
 * <p/>
 * In order to have a reproductible analysis you can use
 * same seed in code ({@link #setSeed(long)}).
 *
 * @author Aurelian Tutuianu
 */
public final class RandomSource {

    private static final Random rand = new Random();

    public static void setSeed(long seed) {
        rand.setSeed(seed);
    }


    public static double nextDouble() {
        return rand.nextDouble();
    }

    public static int nextInt(int n) {
        return rand.nextInt(n);
    }
}
