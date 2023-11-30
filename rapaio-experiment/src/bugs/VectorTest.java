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

package bugs;

import java.util.Random;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

public class VectorTest {

    private static final VectorSpecies<Double> SPEC = DoubleVector.SPECIES_PREFERRED;

    private int[] indexes(int stride) {
        int[] indexes = new int[SPEC.length()];
        for (int i = 1; i < indexes.length; i++) {
            indexes[i] = indexes[i - 1] + stride;
        }
        return indexes;
    }

    public double vdotVectorizedFailure(double[] array1, int offset1, int stride1, double[] array2, int offset2, int stride2, int start,
            int end) {
        int start1 = offset1 + start * stride1;
        int start2 = offset2 + start * stride2;
        int i = 0;
        int loopBound = SPEC.loopBound(end - start);
        DoubleVector vsum = DoubleVector.zero(SPEC);

        int[] indexes1 = indexes(stride1);
        int[] indexes2 = indexes(stride2);

        // we collect info about strides outside the loop for performance reasons
        // (it creates better conditions for JIT optimizations)
        // but it fails to execute properly
        boolean unit1 = stride1 == 1;
        boolean unit2 = stride2 == 1;

        for (; i < loopBound; i += SPEC.length()) {
            DoubleVector a = unit1 ?
                    DoubleVector.fromArray(SPEC, array1, start1) :
                    DoubleVector.fromArray(SPEC, array1, start1, indexes1, 0);
            DoubleVector b = unit2 ?
                    DoubleVector.fromArray(SPEC, array2, start2) :
                    DoubleVector.fromArray(SPEC, array2, start2, indexes2, 0);
            vsum = vsum.add(a.mul(b));
            start1 += SPEC.length() * stride1;
            start2 += SPEC.length() * stride2;
        }

        double sum = vsum.reduceLanes(VectorOperators.ADD);
        for (; i < end - start; i++) {
            sum += array1[start1] * array2[start2];
            start1 += stride1;
            start2 += stride2;
        }
        return sum;
    }

    public double vdotVectorizedSuccess(double[] array1, int offset1, int stride1, double[] array2, int offset2, int stride2, int start,
            int end) {
        int start1 = offset1 + start * stride1;
        int start2 = offset2 + start * stride2;
        int i = 0;
        int loopBound = SPEC.loopBound(end - start);
        DoubleVector vsum = DoubleVector.zero(SPEC);

        int[] indexes1 = indexes(stride1);
        int[] indexes2 = indexes(stride2);

        // if conditions are inside the loop it does not fail
        for (; i < loopBound; i += SPEC.length()) {
            DoubleVector a = stride1 == 1 ?
                    DoubleVector.fromArray(SPEC, array1, start1) :
                    DoubleVector.fromArray(SPEC, array1, start1, indexes1, 0);
            DoubleVector b = stride2 == 1 ?
                    DoubleVector.fromArray(SPEC, array2, start2) :
                    DoubleVector.fromArray(SPEC, array2, start2, indexes2, 0);
            vsum = vsum.add(a.mul(b));
            start1 += SPEC.length() * stride1;
            start2 += SPEC.length() * stride2;
        }

        double sum = vsum.reduceLanes(VectorOperators.ADD);
        for (; i < end - start; i++) {
            sum += array1[start1] * array2[start2];
            start1 += stride1;
            start2 += stride2;
        }
        return sum;
    }

    public static void main(String[] args) {
        new VectorTest().run();
    }

    public void run() {

        boolean failure = true;

        Random random = new Random(42);

        int times = 20_000;
        final int N = 100_000_000;

        double[] array1 = new double[N];
        double[] array2 = new double[N];
        for (int i = 0; i < N; i++) {
            array1[i] = random.nextDouble();
            array2[i] = random.nextDouble();
        }

        while (times-- > 0) {

            int r1 = random.nextInt(20_000);
            int r2 = random.nextInt(20_000);

            int start = Math.min(r1, r2);
            int end = Math.max(r1, r2);

            int offset1 = random.nextInt(10_000);
            int offset2 = random.nextInt(10_000);
            int stride1 = random.nextInt(100) + 1;
            int stride2 = random.nextInt(100) + 1;

            if (failure) {
                vdotVectorizedFailure(array1, offset1, stride1, array2, offset2, stride2, start, end);
            } else {
                vdotVectorizedSuccess(array1, offset1, stride1, array2, offset2, stride2, start, end);
            }
        }
    }
}
