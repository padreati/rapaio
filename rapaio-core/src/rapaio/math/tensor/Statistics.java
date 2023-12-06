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

package rapaio.math.tensor;

public final class Statistics<N extends Number, T extends Tensor<N, T>> {

    private final DType<N, T> dType;
    private final int size;
    private final int nanSize;
    private final N mean;
    private final N nanMean;
    private final N variance;
    private final N nanVariance;

    public Statistics(final DType<N, T> dType, final int size, final int nanSize, N mean, N nanMean, N variance, N nanVariance) {
        this.dType = dType;
        this.size = size;
        this.nanSize = nanSize;
        this.mean = mean;
        this.nanMean = nanMean;
        this.variance = variance;
        this.nanVariance = nanVariance;
    }

    public DType<N, T> dType() {
        return dType;
    }

    public int size() {
        return size;
    }

    public int nanSize() {
        return nanSize;
    }

    public N mean() {
        return mean;
    }

    public N nanMean() {
        return nanMean;
    }

    public N variance() {
        return variance;
    }

    public N nanVariance() {
        return nanVariance;
    }

    public N sampleVariance() {
        return dType.castValue(variance.doubleValue() * size / (size - 1));
    }

    public N nanSampleVariance() {
        return dType.castValue(nanVariance.doubleValue() * nanSize / (nanSize - 1));
    }

    public N std() {
        return dType.castValue(Math.sqrt(variance.doubleValue()));
    }

    public N nanStd() {
        return dType.castValue(Math.sqrt(nanVariance.doubleValue()));
    }

    public N sampleStd() {
        return dType.castValue(Math.sqrt(sampleVariance().doubleValue()));
    }

    public N nanSampleStd() {
        return dType.castValue(Math.sqrt(nanSampleVariance().doubleValue()));
    }
}
