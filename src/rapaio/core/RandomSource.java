/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.core;

import java.io.Serial;
import java.io.Serializable;
import java.util.Random;

/**
 * Random number producer used by rapaio facilities. Currently the implementation is a wrapper
 * over standard Java {@link Random}.
 * <p>
 * In order to have a reproducible analysis you can use same seed in code ({@link #setSeed(long)}).
 *
 * @author Aurelian Tutuianu
 */
public final class RandomSource implements Serializable {

    @Serial
    private static final long serialVersionUID = -1201316989986445607L;

    private static final Random rand = new Random();

    private RandomSource() {
    }

    /**
     * Set seed of random number generator.
     *
     * @param seed seed
     */
    public static void setSeed(long seed) {
        rand.setSeed(seed);
    }

    /**
     * Returns the next pseudorandom, uniformly distributed
     * {@code double} value between {@code 0.0} and
     * {@code 1.0} from this random number generator's sequence.
     */
    public static double nextDouble() {
        return rand.nextDouble();
    }

    public static int nextInt() {
        return rand.nextInt();
    }

    public static int nextInt(int n) {
        return rand.nextInt(n);
    }

    public static Random getRandom() {
        return rand;
    }
}

