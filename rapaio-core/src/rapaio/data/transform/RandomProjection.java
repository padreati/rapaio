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

package rapaio.data.transform;

import java.io.Serial;
import java.util.Random;
import java.util.stream.IntStream;

import rapaio.core.SamplingTools;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarRange;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.Tensors;

/**
 * Builds a random projection of some give numeric features.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/28/16.
 */
public class RandomProjection extends AbstractTransform {

    public static RandomProjection newGaussianSd(Random random, int k, VarRange varRange) {
        return new RandomProjection(random, k, gaussian(k), varRange);
    }

    public static RandomProjection newAchlioptas(Random random, int k, VarRange varRange) {
        return new RandomProjection(random, k, achlioptas(3), varRange);
    }

    public static RandomProjection newAchlioptas(Random random, int k, double s, VarRange varRange) {
        return new RandomProjection(random, k, achlioptas(s), varRange);
    }

    @Serial
    private static final long serialVersionUID = -2790372378136065870L;

    private final int k;
    private final Method method;
    private final Random random;
    private Tensor<Double> rp;

    private RandomProjection(Random random, int k, Method method, VarRange varRange) {
        super(varRange);
        this.random = random;
        this.k = k;
        this.method = method;
    }

    @Override
    public RandomProjection newInstance() {
        return new RandomProjection(random, k, method, varRange);
    }

    @Override
    public void coreFit(Frame df) {
        // build k random projections

        rp = Tensors.zeros(Shape.of(varNames.length, k));
        for (int i = 0; i < k; i++) {
            Tensor<Double> v = method.projection(random, varNames.length);
            for (int j = 0; j < varNames.length; j++) {
                rp.setDouble(v.getDouble(j), j, i);
            }
        }
    }

    @Override
    public Frame coreApply(Frame df) {

        Tensor<Double> X = df.mapVars(varNames).tensor();
        Tensor<Double> p = X.mm(rp);

        Frame non = df.removeVars(VarRange.of(varNames));
        Frame trans = SolidFrame.matrix(p, IntStream.range(1, k + 1).boxed().map(i -> "RP_" + i).toArray(String[]::new));
        return non.bindVars(trans);
    }

    public interface Method {
        Tensor<Double> projection(Random random, int rowCount);
    }

    private static Method gaussian(int k) {
        return (random, rowCount) -> {
            Normal norm = Normal.std();
            Tensor<Double> v = Tensors.zeros(Shape.of(rowCount));
            for (int i = 0; i < v.size(); i++) {
                v.setDouble(norm.sampleNext(random) / Math.sqrt(k), i);
            }
            v.normalize(2.);
            return v;
        };
    }

    private static Method achlioptas(double s) {
        double[] p = new double[3];
        p[0] = 1 / (2 * s);
        p[1] = 1 - 1 / s;
        p[2] = 1 / (2 * s);

        double sqrt = Math.sqrt(s);

        return (random, rowCount) -> {
            int[] sample = SamplingTools.sampleWeightedWR(random, rowCount, p);
            Tensor<Double> v = Tensors.zeros(Shape.of(rowCount));
            for (int i = 0; i < sample.length; i++) {
                if (sample[i] == 0) {
                    v.setDouble(-sqrt, i);
                    continue;
                }
                if (sample[i] == 1) {
                    v.setDouble(0, i);
                    continue;
                }
                v.setDouble(sqrt, i);
            }
            return v.normalize(2.);
        };
    }
}
