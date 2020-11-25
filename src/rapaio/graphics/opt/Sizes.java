package rapaio.graphics.opt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import rapaio.util.collection.DoubleArrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/25/20.
 */
@RequiredArgsConstructor
public final class Sizes {

    @Getter
    private final boolean absolute;
    private final double[] relativeSizes;
    private final int[] absoluteSizes;

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
        DoubleArrays.mult(dimensions, 0, totalSize, dimensions.length);
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
        return isAbsolute() ? getAbsoluteSizes(length, totalSize) : getRelativeSizes(length, totalSize);
    }
}
