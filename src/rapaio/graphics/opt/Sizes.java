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

package rapaio.graphics.opt;

import rapaio.util.collection.DoubleArrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/25/20.
 */
public record Sizes(boolean absolute, double[] relativeSizes, int[] absoluteSizes) {

    public double[] getRelativeSizes(int length, double totalSize) {
        double[] dimensions = DoubleArrays.newFill(length, -1);
        System.arraycopy(relativeSizes, 0, dimensions, 0, relativeSizes.length);
        int countExpandable = 0;
        double sum = 0.0;
        for (double dimension : dimensions) {
            if (dimension == -1) {
                countExpandable++;
            } else {
                sum += dimension;
            }
        }
        if (countExpandable > 0 && sum >= 1) {
            return DoubleArrays.newFill(length, 0);
        }

        if (countExpandable > 0) {
            double dimExpandable = (1 - sum) / countExpandable;
            for (int i = 0; i < dimensions.length; i++) {
                if (dimensions[i] == -1) {
                    dimensions[i] = dimExpandable;
                }
            }
        } else {
            if (sum > 0) {
                for (int i = 0; i < dimensions.length; i++) {
                    dimensions[i] /= sum;
                }
            }
        }
        DoubleArrays.mul(dimensions, 0, totalSize, dimensions.length);
        return dimensions;
    }

    public double[] getAbsoluteSizes(int length, double totalSize) {
        double[] dimensions = DoubleArrays.newFill(length, -1);
        for (int i = 0; i < absoluteSizes.length; i++) {
            dimensions[i] = absoluteSizes[i];
        }
        int countExpandable = 0;
        int sum = 0;
        for (double dimension : dimensions) {
            if (dimension == -1) {
                countExpandable++;
            } else {
                sum += dimension;
            }
        }
        if (sum > totalSize) {
            return DoubleArrays.newFill(length, 0);
        }
        double dimExpandable = (totalSize - sum) / countExpandable;
        for (int i = 0; i < dimensions.length; i++) {
            if (dimensions[i] == -1) {
                dimensions[i] = dimExpandable;
            }
        }
        return dimensions;
    }

    public double[] computeSizes(int length, double totalSize) {
        return absolute() ? getAbsoluteSizes(length, totalSize) : getRelativeSizes(length, totalSize);
    }
}
